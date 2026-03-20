import Network.DonationDriverService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server extends JFrame implements DonationDriverService {

    private static final int PORT = 5267;
    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;
    private Registry registry;
    @SuppressWarnings("unused")
    private final Set<String> activeSessions = new HashSet<>();

    // Lazily initialized via reflection so this project can still compile
    // even if Gson is not yet added to the classpath.
    private static final Object GSON = initGson();

    private static Object initGson() {
        try {
            Class<?> gsonBuilderCls = Class.forName("com.google.gson.GsonBuilder");
            Object builder = gsonBuilderCls.getConstructor().newInstance();

            // Optional: prevent HTML escaping in JSON (helps when embedding rich text).
            try {
                gsonBuilderCls.getMethod("disableHtmlEscaping").invoke(builder);
            } catch (Exception ignored) {
                // Method not present in older Gson versions; safe to ignore.
            }

            // GsonBuilder#create()
            return gsonBuilderCls.getMethod("create").invoke(builder);
        } catch (Throwable ignored) {
            // Gson not present on the classpath yet.
            return null;
        }
    }

    /**
     * Convenience wrapper for future JSON modeling.
     * Returns null if Gson is not on the classpath.
     */
    @SuppressWarnings("unused")
    private static String toJsonWithGson(Object obj) {
        if (GSON == null || obj == null) return null;
        try {
            return (String) GSON.getClass().getMethod("toJson", Object.class).invoke(GSON, obj);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /* ===================== JSON persistence ===================== */
    private static final File DATA_DIR = resolveDataDir();

    private static File resolveDataDir() {
        // Try to locate the project folder containing `DonationDriverUI/`.
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File donationDriverDir = new File(dir, "DonationDriverUI");
            if (donationDriverDir.isDirectory()) {
                File dataDir = new File(donationDriverDir, "data");
                dataDir.mkdirs();
                return dataDir;
            }
        }
        // Fallback (still creates a local folder)
        File fallback = new File(cwd, "DonationDriverUI/data");
        fallback.mkdirs();
        return fallback;
    }

    private static File usersFile() {
        return new File(DATA_DIR, "users.json");
    }

    private static File ticketsFile() {
        return new File(DATA_DIR, "tickets.json");
    }

    private static File drivesFile() {
        return new File(DATA_DIR, "drives.json");
    }

    private static File activeRidersFile() {
        return new File(DATA_DIR, "active_riders.json");
    }

    private static File serverLogsFile() {
        return new File(DATA_DIR, "server_logs.json");
    }

    private static File serverSettingsFile() {
        return new File(DATA_DIR, "server_settings.json");
    }

    private static Object requireGson() {
        if (GSON == null) {
            throw new IllegalStateException(
                    "Gson not found on the classpath. Please add Gson dependency/jar to run Server JSON features.");
        }
        return GSON;
    }

    /**
     * Generic list loader for JSON arrays.
     * <p>
     * Implementation detail: parses JSON into a raw List and then converts each element to {@code elementClass}.
     */
    private static <T> List<T> loadList(File file, Class<T> elementClass) {
        if (file == null || !file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }

        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            if (json.isEmpty()) return new ArrayList<>();

            Object gson = requireGson();

            // Gson#fromJson(String, Class) exists (but we use List.class to avoid TypeToken dependency).
            @SuppressWarnings("unchecked")
            List<Object> rawList = (List<Object>) gson.getClass()
                    .getMethod("fromJson", String.class, Class.class)
                    .invoke(gson, json, List.class);

            List<T> out = new ArrayList<>();
            for (Object rawEl : rawList) {
                Object elAsJson = gson.getClass().getMethod("toJson", Object.class).invoke(gson, rawEl);
                @SuppressWarnings("unchecked")
                T converted = (T) gson.getClass()
                        .getMethod("fromJson", String.class, Class.class)
                        .invoke(gson, elAsJson, elementClass);
                out.add(converted);
            }
            return out;
        } catch (Exception e) {
            // If parsing fails, fail safe to an empty list.
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Generic list saver for JSON arrays.
     */
    private static <T> void saveList(File file, List<T> list) {
        if (file == null) return;
        if (list == null) list = new ArrayList<>();

        try {
            Object gson = requireGson();
            String json = (String) gson.getClass().getMethod("toJson", Object.class).invoke(gson, list);

            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ServerSettings {
        public boolean maintenanceEnabled = false;
    }

    private static ServerSettings loadServerSettings() {
        Object gson = requireGson();
        if (!serverSettingsFile().exists() || serverSettingsFile().length() == 0) {
            return new ServerSettings();
        }

        try {
            String json = Files.readString(serverSettingsFile().toPath(), StandardCharsets.UTF_8);
            return (ServerSettings) gson.getClass()
                    .getMethod("fromJson", String.class, Class.class)
                    .invoke(gson, json, ServerSettings.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new ServerSettings();
        }
    }

    private static void saveServerSettings(ServerSettings settings) {
        if (settings == null) settings = new ServerSettings();
        Object gson = requireGson();

        try {
            String json = (String) gson.getClass().getMethod("toJson", Object.class).invoke(gson, settings);
            File parent = serverSettingsFile().getParentFile();
            if (parent != null) parent.mkdirs();
            Files.writeString(serverSettingsFile().toPath(), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ===================== XML response builders ===================== */
    private static String xmlResponse(String status, String message, String role) {
        return "<response>"
                + "<status>" + escapeXml(status) + "</status>"
                + "<message>" + escapeXml(message == null ? "" : message) + "</message>"
                + "<role>" + escapeXml(role == null ? "" : role) + "</role>"
                + "</response>";
    }

    private static String ok(String message) {
        return xmlResponse("OK", message, "");
    }

    private static String ok(String message, String role) {
        return xmlResponse("OK", message, role);
    }

    private static String error(String message) {
        return xmlResponse("ERROR", message, "");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        // Must escape at least &,<,> so the XML response stays well-formed.
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /* ===================== Ticket/Drive XML builders ===================== */
    private static String ticketToXml(Ticket t) {
        if (t == null) return "";
        return "<ticket>"
                + "<ticketId>" + safe(t.ticketId) + "</ticketId>"
                + "<userId>" + safe(t.userId) + "</userId>"
                + "<riderId>" + safe(t.riderId) + "</riderId>"
                + "<status>" + safe(t.status) + "</status>"
                + "<itemCategory>" + safe(t.itemCategory) + "</itemCategory>"
                + "<quantity>" + t.quantity + "</quantity>"
                + "<condition>" + safe(t.condition) + "</condition>"
                + "<expirationDate>" + safe(t.expirationDate) + "</expirationDate>"
                + "<pickupDateTime>" + safe(t.pickupDateTime) + "</pickupDateTime>"
                + "<pickupLocation>" + safe(t.pickupLocation) + "</pickupLocation>"
                + "<photoPath>" + safe(t.photoPath) + "</photoPath>"
                + "<notes>" + safe(t.details) + "</notes>"
                + "<donationDrive>" + safe(t.donationDrive) + "</donationDrive>"
                + "<deliveryDestination>" + safe(t.deliveryDestination) + "</deliveryDestination>"
                + (t.qualityStatus != null ? "<qualityStatus>" + safe(t.qualityStatus) + "</qualityStatus>" : "")
                + (t.qualityReason != null ? "<qualityReason>" + safe(t.qualityReason) + "</qualityReason>" : "")
                + "</ticket>";
    }

    private static String driveToXml(Drive d) {
        if (d == null) return "";
        return "<drive>"
                + "<title>" + safe(d.title) + "</title>"
                + "<description>" + safe(d.description) + "</description>"
                + "<targetAmount>" + d.targetAmount + "</targetAmount>"
                + "<currentAmount>" + d.currentAmount + "</currentAmount>"
                + "</drive>";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isMaintenanceMode() {
        try {
            return loadServerSettings().maintenanceEnabled;
        } catch (Exception e) {
            return false;
        }
    }

    public Server() throws RemoteException {
        super("DonationDriver RMI Server");
        initUI();
    }

    private void initUI() {
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        startBtn = new JButton("Start Server");
        stopBtn = new JButton("Stop Server");
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startServer());
        stopBtn.addActionListener(e -> stopServer());

        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void startServer() {
        try {
            registry = LocateRegistry.createRegistry(PORT);
            UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("rmi://localhost:" + PORT + "/DonationDriverService", this);
            log("RMI Server started and bound on port " + PORT);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        } catch (Exception e) {
            log("Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        try {
            Naming.unbind("rmi://localhost:" + PORT + "/DonationDriverService");
            UnicastRemoteObject.unexportObject(this, true);
            UnicastRemoteObject.unexportObject(registry, true);
            log("RMI Server stopped.");
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        } catch (Exception e) {
            log("Error stopping server: " + e.getMessage());
        }
    }

    @Override
    public String login(String email, String password) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");

        if (email == null || password == null) return error("Missing email or password.");
        String emailNorm = email.trim().toLowerCase();

        List<User> users = loadList(usersFile(), User.class);
        for (User u : users) {
            if (u != null
                    && u.email != null
                    && u.email.trim().toLowerCase().equals(emailNorm)
                    && password.equals(u.password)) {
                return ok("Login successful.", u.role != null ? u.role : "");
            }
        }
        return error("Invalid email or password.");
    }

    @Override
    public String register(String email, String password) throws RemoteException {
        return register("", "", "", "", "", "", email, password, "DONOR");
    }

    @Override
    public String register(String firstName, String lastName, String middleName,
                               String dateOfBirth, String address, String phone,
                               String email, String password, String role) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");

        if (email == null || password == null) return error("Missing email or password.");
        String emailNorm = email.trim().toLowerCase();
        String roleNorm = (role == null || role.trim().isEmpty()) ? "DONOR" : role.trim().toUpperCase();

        List<User> users = loadList(usersFile(), User.class);
        for (User u : users) {
            if (u != null && u.email != null && u.email.trim().toLowerCase().equals(emailNorm)) {
                return error("Email is already registered.");
            }
        }

        User u = new User();
        u.email = emailNorm;
        u.password = password;
        u.role = roleNorm;
        users.add(u);
        saveList(usersFile(), users);

        // Keep message consistent with Client.parseResponse() usage in RegistrationController.
        return ok("Registration successful.", roleNorm);
    }

    @Override
    public String logout(String email) throws RemoteException {
        // Current UI doesn't rely on logout persistence; return OK for compatibility.
        return ok("Logged out.");
    }

    @Override
    public String updateUserRole(String email, String newRole) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (email == null || newRole == null) return error("Missing email or role.");

        String emailNorm = email.trim().toLowerCase();
        String roleNorm = newRole.trim().toUpperCase();

        List<User> users = loadList(usersFile(), User.class);
        for (User u : users) {
            if (u != null && u.email != null && u.email.trim().toLowerCase().equals(emailNorm)) {
                u.role = roleNorm;
                saveList(usersFile(), users);
                return ok("Role updated.", roleNorm);
            }
        }
        return error("User not found.");
    }

    @Override
    public String createTicket(String userId, String type, String details) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (userId == null) return error("Missing userId.");

        Ticket t = new Ticket();
        t.ticketId = generateTicketId();
        t.userId = userId;
        t.status = "PENDING";
        t.itemCategory = type;
        t.quantity = 1;
        t.details = details;
        t.pickupLocation = "";
        t.donationDrive = "";
        t.deliveryDestination = "";
        t.photoBase64 = "";

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        tickets.add(t);
        saveList(ticketsFile(), tickets);

        log("Created ticket " + t.ticketId + " for " + userId);
        return ok("Ticket created: " + t.ticketId);
    }

    @Override
    public String createTicket(String userId, String itemCategory, int quantity,
                                 String condition, String expirationDate,
                                 String pickupDateTime, String pickupLocation,
                                 String photoPath, String notes, String photoBase64) throws RemoteException {
        // Map to the extended ticket structure (no donation drive/delivery destination).
        return createTicket(userId,
                itemCategory,
                quantity,
                condition,
                expirationDate,
                pickupDateTime,
                pickupLocation,
                photoPath,
                notes,
                "", // donationDrive
                "", // deliveryDestination
                photoBase64);
    }

    @Override
    public String createTicket(String userId, String itemCategory, int quantity,
                                 String condition, String expirationDate,
                                 String pickupDateTime, String pickupLocation,
                                 String photoPath, String notes,
                                 String donationDrive, String deliveryDestination,
                                 String photoBase64) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (userId == null) return error("Missing userId.");

        Ticket t = new Ticket();
        t.ticketId = generateTicketId();
        t.userId = userId;
        t.status = "PENDING";
        t.riderId = "";
        t.itemCategory = itemCategory;
        t.quantity = quantity;
        t.condition = condition;
        t.expirationDate = expirationDate;
        t.pickupDateTime = pickupDateTime;
        t.pickupLocation = pickupLocation;
        t.photoPath = photoPath;
        t.details = notes;
        t.donationDrive = donationDrive;
        t.deliveryDestination = deliveryDestination;
        t.photoBase64 = photoBase64;

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        tickets.add(t);
        saveList(ticketsFile(), tickets);

        log("Created ticket " + t.ticketId + " (" + t.status + ") for donor=" + userId);
        return ok("Ticket created: " + t.ticketId);
    }

    @Override
    public String readTickets(String userId) throws RemoteException {
        return readTickets(userId, null);
    }

    @Override
    public String readTickets(String userId, String status) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        String userNorm = userId == null ? "" : userId.trim();
        boolean isRiderQuery = "rider".equalsIgnoreCase(userNorm);

        List<Ticket> filtered = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t == null) continue;

            if (status != null && (t.status == null || !t.status.equalsIgnoreCase(status))) {
                continue;
            }

            if (isRiderQuery) {
                // "rider" request means: show all tickets matching status (used by rider's drive details).
                filtered.add(t);
                continue;
            }

            if (userNorm == null || userNorm.trim().isEmpty()) {
                filtered.add(t);
                continue;
            }

            // Without explicit role info, return tickets where the user is either the donor or the assigned rider.
            boolean matchesDonor = t.userId != null && t.userId.equalsIgnoreCase(userNorm);
            boolean matchesRider = t.riderId != null && t.riderId.equalsIgnoreCase(userNorm);
            if (matchesDonor || matchesRider) {
                filtered.add(t);
            }
        }

        StringBuilder xml = new StringBuilder();
        for (Ticket t : filtered) {
            xml.append(ticketToXml(t));
        }
        return ok(xml.toString());
    }

    @Override
    public String updateTicket(String userId, String ticketId, String status) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (ticketId == null) return error("Missing ticketId.");

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        for (Ticket t : tickets) {
            if (t != null && t.ticketId != null && t.ticketId.equalsIgnoreCase(ticketId)) {
                t.status = status;
                if ("ACCEPTED".equalsIgnoreCase(status)) {
                    t.riderId = userId;
                } else if (t.riderId == null || t.riderId.trim().isEmpty()) {
                    // Keep existing riderId if present; otherwise assign.
                    t.riderId = userId;
                }

                saveList(ticketsFile(), tickets);
                return ok("Ticket updated to " + status);
            }
        }
        return error("Ticket not found.");
    }

    @Override
    public String updateTicket(String userId, String ticketId, String status,
                                 String qualityStatus, String qualityReason) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (ticketId == null) return error("Missing ticketId.");

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        for (Ticket t : tickets) {
            if (t != null && t.ticketId != null && t.ticketId.equalsIgnoreCase(ticketId)) {
                t.status = status;
                t.qualityStatus = qualityStatus;
                t.qualityReason = qualityReason;

                if ("ACCEPTED".equalsIgnoreCase(status)) {
                    t.riderId = userId;
                } else if (t.riderId == null || t.riderId.trim().isEmpty()) {
                    t.riderId = userId;
                }

                saveList(ticketsFile(), tickets);
                return ok("Ticket updated to " + status);
            }
        }
        return error("Ticket not found.");
    }

    @Override
    public String updateTicketPickupTime(String userId, String ticketId, String pickupDateTime) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (ticketId == null) return error("Missing ticketId.");

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        for (Ticket t : tickets) {
            if (t != null && t.ticketId != null && t.ticketId.equalsIgnoreCase(ticketId)) {
                t.pickupDateTime = pickupDateTime;
                saveList(ticketsFile(), tickets);
                return ok("Pickup time updated.");
            }
        }
        return error("Ticket not found.");
    }

    @Override
    public String deleteTicket(String userId, String ticketId) throws RemoteException {
        return deleteTicket(userId, ticketId, null);
    }

    @Override
    public String deleteTicket(String userId, String ticketId, String reason) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (ticketId == null) return error("Missing ticketId.");

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        boolean removed = false;

        List<Ticket> out = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t != null && t.ticketId != null && t.ticketId.equalsIgnoreCase(ticketId)) {
                removed = true;
                continue;
            }
            out.add(t);
        }

        if (removed) {
            saveList(ticketsFile(), out);
            return ok("Ticket deleted.");
        }
        return error("Ticket not found.");
    }

    @Override
    public String permanentDeleteTicket(String adminUserId, String ticketId) throws RemoteException {
        // Same behavior for now; UI does not differentiate.
        return deleteTicket(adminUserId, ticketId, "permanent");
    }

    @Override
    public String createDonationDrive(String userId, String title, String description, String targetAmount, String photoBase64) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (title == null) return error("Missing title.");

        double target = 0.0;
        try {
            if (targetAmount != null && !targetAmount.trim().isEmpty()) {
                target = Double.parseDouble(targetAmount.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        List<Drive> drives = loadList(drivesFile(), Drive.class);
        Drive existing = null;
        for (Drive d : drives) {
            if (d != null && d.title != null && d.title.equalsIgnoreCase(title.trim())) {
                existing = d;
                break;
            }
        }

        if (existing == null) {
            Drive d = new Drive();
            d.title = title.trim();
            d.description = description;
            d.targetAmount = target;
            d.currentAmount = 0.0;
            d.photoBase64 = photoBase64;
            drives.add(d);
        } else {
            existing.description = description;
            existing.targetAmount = target;
            existing.photoBase64 = photoBase64;
        }

        saveList(drivesFile(), drives);
        return ok("Donation drive created/updated.");
    }

    @Override
    public String readDonationDrives() throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");

        List<Drive> drives = loadList(drivesFile(), Drive.class);
        StringBuilder xml = new StringBuilder();
        for (Drive d : drives) {
            xml.append(driveToXml(d));
        }
        return ok(xml.toString());
    }

    @Override
    public String deleteDonationDrive(String userId, String driveTitle) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (driveTitle == null) return error("Missing driveTitle.");

        List<Drive> drives = loadList(drivesFile(), Drive.class);
        List<Drive> out = new ArrayList<>();
        boolean removed = false;

        for (Drive d : drives) {
            if (d != null && d.title != null && d.title.equalsIgnoreCase(driveTitle.trim())) {
                removed = true;
                continue;
            }
            out.add(d);
        }

        if (removed) {
            saveList(drivesFile(), out);
            return ok("Donation drive deleted.");
        }
        return error("Donation drive not found.");
    }

    @Override
    public String updateDriveAmount(String driveTitle, double amount) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (driveTitle == null) return error("Missing driveTitle.");

        String titleNorm = driveTitle.trim();
        List<Drive> drives = loadList(drivesFile(), Drive.class);

        Drive existing = null;
        for (Drive d : drives) {
            if (d != null && d.title != null && d.title.equalsIgnoreCase(titleNorm)) {
                existing = d;
                break;
            }
        }

        if (existing == null) {
            Drive d = new Drive();
            d.title = titleNorm;
            d.description = "";
            d.targetAmount = 0.0;
            d.currentAmount = amount;
            drives.add(d);
        } else {
            existing.currentAmount += amount;
        }

        saveList(drivesFile(), drives);
        return ok("Drive amount updated.");
    }

    @Override
    public String searchTickets(String keyword) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (keyword == null) keyword = "";
        String kw = keyword.trim().toLowerCase();

        List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
        List<Ticket> matched = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t == null) continue;
            if (containsIgnoreCase(t.ticketId, kw)
                    || containsIgnoreCase(t.userId, kw)
                    || containsIgnoreCase(t.riderId, kw)
                    || containsIgnoreCase(t.status, kw)
                    || containsIgnoreCase(t.itemCategory, kw)
                    || containsIgnoreCase(t.details, kw)
                    || containsIgnoreCase(t.donationDrive, kw)
                    || containsIgnoreCase(t.deliveryDestination, kw)
                    || containsIgnoreCase(t.pickupLocation, kw)) {
                matched.add(t);
            }
        }

        StringBuilder xml = new StringBuilder();
        for (Ticket t : matched) xml.append(ticketToXml(t));
        return ok(xml.toString());
    }

    @Override
    public String searchDonationDrives(String keyword) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (keyword == null) keyword = "";
        String kw = keyword.trim().toLowerCase();

        List<Drive> drives = loadList(drivesFile(), Drive.class);
        List<Drive> matched = new ArrayList<>();
        for (Drive d : drives) {
            if (d == null) continue;
            if (containsIgnoreCase(d.title, kw) || containsIgnoreCase(d.description, kw)) {
                matched.add(d);
            }
        }

        StringBuilder xml = new StringBuilder();
        for (Drive d : matched) xml.append(driveToXml(d));
        return ok(xml.toString());
    }

    @Override
    public String ping() throws RemoteException {
        return ok("PONG");
    }

    @Override
    public String setRiderAvailable(String userId) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (userId == null || userId.trim().isEmpty()) return error("Missing userId.");

        String rider = userId.trim();
        List<String> riders = loadList(activeRidersFile(), String.class);
        boolean exists = false;
        for (String r : riders) {
            if (r != null && r.equalsIgnoreCase(rider)) {
                exists = true;
                break;
            }
        }
        if (!exists) riders.add(rider);
        saveList(activeRidersFile(), riders);
        return ok("Rider set available.");
    }

    @Override
    public String setRiderUnavailable(String userId) throws RemoteException {
        if (isMaintenanceMode()) return error("Server is in maintenance mode.");
        if (userId == null || userId.trim().isEmpty()) return error("Missing userId.");

        String rider = userId.trim();
        List<String> riders = loadList(activeRidersFile(), String.class);
        List<String> out = new ArrayList<>();
        for (String r : riders) {
            if (r == null) continue;
            if (r.equalsIgnoreCase(rider)) continue;
            out.add(r);
        }
        saveList(activeRidersFile(), out);
        return ok("Rider set unavailable.");
    }

    @Override
    public String getServerLogs() throws RemoteException {
        List<String> logs = loadList(serverLogsFile(), String.class);
        StringBuilder sb = new StringBuilder();
        for (String line : logs) {
            sb.append(line).append("\n");
        }
        return ok(sb.toString().trim());
    }

    @Override
    public String setServerMaintenanceMode(boolean enabled) throws RemoteException {
        ServerSettings settings = loadServerSettings();
        settings.maintenanceEnabled = enabled;
        saveServerSettings(settings);
        return ok("Maintenance mode updated.");
    }

    private static boolean containsIgnoreCase(String v, String kwLower) {
        if (kwLower == null || kwLower.isEmpty()) return true;
        if (v == null) return false;
        return v.toLowerCase().contains(kwLower);
    }

    private static String generateTicketId() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Data models for future JSON request/response handling.
     * These are not yet used by the current stubbed RMI methods.
     */
    static class User {
        public String email;
        public String password;
        public String role;

        public User() {}

        public User(String email, String password, String role) {
            this.email = email;
            this.password = password;
            this.role = role;
        }

        public String toJson() {
            return "{"
                    + "\"email\":" + jsonString(email) + ","
                    + "\"password\":" + jsonString(password) + ","
                    + "\"role\":" + jsonString(role)
                    + "}";
        }
    }

    static class Ticket {
        public String ticketId;
        public String status;
        public String itemCategory;
        public int quantity;
        public String condition;
        public String expirationDate;
        public String pickupDateTime;
        public String pickupLocation;
        public String donationDrive;
        public String deliveryDestination;
        public String photoPath;
        public String details;
        public String photoBase64;

        // Rider-related
        public String riderId;

        // Donor-related
        public String userId;

        // Quality/verification (optional)
        public String qualityStatus;
        public String qualityReason;

        public Ticket() {}

        public String toJson() {
            return "{"
                    + "\"ticketId\":" + jsonString(ticketId) + ","
                    + "\"status\":" + jsonString(status) + ","
                    + "\"itemCategory\":" + jsonString(itemCategory) + ","
                    + "\"quantity\":" + quantity + ","
                    + "\"condition\":" + jsonString(condition) + ","
                    + "\"expirationDate\":" + jsonString(expirationDate) + ","
                    + "\"pickupDateTime\":" + jsonString(pickupDateTime) + ","
                    + "\"pickupLocation\":" + jsonString(pickupLocation) + ","
                    + "\"donationDrive\":" + jsonString(donationDrive) + ","
                    + "\"deliveryDestination\":" + jsonString(deliveryDestination) + ","
                    + "\"photoPath\":" + jsonString(photoPath) + ","
                    + "\"details\":" + jsonString(details) + ","
                    + "\"photoBase64\":" + jsonString(photoBase64) + ","
                    + "\"riderId\":" + jsonString(riderId) + ","
                    + "\"userId\":" + jsonString(userId) + ","
                    + "\"qualityStatus\":" + jsonString(qualityStatus) + ","
                    + "\"qualityReason\":" + jsonString(qualityReason)
                    + "}";
        }
    }

    static class Drive {
        public String title;
        public String description;
        public double targetAmount;
        public double currentAmount;
        public String photoBase64;

        public Drive() {}

        public Drive(String title, String description, double targetAmount, double currentAmount, String photoBase64) {
            this.title = title;
            this.description = description;
            this.targetAmount = targetAmount;
            this.currentAmount = currentAmount;
            this.photoBase64 = photoBase64;
        }

        public String toJson() {
            return "{"
                    + "\"title\":" + jsonString(title) + ","
                    + "\"description\":" + jsonString(description) + ","
                    + "\"targetAmount\":" + targetAmount + ","
                    + "\"currentAmount\":" + currentAmount + ","
                    + "\"photoBase64\":" + jsonString(photoBase64)
                    + "}";
        }
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        return "\"" + escapeJson(s) + "\"";
    }

    private static String escapeJson(String s) {
        // Minimal JSON escaping (enough for typical XML/GUI input).
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Server srv = new Server();
                srv.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
