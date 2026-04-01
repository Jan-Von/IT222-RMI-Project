import Network.DonationDriverService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server extends JFrame implements DonationDriverService {
    private static final int PORT = 5267;

    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;
    private Registry registry;

    private final Set<String> activeSessions = new HashSet<>();
    private volatile boolean maintenanceMode;

    // Gson init (optional at runtime; we also support a Gson-free fallback).
    private static final Object GSON = initGson();

    private static Object initGson() {
        try {
            Class<?> gsonBuilderCls = Class.forName("com.google.gson.GsonBuilder");
            Object builder = gsonBuilderCls.getConstructor().newInstance();
            try {
                gsonBuilderCls.getMethod("disableHtmlEscaping").invoke(builder);
            } catch (Exception ignored) {}
            return gsonBuilderCls.getMethod("create").invoke(builder);
        } catch (Throwable ignored) {
            return null;
        }
    }

    static class User {
        public String email;
        public String password;
        public String role;
    }

    static class Ticket {
        public String ticketId;
        public String userId; // donor
        public String riderId; // rider
        public String status;
        public String itemCategory;
        public int quantity;
        public String condition;
        public String expirationDate;
        public String pickupDateTime;
        public String pickupLocation;
        public String photoPath;
        public String details; // notes/details
        public String donationDrive;
        public String deliveryDestination;
        public String photoBase64;
        public String qualityStatus;
        public String qualityReason;
    }

    static class Drive {
        public String title;
        public String description;
        public double targetAmount;
        public double currentAmount;
        public String photoBase64;
    }

    private static final File DATA_DIR = resolveDataDir();

    private static File resolveDataDir() {
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File donationDriverDir = new File(dir, "DonationDriverUI");
            if (donationDriverDir.isDirectory()) {
                File dataDir = new File(donationDriverDir, "data");
                dataDir.mkdirs();
                return dataDir;
            }
        }
        File fallback = new File(cwd, "DonationDriverUI/data");
        fallback.mkdirs();
        return fallback;
    }

    private static File usersFile() { return new File(DATA_DIR, "users.json"); }
    private static File ticketsFile() { return new File(DATA_DIR, "tickets.json"); }
    private static File drivesFile() { return new File(DATA_DIR, "drives.json"); }

    private static File availableRidersFile() { return new File(DATA_DIR, "available_riders.json"); }

    private static File serverLogsFile() { return new File(DATA_DIR, "server_logs.json"); }
    private static File serverSettingsFile() { return new File(DATA_DIR, "server_settings.json"); }

    private void initDataFiles() {
        DATA_DIR.mkdirs();
        ensureEmptyArrayFile(usersFile());
        ensureEmptyArrayFile(new File(DATA_DIR, "tickets.json"));
        ensureEmptyArrayFile(drivesFile());
        ensureEmptyArrayFile(availableRidersFile());
        ensureEmptyArrayFile(new File(DATA_DIR, "server_logs.json"));
        ensureEmptyObjectFile(new File(DATA_DIR, "server_settings.json"), "{\"maintenanceEnabled\":false}");
    }

    private static void ensureEmptyArrayFile(File f) {
        try {
            if (!f.exists() || f.length() == 0) {
                File parent = f.getParentFile();
                if (parent != null) parent.mkdirs();
                Files.writeString(f.toPath(), "[]", StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
    }

    private static void ensureEmptyObjectFile(File f, String defaultJson) {
        try {
            if (!f.exists() || f.length() == 0) {
                File parent = f.getParentFile();
                if (parent != null) parent.mkdirs();
                Files.writeString(f.toPath(), defaultJson, StandardCharsets.UTF_8);
            }
        } catch (Exception ignored) {}
    }

    /* ===================== Generic load/save (JSON) ===================== */
    private static <T> List<T> loadList(File file, Class<T> elementClass) {
        if (file == null || !file.exists() || file.length() == 0) return new ArrayList<>();
        try {
            // Gson path (if gson exists on runtime classpath)
            if (GSON != null) {
                Object gson = GSON;
                @SuppressWarnings("unchecked")
                List<Object> rawList = (List<Object>) gson.getClass()
                        .getMethod("fromJson", String.class, Class.class)
                        .invoke(gson, Files.readString(file.toPath(), StandardCharsets.UTF_8), List.class);

                List<T> out = new ArrayList<>();
                for (Object rawEl : rawList) {
                    if (rawEl == null) continue;
                    String elJson = (String) gson.getClass().getMethod("toJson", Object.class).invoke(gson, rawEl);
                    @SuppressWarnings("unchecked")
                    T converted = (T) gson.getClass()
                            .getMethod("fromJson", String.class, Class.class)
                            .invoke(gson, elJson, elementClass);
                    out.add(converted);
                }
                return out;
            }

            // Gson-free fallback
            return loadListFallback(file, elementClass);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static <T> void saveList(File file, List<T> list) {
        if (file == null) return;
        if (list == null) list = new ArrayList<>();
        try {
            if (GSON != null) {
                String json = (String) GSON.getClass().getMethod("toJson", Object.class).invoke(GSON, list);
                File parent = file.getParentFile();
                if (parent != null) parent.mkdirs();
                Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
                return;
            }
            saveListFallback(file, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static <T> List<T> loadListFallback(File file, Class<T> elementClass) throws Exception {
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
        if (json.isEmpty() || "[]".equals(json)) return new ArrayList<>();

        if (elementClass == String.class) {
            return castStringList(parseJsonStringArray(json));
        }

        if (elementClass == User.class) {
            List<User> users = new ArrayList<>();
            for (String obj : parseJsonObjects(json)) {
                User u = new User();
                u.email = extractJsonStringField(obj, "email");
                u.password = extractJsonStringField(obj, "password");
                u.role = extractJsonStringField(obj, "role");
                users.add(u);
            }
            @SuppressWarnings("unchecked")
            List<T> out = (List<T>) (List<?>) users;
            return out;
        }
        if (elementClass == Ticket.class) {
            List<Ticket> tickets = castObjectListTickets(parseJsonObjects(json));
            @SuppressWarnings("unchecked")
            List<T> out = (List<T>) (List<?>) tickets;
            return out;
        }
        if (elementClass == Drive.class) {
            List<Drive> drives = castObjectListDrives(parseJsonObjects(json));
            @SuppressWarnings("unchecked")
            List<T> out = (List<T>) (List<?>) drives;
            return out;
        }

        return new ArrayList<>();
    }

    private static <T> List<T> castStringList(List<String> in) {
        @SuppressWarnings("unchecked")
        List<T> out = (List<T>) (List<?>) in;
        return out;
    }

    private static List<Ticket> castObjectListTickets(List<String> jsonObjects) throws Exception {
        List<Ticket> out = new ArrayList<>();
        for (String obj : jsonObjects) {
            Ticket t = new Ticket();
            t.ticketId = extractJsonStringField(obj, "ticketId");
            t.userId = extractJsonStringField(obj, "userId");
            t.riderId = extractJsonStringField(obj, "riderId");
            t.status = extractJsonStringField(obj, "status");
            t.itemCategory = extractJsonStringField(obj, "itemCategory");
            t.quantity = extractJsonIntField(obj, "quantity", 0);
            t.condition = extractJsonStringField(obj, "condition");
            t.expirationDate = extractJsonStringField(obj, "expirationDate");
            t.pickupDateTime = extractJsonStringField(obj, "pickupDateTime");
            t.pickupLocation = extractJsonStringField(obj, "pickupLocation");
            t.photoPath = extractJsonStringField(obj, "photoPath");
            t.details = extractJsonStringField(obj, "details");
            t.donationDrive = extractJsonStringField(obj, "donationDrive");
            t.deliveryDestination = extractJsonStringField(obj, "deliveryDestination");
            t.photoBase64 = extractJsonStringField(obj, "photoBase64");
            t.qualityStatus = extractJsonStringField(obj, "qualityStatus");
            t.qualityReason = extractJsonStringField(obj, "qualityReason");
            out.add(t);
        }
        return out;
    }

    private static List<Drive> castObjectListDrives(List<String> jsonObjects) throws Exception {
        List<Drive> out = new ArrayList<>();
        for (String obj : jsonObjects) {
            Drive d = new Drive();
            d.title = extractJsonStringField(obj, "title");
            d.description = extractJsonStringField(obj, "description");
            d.targetAmount = extractJsonDoubleField(obj, "targetAmount", 0.0);
            d.currentAmount = extractJsonDoubleField(obj, "currentAmount", 0.0);
            d.photoBase64 = extractJsonStringField(obj, "photoBase64");
            out.add(d);
        }
        return out;
    }

    private static <T> void saveListFallback(File file, List<T> list) throws Exception {
        String name = file.getName().toLowerCase();

        String json;
        if ("users.json".equals(name)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (T el : list) {
                if (!(el instanceof User)) continue;
                if (!first) sb.append(",");
                first = false;
                User u = (User) el;
                sb.append("{")
                        .append("\"email\":\"").append(escapeJsonString(u.email)).append("\",")
                        .append("\"password\":\"").append(escapeJsonString(u.password)).append("\",")
                        .append("\"role\":\"").append(escapeJsonString(u.role)).append("\"")
                        .append("}");
            }
            sb.append("]");
            json = sb.toString();
        } else if ("tickets.json".equals(name)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (T el : list) {
                if (!(el instanceof Ticket)) continue;
                if (!first) sb.append(",");
                first = false;
                Ticket t = (Ticket) el;
                sb.append("{")
                        .append("\"ticketId\":\"").append(escapeJsonString(t.ticketId)).append("\",")
                        .append("\"userId\":\"").append(escapeJsonString(t.userId)).append("\",")
                        .append("\"riderId\":\"").append(escapeJsonString(t.riderId)).append("\",")
                        .append("\"status\":\"").append(escapeJsonString(t.status)).append("\",")
                        .append("\"itemCategory\":\"").append(escapeJsonString(t.itemCategory)).append("\",")
                        .append("\"quantity\":").append(t.quantity).append(",")
                        .append("\"condition\":\"").append(escapeJsonString(t.condition)).append("\",")
                        .append("\"expirationDate\":\"").append(escapeJsonString(t.expirationDate)).append("\",")
                        .append("\"pickupDateTime\":\"").append(escapeJsonString(t.pickupDateTime)).append("\",")
                        .append("\"pickupLocation\":\"").append(escapeJsonString(t.pickupLocation)).append("\",")
                        .append("\"photoPath\":\"").append(escapeJsonString(t.photoPath)).append("\",")
                        .append("\"details\":\"").append(escapeJsonString(t.details)).append("\",")
                        .append("\"donationDrive\":\"").append(escapeJsonString(t.donationDrive)).append("\",")
                        .append("\"deliveryDestination\":\"").append(escapeJsonString(t.deliveryDestination)).append("\",")
                        .append("\"photoBase64\":\"").append(escapeJsonString(t.photoBase64)).append("\",")
                        .append("\"qualityStatus\":\"").append(escapeJsonString(t.qualityStatus)).append("\",")
                        .append("\"qualityReason\":\"").append(escapeJsonString(t.qualityReason)).append("\"")
                        .append("}");
            }
            sb.append("]");
            json = sb.toString();
        } else if ("drives.json".equals(name)) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (T el : list) {
                if (!(el instanceof Drive)) continue;
                if (!first) sb.append(",");
                first = false;
                Drive d = (Drive) el;
                sb.append("{")
                        .append("\"title\":\"").append(escapeJsonString(d.title)).append("\",")
                        .append("\"description\":\"").append(escapeJsonString(d.description)).append("\",")
                        .append("\"targetAmount\":").append(d.targetAmount).append(",")
                        .append("\"currentAmount\":").append(d.currentAmount).append(",")
                        .append("\"photoBase64\":\"").append(escapeJsonString(d.photoBase64)).append("\"")
                        .append("}");
            }
            sb.append("]");
            json = sb.toString();
        } else {
            // available_riders.json, server_logs.json => array of strings
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (T el : list) {
                if (!(el instanceof String)) continue;
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJsonString(String.valueOf(el))).append("\"");
            }
            sb.append("]");
            json = sb.toString();
        }

        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();
        Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
    }

    private static List<String> parseJsonObjects(String jsonArray) {
        // Extract top-level {...} objects from a JSON array string.
        List<String> out = new ArrayList<>();
        int idx = jsonArray.indexOf('{');
        while (idx >= 0) {
            int depth = 0;
            int start = idx;
            for (int i = idx; i < jsonArray.length(); i++) {
                char c = jsonArray.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        out.add(jsonArray.substring(start, i + 1));
                        idx = jsonArray.indexOf('{', i + 1);
                        break;
                    }
                }
            }
        }
        return out;
    }

    private static List<String> parseJsonStringArray(String jsonArray) {
        List<String> out = new ArrayList<>();
        // Matches "...." strings, handling escapes.
        Matcher m = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(jsonArray);
        while (m.find()) {
            out.add(unescapeJsonString(m.group(1)));
        }
        return out;
    }

    private static int extractJsonIntField(String obj, String field, int def) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(-?\\d+)");
        Matcher m = p.matcher(obj);
        if (!m.find()) return def;
        try {
            return Integer.parseInt(m.group(1));
        } catch (Exception ignored) {
            return def;
        }
    }

    private static double extractJsonDoubleField(String obj, String field, double def) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        Matcher m = p.matcher(obj);
        if (!m.find()) return def;
        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception ignored) {
            return def;
        }
    }

    private static <T> List<T> loadUsersOrDrivesFallback(File file, Class<T> elementClass) {
        // We only need Users for registration/login in this minimal fix.
        if (file == null || !file.exists()) return new ArrayList<>();
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            if (json.isEmpty() || "[]".equals(json)) return new ArrayList<>();

            if (elementClass != User.class) {
                return new ArrayList<>();
            }

            // Extract objects: { ... } inside a top-level array
            List<String> objects = extractJsonObjects(json);
            List<T> out = new ArrayList<>();
            for (String obj : objects) {
                User u = new User();
                u.email = extractJsonStringField(obj, "email");
                u.password = extractJsonStringField(obj, "password");
                u.role = extractJsonStringField(obj, "role");
                @SuppressWarnings("unchecked")
                T cast = (T) u;
                out.add(cast);
            }
            return out;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static void saveUsersFallback(File file, List<User> users) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            boolean first = true;
            for (User u : users) {
                if (u == null) continue;
                if (!first) sb.append(",");
                first = false;
                sb.append("{")
                        .append("\"email\":\"").append(escapeJsonString(u.email)).append("\",")
                        .append("\"password\":\"").append(escapeJsonString(u.password)).append("\",")
                        .append("\"role\":\"").append(escapeJsonString(u.role)).append("\"")
                        .append("}");
            }
            sb.append("]");
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            Files.writeString(file.toPath(), sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    private static List<User> loadUsers() {
        return loadList(usersFile(), User.class);
    }

    private static String xmlResponse(String status, String message, String role) {
        return "<response>"
                + "<status>" + escapeXml(status) + "</status>"
                + "<message>" + escapeXml(message == null ? "" : message) + "</message>"
                + "<role>" + escapeXml(role == null ? "" : role) + "</role>"
                + "</response>";
    }

    private static String ok(String message, String role) {
        return xmlResponse("OK", message, role);
    }

    private static String error(String message) {
        return xmlResponse("ERROR", message, "");
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static String escapeJsonString(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static String unescapeJsonString(String s) {
        if (s == null) return "";
        // Not perfect, but good enough for our stored strings.
        return s.replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static List<String> extractJsonObjects(String jsonArray) {
        List<String> out = new ArrayList<>();
        int idx = jsonArray.indexOf('{');
        while (idx >= 0) {
            int depth = 0;
            int start = idx;
            for (int i = idx; i < jsonArray.length(); i++) {
                char c = jsonArray.charAt(i);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        out.add(jsonArray.substring(start, i + 1));
                        idx = jsonArray.indexOf('{', i + 1);
                        break;
                    }
                }
            }
        }
        return out;
    }

    private static String extractJsonStringField(String obj, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(obj);
        if (!m.find()) return "";
        return unescapeJsonString(m.group(1));
    }

    private static String driveToXml(String title, String description) {
        return "<drive>"
                + "<title>" + escapeXml(title) + "</title>"
                + "<description>" + escapeXml(description == null ? "" : description) + "</description>"
                + "</drive>";
    }

    private static boolean isMaintenanceEnabled() {
        try {
            File f = serverSettingsFile();
            if (f == null || !f.exists() || f.length() == 0) return false;
            String json = Files.readString(f.toPath(), StandardCharsets.UTF_8);
            if (json == null) return false;
            String compact = json.replaceAll("\\s+", "");
            return compact.toLowerCase().contains("\"maintenanceenabled\":true");
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void appendServerLog(String logLine) {
        if (logLine == null) return;
        try {
            List<String> logs = loadList(serverLogsFile(), String.class);
            logs.add(logLine);
            saveList(serverLogsFile(), logs);
        } catch (Exception ignored) {
        }
    }

    private static String generateTicketId() {
        long now = System.currentTimeMillis();
        int r = (int) (Math.random() * 1_000_000);
        return String.valueOf(now) + "_" + r;
    }

    private static boolean containsIgnoreCase(String v, String kwLower) {
        if (kwLower == null || kwLower.isEmpty()) return true;
        if (v == null) return false;
        return v.toLowerCase().contains(kwLower);
    }

    private static String getUserRole(String email) {
        if (email == null) return "DONOR";
        String e = email.trim().toLowerCase();
        try {
            List<User> users = loadList(usersFile(), User.class);
            for (User u : users) {
                if (u == null || u.email == null) continue;
                if (u.email.trim().toLowerCase().equals(e)) {
                    return u.role == null ? "DONOR" : u.role;
                }
            }
        } catch (Exception ignored) {}
        return "DONOR";
    }

    private static String ticketToXml(Ticket t) {
        if (t == null) return "";
        String qStatus = t.qualityStatus == null ? "" : t.qualityStatus;
        String qReason = t.qualityReason == null ? "" : t.qualityReason;

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
                + "<photoBase64>" + safe(t.photoBase64) + "</photoBase64>"
                + "<qualityStatus>" + safe(qStatus) + "</qualityStatus>"
                + "<qualityReason>" + safe(qReason) + "</qualityReason>"
                + "</ticket>";
    }

    private static String driveToXml(Drive d) {
        if (d == null) return "";
        return "<drive>"
                + "<title>" + safe(d.title) + "</title>"
                + "<description>" + safe(d.description) + "</description>"
                + "<targetAmount>" + d.targetAmount + "</targetAmount>"
                + "<currentAmount>" + d.currentAmount + "</currentAmount>"
                + "<photoBase64>" + safe(d.photoBase64) + "</photoBase64>"
                + "</drive>";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
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

    private static final DateTimeFormatter LOG_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void log(String type, String userId, String message) {
        String timestamp = LocalDateTime.now().format(LOG_FMT);
        String line = "[" + timestamp + "] [" + (type == null ? "INFO" : type.toUpperCase()) + "] "+ "user=" + (userId == null || userId.isBlank() ? "SYSTEM" : userId)+ " | " + (message == null ? "" : message);

        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));

        File logFile = new File(DATA_DIR, "server_log.txt");
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (Exception ignored) {}
    }

    private void startServer() {
        try {
            initDataFiles();
            maintenanceMode = isMaintenanceEnabled();

            registry = LocateRegistry.createRegistry(PORT);
            UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("rmi://localhost:" + PORT + "/DonationDriverService", this);
            log("INFO", "SYSTEM", "RMI Server started and bound on port " + PORT);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        } catch (Exception e) {
            log("ERROR", "SYSTEM", "Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        try {
            Naming.unbind("rmi://localhost:" + PORT + "/DonationDriverService");
            UnicastRemoteObject.unexportObject(this, true);
            if (registry != null) UnicastRemoteObject.unexportObject(registry, true);
            log("INFO", "SYSTEM", "RMI Server stopped.");
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        } catch (Exception e) {
            log("ERROR", "SYSTEM", "Error stopping server: " + e.getMessage());
        }
    }

    @Override
    public String login(String email, String password) throws RemoteException {
        if (email == null || password == null) return error("Missing email or password.");
        maintenanceMode = isMaintenanceEnabled();
        if (maintenanceMode) return error("Server is in maintenance mode.");
        String emailNorm = email.trim().toLowerCase();

        List<User> users = loadUsers();
        for (User u : users) {
            if (u == null || u.email == null) continue;
            if (u.email.trim().toLowerCase().equals(emailNorm) && password.equals(u.password)) {
                String role = u.role == null ? "DONOR" : u.role;
                synchronized (activeSessions) {
                    if (activeSessions.contains(emailNorm)) {
                        log("AUTH", emailNorm, "Login failed: user already logged in.");
                        return error("User is already logged in.");
                    }
                    activeSessions.add(emailNorm);
                }
                log("AUTH", emailNorm, "Login successful. Role=" + role);
                return ok("Login Success!", role);
            }
        }
        log("AUTH", emailNorm, "Login failed: invalid credentials.");
        return error("Invalid email or password!");
    }

    @Override
    public String register(String email, String password) throws RemoteException {
        return register("", "", "", "", "", "", email, password, "DONOR");
    }

    @Override
    public String register(String firstName, String lastName, String middleName, String dateOfBirth,
                               String address, String phone, String email, String password, String role) throws RemoteException {
        if (email == null || password == null) return error("Missing email or password.");
        maintenanceMode = isMaintenanceEnabled();
        if (maintenanceMode) return error("Server is in maintenance mode.");

        String emailNorm = email.trim().toLowerCase();
        String roleNorm = (role == null || role.trim().isEmpty()) ? "DONOR" : role.trim().toUpperCase();

        List<User> users = loadUsers();
        for (User u : users) {
            if (u == null || u.email == null) continue;
            if (u.email.trim().toLowerCase().equals(emailNorm)) {
                return error("Email is already registered.");
            }
        }

        User nu = new User();
        nu.email = emailNorm;
        nu.password = password;
        nu.role = roleNorm;
        users.add(nu);
        saveList(usersFile(), users);

        log("AUTH", emailNorm, "Registered successfully. Role=" + roleNorm);
        return ok("Registration successful. You can now log in.", roleNorm);
    }

    @Override
    public String logout(String email) throws RemoteException {
        if (email != null) {
            synchronized (activeSessions) {
                activeSessions.remove(email.trim().toLowerCase());
            }
        }
        log("AUTH", email.trim().toLowerCase(), "Logged out.");
        return ok("Logged out.", "");
    }

    @Override
    public String updateUserRole(String email, String newRole) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (email == null || newRole == null) return error("Missing email or role.");

            String emailNorm = email.trim().toLowerCase();
            String roleNorm = newRole.trim().toUpperCase();

            List<User> users = loadList(usersFile(), User.class);
            for (User u : users) {
                if (u != null && u.email != null && u.email.trim().toLowerCase().equals(emailNorm)) {
                    u.role = roleNorm;
                    saveList(usersFile(), users);
                    log("CRUD", emailNorm, "Role updated to " + roleNorm);
                    return ok("Role updated.", roleNorm);
                }
            }
            log("CRUD", emailNorm, "updateUserRole failed: user not found.");
            return error("User not found.");
        } catch (Exception e) {
            return error("updateUserRole failed: " + e.getMessage());
        }
    }

    @Override
    public String createTicket(String userId, String type, String details) throws RemoteException {
        // Minimal mapping for older overload: treat `type` as itemCategory.
        return createTicket(userId, type, 1, "N/A", "", "", "", "", details, "", "", "");
    }

    @Override
    public String createTicket(String userId, String itemCategory, int quantity, String condition,
                                 String expirationDate, String pickupDateTime, String pickupLocation,
                                 String photoPath, String notes, String photoBase64) throws RemoteException {
        return createTicket(userId, itemCategory, quantity, condition,
                expirationDate, pickupDateTime, pickupLocation, photoPath, notes,
                "", "", photoBase64);
    }

    @Override
    public String createTicket(String userId, String itemCategory, int quantity, String condition,
                                 String expirationDate, String pickupDateTime, String pickupLocation,
                                 String photoPath, String notes, String donationDrive,
                                 String deliveryDestination, String photoBase64) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (userId == null) return error("Missing userId.");

            Ticket t = new Ticket();
            t.ticketId = generateTicketId();
            t.userId = userId.trim();
            t.riderId = "";
            t.status = "PENDING";
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
            t.qualityStatus = "";
            t.qualityReason = "";

            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
            tickets.add(t);
            saveList(ticketsFile(), tickets);

            appendServerLog("CREATE_TICKET | " + t.ticketId + " by " + t.userId);
            log("CRUD", t.userId, "createTicket | ticketId=" + t.ticketId + " category=" + itemCategory);
            return ok("Ticket created.", "");
        } catch (Exception e) {
            return error("createTicket failed: " + e.getMessage());
        }
    }

    @Override
    public String readTickets(String userId) throws RemoteException {
        return readTickets(userId, null);
    }

    @Override
    public String readTickets(String userId, String status) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");

            String userNorm = (userId == null) ? "" : userId.trim();
            boolean riderQuery = "rider".equalsIgnoreCase(userNorm);

            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
            List<Ticket> filtered = new ArrayList<>();

            String userRole = null;
            if (!riderQuery && !userNorm.isEmpty()) {
                userRole = getUserRole(userNorm);
            }

            for (Ticket t : tickets) {
                if (t == null) continue;
                if (status != null && (t.status == null || !t.status.equalsIgnoreCase(status))) continue;

                if (riderQuery) {
                    filtered.add(t);
                    continue;
                }

                if (userNorm.isEmpty()) {
                    filtered.add(t);
                    continue;
                }

                if ("RIDER".equalsIgnoreCase(userRole)) {
                    if (t.riderId != null && t.riderId.equalsIgnoreCase(userNorm)) {
                        filtered.add(t);
                    }
                } else {
                    if (t.userId != null && t.userId.equalsIgnoreCase(userNorm)) {
                        filtered.add(t);
                    }
                }
            }

            StringBuilder xml = new StringBuilder();
            for (Ticket t : filtered) xml.append(ticketToXml(t));
             log("CRUD", userId == null ? "SYSTEM" : userId, "readTickets | status=" + status + " count=" + filtered.size());
            return ok(xml.toString(), "");
        } catch (Exception e) {
            return error("readTickets failed: " + e.getMessage());
        }
    }

    @Override
    public String updateTicket(String userId, String ticketId, String status) throws RemoteException {
        return updateTicket(userId, ticketId, status, null, null);
    }

    @Override
    public String updateTicket(String userId, String ticketId, String status, String qualityStatus, String qualityReason) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (ticketId == null) return error("Missing ticketId.");

            String riderEmail = userId == null ? "" : userId.trim();
            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);

            for (Ticket t : tickets) {
                if (t == null || t.ticketId == null) continue;
                if (!t.ticketId.equalsIgnoreCase(ticketId)) continue;

                t.status = status;
                if ("ACCEPTED".equalsIgnoreCase(status)) {
                    t.riderId = riderEmail;
                } else {
                    // Keep riderId for visibility on rider side.
                    if (t.riderId == null || t.riderId.trim().isEmpty()) {
                        t.riderId = riderEmail;
                    }
                }

                if (qualityStatus != null) t.qualityStatus = qualityStatus;
                if (qualityReason != null) t.qualityReason = qualityReason;

                saveList(ticketsFile(), tickets);
                appendServerLog("UPDATE_TICKET | " + t.ticketId + " -> " + status);
                log("CRUD", userId == null ? "SYSTEM" : userId, "updateTicket | ticketId=" + ticketId + " status=" + status);
                return ok("Ticket updated.", "");
            }
            return error("Ticket not found.");
        } catch (Exception e) {
            return error("updateTicket failed: " + e.getMessage());
        }
    }

    @Override
    public String updateTicketPickupTime(String userId, String ticketId, String pickupDateTime) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (ticketId == null) return error("Missing ticketId.");

            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
            for (Ticket t : tickets) {
                if (t == null || t.ticketId == null) continue;
                if (!t.ticketId.equalsIgnoreCase(ticketId)) continue;

                t.pickupDateTime = pickupDateTime;
                saveList(ticketsFile(), tickets);
                appendServerLog("UPDATE_PICKUP_TIME | " + t.ticketId);
                log("CRUD", userId == null ? "SYSTEM" : userId, "updateTicketPickupTime | ticketId=" + ticketId + " time=" + pickupDateTime);
                return ok("Pickup time updated.", "");
            }
            return error("Ticket not found.");
        } catch (Exception e) {
            return error("updateTicketPickupTime failed: " + e.getMessage());
        }
    }

    @Override
    public String deleteTicket(String userId, String ticketId) throws RemoteException {
        return deleteTicket(userId, ticketId, null);
    }

    @Override
    public String deleteTicket(String userId, String ticketId, String reason) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (ticketId == null) return error("Missing ticketId.");

            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
            List<Ticket> out = new ArrayList<>();
            boolean removed = false;
            for (Ticket t : tickets) {
                if (t != null && t.ticketId != null && t.ticketId.equalsIgnoreCase(ticketId)) {
                    removed = true;
                } else {
                    out.add(t);
                }
            }
            if (!removed) return error("Ticket not found.");

            saveList(ticketsFile(), out);
            appendServerLog("DELETE_TICKET | " + ticketId + " reason=" + (reason == null ? "" : reason));
            log("CRUD", userId == null ? "SYSTEM" : userId, "deleteTicket | ticketId=" + ticketId + " reason=" + (reason == null ? "" : reason));
            return ok("Ticket deleted.", "");
        } catch (Exception e) {
            return error("deleteTicket failed: " + e.getMessage());
        }
    }

    @Override
    public String permanentDeleteTicket(String adminUserId, String ticketId) throws RemoteException {
        return deleteTicket(adminUserId, ticketId, "permanent");
    }

    @Override
    public String createDonationDrive(String userId, String title, String description, String targetAmount, String photoBase64) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (title == null) return error("Missing title.");

            double target = 0.0;
            try {
                if (targetAmount != null && !targetAmount.trim().isEmpty()) {
                    target = Double.parseDouble(targetAmount.trim());
                }
            } catch (NumberFormatException ignored) {}

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
            appendServerLog("CREATE_DRIVE | " + title.trim());
            log("CRUD", userId == null ? "SYSTEM" : userId, "createDonationDrive | title=" + title.trim());
            return ok("Donation drive created.", "");
        } catch (Exception e) {
            return error("createDonationDrive failed: " + e.getMessage());
        }
    }

    @Override
    public String readDonationDrives() throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");

            List<Drive> drives = loadList(drivesFile(), Drive.class);
            StringBuilder xml = new StringBuilder();
            for (Drive d : drives) xml.append(driveToXml(d));
            log("CRUD", "SYSTEM", "readDonationDrives | count=" + drives.size());
            return ok(xml.toString(), "");
        } catch (Exception e) {
            return error("readDonationDrives failed: " + e.getMessage());
        }
    }

    @Override
    public String deleteDonationDrive(String userId, String driveTitle) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (driveTitle == null) return error("Missing driveTitle.");

            List<Drive> drives = loadList(drivesFile(), Drive.class);
            List<Drive> out = new ArrayList<>();
            boolean removed = false;
            for (Drive d : drives) {
                if (d != null && d.title != null && d.title.equalsIgnoreCase(driveTitle.trim())) {
                    removed = true;
                } else {
                    out.add(d);
                }
            }
            if (!removed) return error("Donation drive not found.");

            saveList(drivesFile(), out);
            appendServerLog("DELETE_DRIVE | " + driveTitle);
            log("CRUD", userId == null ? "SYSTEM" : userId, "deleteDonationDrive | title=" + driveTitle);
            return ok("Donation drive deleted.", "");
        } catch (Exception e) {
            return error("deleteDonationDrive failed: " + e.getMessage());
        }
    }

    @Override
    public String updateDriveAmount(String driveTitle, double amount) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
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
                d.photoBase64 = "";
                drives.add(d);
            } else {
                existing.currentAmount += amount;
            }

            saveList(drivesFile(), drives);
            appendServerLog("UPDATE_DRIVE_AMOUNT | " + titleNorm + " +" + amount);
            log("CRUD", "SYSTEM", "updateDriveAmount | title=" + titleNorm + " amount=" + amount);
            return ok("Drive amount updated.", "");
        } catch (Exception e) {
            return error("updateDriveAmount failed: " + e.getMessage());
        }
    }

    @Override
    public String searchTickets(String keyword) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            String kw = keyword == null ? "" : keyword.trim().toLowerCase();

            List<Ticket> tickets = loadList(ticketsFile(), Ticket.class);
            StringBuilder xml = new StringBuilder();
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
                    xml.append(ticketToXml(t));
                }
            }
            log("CRUD", "SYSTEM", "searchTickets | keyword=" + keyword);
            return ok(xml.toString(), "");
        } catch (Exception e) {
            return error("searchTickets failed: " + e.getMessage());
        }
    }

    @Override
    public String searchDonationDrives(String keyword) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            String kw = keyword == null ? "" : keyword.trim().toLowerCase();

            List<Drive> drives = loadList(drivesFile(), Drive.class);
            StringBuilder xml = new StringBuilder();
            for (Drive d : drives) {
                if (d == null) continue;
                if (containsIgnoreCase(d.title, kw) || containsIgnoreCase(d.description, kw)) {
                    xml.append(driveToXml(d));
                }
            }
            log("CRUD", "SYSTEM", "searchDonationDrives | keyword=" + keyword);
            return ok(xml.toString(), "");
        } catch (Exception e) {
            return error("searchDonationDrives failed: " + e.getMessage());
        }
    }

    @Override
    public String ping() throws RemoteException {
        return ok("PONG", "");
    }

    @Override
    public String setRiderAvailable(String userId) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (userId == null || userId.trim().isEmpty()) return error("Missing userId.");

            List<String> riders = loadList(availableRidersFile(), String.class);
            String rider = userId.trim();
            boolean exists = false;
            for (String r : riders) {
                if (r != null && r.equalsIgnoreCase(rider)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) riders.add(rider);
            saveList(availableRidersFile(), riders);
            appendServerLog("RIDER_SET_AVAILABLE | " + rider);
            log("CRUD", rider, "setRiderAvailable");
            return ok("Rider set available.", "");
        } catch (Exception e) {
            return error("setRiderAvailable failed: " + e.getMessage());
        }
    }

    @Override
    public String setRiderUnavailable(String userId) throws RemoteException {
        try {
            if (isMaintenanceEnabled()) return error("Server is in maintenance mode.");
            if (userId == null || userId.trim().isEmpty()) return error("Missing userId.");

            String rider = userId.trim();
            List<String> riders = loadList(availableRidersFile(), String.class);
            List<String> out = new ArrayList<>();
            for (String r : riders) {
                if (r != null && r.equalsIgnoreCase(rider)) continue;
                out.add(r);
            }
            saveList(availableRidersFile(), out);
            appendServerLog("RIDER_SET_UNAVAILABLE | " + rider);
            log("CRUD", rider, "setRiderUnavailable");
            return ok("Rider set unavailable.", "");
        } catch (Exception e) {
            return error("setRiderUnavailable failed: " + e.getMessage());
        }
    }

    @Override
    public String getServerLogs() throws RemoteException {
        try {
            List<String> logs = loadList(serverLogsFile(), String.class);
            StringBuilder sb = new StringBuilder();
            for (String line : logs) {
                if (line == null) continue;
                sb.append(line).append("\n");
            }
            log("INFO", "SYSTEM", "getServerLogs | entries=" + logs.size());
            return ok(sb.toString().trim(), "");
        } catch (Exception e) {
            return error("getServerLogs failed: " + e.getMessage());
        }
    }

    @Override
    public String setServerMaintenanceMode(boolean enabled) throws RemoteException {
        try {
            DATA_DIR.mkdirs();
            String json = "{\"maintenanceEnabled\":" + (enabled ? "true" : "false") + "}";
            Files.writeString(new File(DATA_DIR, "server_settings.json").toPath(), json, StandardCharsets.UTF_8);
            maintenanceMode = enabled;
            appendServerLog("MAINTENANCE_MODE | enabled=" + enabled);
            log("MAINTENANCE", "SYSTEM", "setServerMaintenanceMode | enabled=" + enabled);
            return ok("Maintenance mode updated.", "");
        } catch (Exception e) {
            return error("setServerMaintenanceMode failed: " + e.getMessage());
        }
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
