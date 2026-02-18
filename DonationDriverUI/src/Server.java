import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;

public class Server {

    private static final int PORT = 5267;
    private static final String LOG_FILE = "server_log.txt";
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String USERS_XML_RELATIVE = "DonationDriverUI/users.xml";

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        System.out.println("Server started on port " + PORT + "...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String clientInfo = socket.getInetAddress() + ":" + socket.getPort();
            System.out.println("Client connected: " + clientInfo);

            try (
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    BufferedWriter out = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    String requestXml = line.trim();
                    if (requestXml.isEmpty()) {
                        continue;
                    }
                    String responseXml = handleRequest(requestXml);
                    out.write(responseXml);
                    out.newLine();
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + clientInfo);
            } finally {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private String handleRequest(String requestXml) {
            String action = extractTagValue(requestXml, "action");
            String userId = extractTagValue(requestXml, "userId");

            String status = "ERROR";
            String message;
            String dataAffected = "";

            if (action == null) {
                message = "Missing <action> in request.";
            } else {
                switch (action) {
                    case "LOGIN": {
                        String email = extractTagValue(requestXml, "email");
                        String password = extractTagValue(requestXml, "password");
                        if (email == null || password == null) {
                            message = "Missing email or password.";
                        } else if (authenticateUser(email.trim(), password.trim())) {
                            status = "OK";
                            message = "Login successful.";
                            userId = email.trim();
                            // Include role in dataAffected or separate variable to be put in response
                            String role = getUserRole(userId);
                            dataAffected = "user session: " + email.trim() + " (" + role + ")";

                            // We need to inject <role> into the response XML constructed at the end of
                            // handleRequest.
                            // However, the construction at line 266 is generic.
                            // Easier approach: append it to message or modify the response construction
                            // below.
                            // Let's modify the response construction to include extra fields if available.
                            // But `handleRequest` constructs the XML manually.
                            // Let's use a temporary hack or better, update the return statement.
                            // Actually, let's just append the role tag to the message for now? No, CLIENT
                            // needs to parse it.
                            // Best way: append it to a member variable or return a structured object.
                            // Given the structure, I will append the role tag manually in the return
                            // statement if userId is present.
                            // Wait, handleRequest returns a String.
                            // I will store the role in a local variable `userRole` and use it at the end.
                        } else {
                            message = "Invalid email or password.";
                            dataAffected = "login attempt failed for: " + (email != null ? email : "(unknown)");
                        }
                        break;
                    }
                    case "REGISTER": {
                        String email = extractTagValue(requestXml, "email");
                        String password = extractTagValue(requestXml, "password");
                        String firstName = extractTagValue(requestXml, "firstName");
                        String lastName = extractTagValue(requestXml, "lastName");
                        String middleName = extractTagValue(requestXml, "middleName");
                        String dateOfBirth = extractTagValue(requestXml, "dateOfBirth");
                        String address = extractTagValue(requestXml, "address");
                        String phone = extractTagValue(requestXml, "phone");
                        String role = extractTagValue(requestXml, "role");
                        if (role == null || role.isEmpty())
                            role = "DONOR"; // Default

                        if (email == null || password == null) {
                            message = "Missing email or password.";
                            dataAffected = "registration attempt (missing fields)";
                        } else if (userEmailExists(email.trim())) {
                            message = "Registration failed: email already exists.";
                            dataAffected = "email already exists: " + email.trim();
                        } else if (saveUserToXmlSingleFile(email.trim(), password.trim(),
                                firstName, lastName, middleName, dateOfBirth, address, phone, role)) {
                            status = "OK";
                            message = "Registration successful.";
                            dataAffected = "new user registered: " + email.trim() + " as " + role;
                        } else {
                            message = "Registration failed due to server error.";
                            dataAffected = "registration failed for: " + email.trim();
                        }
                        break;
                    }
                    case "CREATE_TICKET": {
                        OperationResult result = createTicket(userId, requestXml);
                        if (!result.success && "NO_RIDERS_AVAILABLE".equals(result.message)) {
                            status = "NO_RIDERS";
                            message = "No available riders at the moment. Please wait or try again later.";
                            dataAffected = "ticket not created - no riders available";
                        } else if (result.success) {
                            status = "OK";
                            dataAffected = result.message;
                            message = result.message;
                        } else {
                            dataAffected = "create failed: " + result.message;
                            message = result.message;
                        }
                        break;
                    }
                    case "READ_TICKETS": {
                        String ticketsXml = readTickets(userId, requestXml);
                        status = "OK";
                        message = ticketsXml;
                        String filterStatus = extractTagValue(requestXml, "status");
                        dataAffected = "tickets queried"
                                + (filterStatus != null && !filterStatus.isEmpty() ? " (status=" + filterStatus + ")"
                                        : "")
                                + " by " + (userId != null && !userId.isEmpty() ? userId : "all");
                        break;
                    }
                    case "UPDATE_TICKET": {
                        String ticketId = extractTagValue(requestXml, "ticketId");
                        String newStatus = extractTagValue(requestXml, "status");
                        OperationResult result = updateTicket(userId, requestXml);
                        if (result.success) {
                            status = "OK";
                            dataAffected = "ticket " + ticketId + " updated"
                                    + (newStatus != null && !newStatus.isEmpty() ? " to " + newStatus : "");
                        } else {
                            dataAffected = "update failed: " + result.message;
                        }
                        message = result.message;
                        break;
                    }
                    case "DELETE_TICKET": {
                        String ticketId = extractTagValue(requestXml, "ticketId");
                        String reason = extractTagValue(requestXml, "deleteReason");
                        OperationResult result = deleteTicket(userId, requestXml);
                        if (result.success) {
                            status = "OK";
                            dataAffected = "ticket " + ticketId + " cancelled"
                                    + (reason != null && !reason.isEmpty() ? " (reason: " + reason + ")" : "");
                        } else {
                            dataAffected = "cancel failed: " + result.message;
                        }
                        message = result.message;
                        break;
                    }
                    case "PERMANENT_DELETE_TICKET": {
                        String ticketId = extractTagValue(requestXml, "ticketId");
                        OperationResult result = permanentDeleteTicket(userId, ticketId);
                        if (result.success) {
                            status = "OK";
                            dataAffected = "ticket " + ticketId + " permanently deleted";
                        } else {
                            dataAffected = "permanent delete failed: " + result.message;
                        }
                        message = result.message;
                        break;
                    }
                    case "PING":
                        status = "OK";
                        message = "PONG from DonationServer.";
                        dataAffected = "health check";
                        break;
                    case "RIDER_SET_AVAILABLE": {
                        if (userId == null || userId.trim().isEmpty()) {
                            message = "User must be logged in to set rider availability.";
                        } else if (!userEmailExists(userId.trim())) {
                            message = "User account not found.";
                        } else if (addRiderToAvailable(userId.trim())) {
                            status = "OK";
                            message = "Rider is now available.";
                            dataAffected = "rider " + userId + " set available";
                        } else {
                            message = "Failed to set rider available.";
                        }
                        break;
                    }
                    case "RIDER_SET_UNAVAILABLE": {
                        if (userId == null || userId.trim().isEmpty()) {
                            message = "User must be logged in.";
                        } else {
                            removeRiderFromAvailable(userId.trim());
                            status = "OK";
                            message = "Rider is now unavailable.";
                            dataAffected = "rider " + userId + " set unavailable";
                        }
                        break;
                    }
                    case "UPDATE_USER_ROLE": {
                        String email = extractTagValue(requestXml, "email");
                        String newRole = extractTagValue(requestXml, "role");

                        if (email == null || newRole == null) {
                            message = "Missing email or role.";
                        } else if (!userEmailExists(email)) {
                            message = "User not found.";
                        } else {
                            if (updateUserRoleInXml(email, newRole)) {
                                status = "OK";
                                message = "User role updated to " + newRole;
                                dataAffected = "user " + email + " role -> " + newRole;
                            } else {
                                message = "Failed to update user role.";
                            }
                        }
                        break;
                    }
                    case "CREATE_DONATION_DRIVE": {
                        if (!isAdminUser(userId)) {
                            message = "Only admins can create donation drives.";
                        } else {
                            OperationResult result = createDonationDrive(userId, requestXml);
                            if (result.success) {
                                status = "OK";
                                message = result.message;
                                dataAffected = "drive created";
                            } else {
                                message = result.message;
                            }
                        }
                        break;
                    }
                    case "READ_DONATION_DRIVES": {
                        String drivesXml = readDonationDrives();
                        status = "OK";
                        message = drivesXml;
                        dataAffected = "drives queried";
                        break;
                    }
                    case "UPDATE_DRIVE_AMOUNT": {
                        String driveTitle = extractTagValue(requestXml, "driveTitle");
                        String amountStr = extractTagValue(requestXml, "amount");
                        if (driveTitle == null || amountStr == null) {
                            message = "Missing driveTitle or amount.";
                        } else {
                            OperationResult result = updateDriveAmount(driveTitle, amountStr);
                            if (result.success) {
                                status = "OK";
                                message = result.message;
                                dataAffected = "drive '" + driveTitle + "' amount updated by " + amountStr;
                            } else {
                                message = result.message;
                            }
                        }
                        break;
                    }
                    default:
                        message = "Unknown action: " + action;
                        dataAffected = "unknown action: " + action;
                }
            }
            logTransaction(action != null ? action : "UNKNOWN", userId, dataAffected);

            String roleTag = "";
            if (userId != null && !userId.isEmpty()) {
                String r = getUserRole(userId);
                roleTag = "<role>" + escapeXml(r) + "</role>";
            }

            String responseXml = "<response>" +
                    "<status>" + escapeXml(status) + "</status>" +
                    "<message>" + escapeXml(message) + "</message>" +
                    (userId != null && !userId.isEmpty()
                            ? "<userId>" + escapeXml(userId) + "</userId>"
                            : "")
                    + roleTag +
                    "</response>";
            // log("RESPONSE", userId, responseXml); // Verbose logging disabled
            return responseXml;
        }

        // ... existing user methods ...

        private static final String DRIVES_DIR = "drives";
        private static long nextDriveId = System.currentTimeMillis(); // Simple ID generation

        private OperationResult createDonationDrive(String adminId, String requestXml) {
            String title = extractTagValue(requestXml, "title");
            String description = extractTagValue(requestXml, "description");
            String targetAmount = extractTagValue(requestXml, "targetAmount");

            if (title == null || title.trim().isEmpty()) {
                return new OperationResult(false, "Title is required.");
            }

            File dir = new File(DRIVES_DIR);
            if (!dir.exists() && !dir.mkdirs()) {
                return new OperationResult(false, "Server error: unable to create drives directory.");
            }

            String driveId = String.valueOf(nextDriveId++);
            File file = new File(dir, driveId + ".xml");

            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            sb.append("<drive>\n");
            sb.append("  <driveId>").append(escapeXml(driveId)).append("</driveId>\n");
            sb.append("  <title>").append(escapeXml(title)).append("</title>\n");
            sb.append("  <description>").append(escapeXml(description != null ? description : ""))
                    .append("</description>\n");
            sb.append("  <targetAmount>").append(escapeXml(targetAmount != null ? targetAmount : "0"))
                    .append("</targetAmount>\n");
            sb.append("  <currentAmount>").append("0").append("</currentAmount>\n");
            sb.append("  <status>").append("ACTIVE").append("</status>\n");
            sb.append("  <createdBy>").append(escapeXml(adminId)).append("</createdBy>\n");
            sb.append("  <createdAt>").append(escapeXml(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())))
                    .append("</createdAt>\n");
            sb.append("</drive>\n");

            try (FileWriter fw = new FileWriter(file, false)) {
                fw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return new OperationResult(false, "Server error: failed to save drive.");
            }

            return new OperationResult(true, "Donation drive created successfully.");
        }

        private String readDonationDrives() {
            File dir = new File(DRIVES_DIR);
            if (!dir.exists()) {
                return "<drives></drives>";
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            StringBuilder sb = new StringBuilder();
            sb.append("<drives>");

            if (files != null) {
                for (File file : files) {
                    String xml = readWholeFile(file);
                    if (xml != null) {
                        int start = xml.indexOf("<drive>");
                        if (start >= 0) {
                            sb.append(xml.substring(start));
                        }
                    }
                }
            }

            sb.append("</drives>");
            return sb.toString();
        }

        /**
         * Finds the drive XML file whose title matches driveTitle and adds amount to
         * currentAmount.
         */
        private OperationResult updateDriveAmount(String driveTitle, String amountStr) {
            double addAmount;
            try {
                addAmount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                return new OperationResult(false, "Invalid amount: " + amountStr);
            }

            File dir = new File(DRIVES_DIR);
            if (!dir.exists()) {
                // No drives directory — silently succeed (hardcoded drives don't have files)
                return new OperationResult(true, "Drive not found in filesystem (hardcoded drive).");
            }

            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files == null)
                return new OperationResult(true, "No drive files found.");

            for (File file : files) {
                String xml = readWholeFile(file);
                if (xml == null)
                    continue;
                String title = extractTagValue(xml, "title");
                if (driveTitle.equalsIgnoreCase(title)) {
                    // Found the matching drive — update currentAmount
                    String currentStr = extractTagValue(xml, "currentAmount");
                    double current = 0;
                    try {
                        current = Double.parseDouble(currentStr != null ? currentStr : "0");
                    } catch (NumberFormatException ignored) {
                    }
                    double newAmount = current + addAmount;

                    // Replace <currentAmount>...</currentAmount> in the XML string
                    String updated = xml.replaceFirst(
                            "<currentAmount>[^<]*</currentAmount>",
                            "<currentAmount>" + newAmount + "</currentAmount>");
                    try (FileWriter fw = new FileWriter(file, false)) {
                        fw.write(updated);
                        return new OperationResult(true, "Drive amount updated.");
                    } catch (IOException e) {
                        return new OperationResult(false, "Failed to write drive file.");
                    }
                }
            }
            // Drive not found in filesystem (e.g. hardcoded drive) — not an error
            return new OperationResult(true, "Drive not tracked in filesystem.");
        }

        private boolean authenticateUser(String email, String password) {
            File file = resolveUsersXmlFile();
            if (!file.exists()) {
                return false;
            }

            try {
                Document doc = loadUsersDocument(file);
                NodeList users = doc.getElementsByTagName("user");
                for (int i = 0; i < users.getLength(); i++) {
                    Element userEl = (Element) users.item(i);
                    String xmlEmail = getUserField(userEl, "email");
                    String xmlPassword = getUserField(userEl, "password");
                    if (xmlEmail != null && xmlPassword != null
                            && xmlEmail.equalsIgnoreCase(email)
                            && xmlPassword.equals(password)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private String extractTagValue(String xml, String tag) {
            String open = "<" + tag + ">";
            String close = "</" + tag + ">";
            int i = xml.indexOf(open);
            int j = xml.indexOf(close);
            if (i == -1 || j == -1 || j <= i) {
                return null;
            }
            return xml.substring(i + open.length(), j).trim();
        }

        private String extractTagValueOrCData(String xml, String tag) {
            String raw = extractTagValue(xml, tag);
            if (raw == null)
                return null;
            if (raw.startsWith("<![CDATA[") && raw.endsWith("]]>")) {
                return raw.substring(9, raw.length() - 3);
            }
            return raw;
        }

        private String escapeXml(String s) {
            if (s == null) {
                return "";
            }
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;");
        }

        private File resolveUsersXmlFile() {
            File cwd = new File(System.getProperty("user.dir"));
            for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
                File candidate = new File(dir, USERS_XML_RELATIVE);
                if (candidate.exists()) {
                    return candidate;
                }
            }
            return new File(cwd, USERS_XML_RELATIVE);
        }

        private Document loadUsersDocument(File file) throws Exception {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(file);
        }

        private void writeUsersDocument(Document doc, File file) throws Exception {
            file.getParentFile().mkdirs();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            try (FileOutputStream out = new FileOutputStream(file)) {
                t.transform(new DOMSource(doc), new StreamResult(out));
            }
        }

        private String getUserField(Element userEl, String tagName) {
            NodeList list = userEl.getElementsByTagName(tagName);
            if (list.getLength() == 0)
                return null;
            Element el = (Element) list.item(0);
            return el.getTextContent() != null ? el.getTextContent().trim() : null;
        }

        private void appendUserField(Document doc, Element userEl, String tagName, String value) {
            Element el = doc.createElement(tagName);
            el.setTextContent(value != null ? value : "");
            userEl.appendChild(el);
        }

        private boolean userEmailExists(String email) {
            File file = resolveUsersXmlFile();
            if (!file.exists())
                return false;
            try {
                Document doc = loadUsersDocument(file);
                NodeList users = doc.getElementsByTagName("user");
                for (int i = 0; i < users.getLength(); i++) {
                    Element userEl = (Element) users.item(i);
                    String xmlEmail = getUserField(userEl, "email");
                    if (xmlEmail != null && xmlEmail.equalsIgnoreCase(email)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        /**
         * Returns the role of the given email from users.xml. If the email is missing
         * or unknown, it defaults to "DONOR.
         * Possible roles include DONOR and RIDER. Admins are handled separately through
         * admin_credentials.xml checking logic if applicable, but usually simplified
         * here.
         * For this project, we might check an admin list, but let's stick to the XML
         * role if present,
         * or hardcoded admin check.
         */
        private String getUserRole(String email) {
            if (email == null || email.trim().isEmpty()) {
                return "DONOR";
            }
            // Hardcoded admin check for safety/legacy
            if ("admin".equalsIgnoreCase(email))
                return "ADMIN";

            File file = resolveUsersXmlFile();
            if (!file.exists())
                return "DONOR";
            try {
                Document doc = loadUsersDocument(file);
                NodeList users = doc.getElementsByTagName("user");
                for (int i = 0; i < users.getLength(); i++) {
                    Element userEl = (Element) users.item(i);
                    String xmlEmail = getUserField(userEl, "email");
                    if (xmlEmail != null && xmlEmail.equalsIgnoreCase(email)) {
                        String role = getUserField(userEl, "role");
                        return (role != null && !role.isEmpty()) ? role.toUpperCase() : "DONOR";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "DONOR";
        }

        private boolean isAdminUser(String userId) {
            if (userId == null)
                return false;
            if (userId.equals("admin"))
                return true;
            return "ADMIN".equalsIgnoreCase(getUserRole(userId));
        }

        private boolean isRiderUser(String userId) {
            String role = getUserRole(userId);
            return "RIDER".equalsIgnoreCase(role);
        }

        private boolean isDonorUser(String userId) {
            String role = getUserRole(userId);
            return "DONOR".equalsIgnoreCase(role);
        }

        private boolean saveUserToXmlSingleFile(
                String email,
                String password,
                String firstName,
                String lastName,
                String middleName,
                String dateOfBirth,
                String address,
                String phone,
                String role) {
            try {
                File file = resolveUsersXmlFile();
                Document doc;
                Element root;

                if (file.exists()) {
                    doc = loadUsersDocument(file);
                    root = doc.getDocumentElement();
                    if (root == null) {
                        root = doc.createElement("users");
                        doc.appendChild(root);
                    }
                } else {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    doc = db.newDocument();
                    root = doc.createElement("users");
                    doc.appendChild(root);
                }

                Element userEl = doc.createElement("user");
                appendUserField(doc, userEl, "email", email);
                appendUserField(doc, userEl, "password", password);
                appendUserField(doc, userEl, "firstName", firstName != null ? firstName : "");
                appendUserField(doc, userEl, "lastName", lastName != null ? lastName : "");
                appendUserField(doc, userEl, "middleName", middleName != null ? middleName : "");
                appendUserField(doc, userEl, "dateOfBirth", dateOfBirth != null ? dateOfBirth : "");
                appendUserField(doc, userEl, "address", address != null ? address : "");
                appendUserField(doc, userEl, "phoneNumber", phone != null ? phone : "");
                appendUserField(doc, userEl, "role", role != null ? role : "DONOR");

                root.appendChild(userEl);
                writeUsersDocument(doc, file);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean updateUserRoleInXml(String email, String newRole) {
            try {
                File file = resolveUsersXmlFile();
                if (!file.exists())
                    return false;
                Document doc = loadUsersDocument(file);
                NodeList users = doc.getElementsByTagName("user");
                boolean found = false;
                for (int i = 0; i < users.getLength(); i++) {
                    Element userEl = (Element) users.item(i);
                    String xmlEmail = getUserField(userEl, "email");
                    if (xmlEmail != null && xmlEmail.equalsIgnoreCase(email)) {
                        NodeList roles = userEl.getElementsByTagName("role");
                        if (roles.getLength() > 0) {
                            roles.item(0).setTextContent(newRole);
                        } else {
                            appendUserField(doc, userEl, "role", newRole);
                        }
                        found = true;
                        break;
                    }
                }
                if (found) {
                    writeUsersDocument(doc, file);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private static final String TICKETS_DIR = "tickets";
        private static final String AVAILABLE_RIDERS_FILE = "available_riders.txt";
        private static final Object TICKET_LOCK = new Object();
        private static final Object RIDER_LOCK = new Object();
        // Simple incremental ID for session; in production use persistent counter
        private static long nextTicketId = System.currentTimeMillis();

        private static File resolveAvailableRidersFile() {
            File cwd = new File(System.getProperty("user.dir"));
            File ticketsDir = new File(cwd, TICKETS_DIR);
            File parent = ticketsDir.getParentFile();
            return new File(parent != null ? parent : cwd, AVAILABLE_RIDERS_FILE);
        }

        /**
         * Returns true only when at least one rider is marked available.
         */
        private static boolean areRidersAvailable() {
            File file = resolveAvailableRidersFile();
            if (!file.exists())
                return false;
            synchronized (RIDER_LOCK) {
                try {
                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    for (String line : lines) {
                        if (line != null && !line.trim().isEmpty()) {
                            return true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }

        private static boolean addRiderToAvailable(String userId) {
            if (userId == null || userId.trim().isEmpty())
                return false;
            File file = resolveAvailableRidersFile();
            synchronized (RIDER_LOCK) {
                try {
                    Set<String> riders = new HashSet<>();
                    if (file.exists()) {
                        riders.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
                    }
                    riders.removeIf(s -> s == null || s.trim().isEmpty());
                    riders.add(userId.trim());
                    file.getParentFile().mkdirs();
                    Files.write(file.toPath(), riders, StandardCharsets.UTF_8);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        private static boolean removeRiderFromAvailable(String userId) {
            if (userId == null || userId.trim().isEmpty())
                return false;
            File file = resolveAvailableRidersFile();
            synchronized (RIDER_LOCK) {
                try {
                    if (!file.exists())
                        return true;
                    List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    String target = userId.trim();
                    boolean removed = lines.removeIf(s -> s != null && s.trim().equals(target));
                    Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        private static class OperationResult {
            final boolean success;
            final String message;

            OperationResult(boolean success, String message) {
                this.success = success;
                this.message = message;
            }
        }

        private OperationResult createTicket(String userId, String requestXml) {
            if (userId == null || userId.trim().isEmpty()) {
                return new OperationResult(false, "UserId is required to create a donation ticket.");
            }

            String itemCategory = extractTagValue(requestXml, "itemCategory");
            if (itemCategory == null || itemCategory.isEmpty()) {
                itemCategory = extractTagValue(requestXml, "type");
            }

            String quantityStr = extractTagValue(requestXml, "quantity");
            String condition = extractTagValue(requestXml, "condition");
            String expirationDate = extractTagValue(requestXml, "expirationDate");
            String pickupDateTime = extractTagValue(requestXml, "pickupDateTime");
            String pickupLocation = extractTagValue(requestXml, "pickupLocation");
            String photoPath = extractTagValue(requestXml, "photoPath");
            String notes = extractTagValue(requestXml, "details");
            String donationDrive = extractTagValue(requestXml, "donationDrive");
            String deliveryDestination = extractTagValue(requestXml, "deliveryDestination");
            String photoBase64 = extractTagValueOrCData(requestXml, "photoBase64");

            if (itemCategory == null || itemCategory.trim().isEmpty()) {
                return new OperationResult(false, "Item category (food, clothes, books, etc.) is required.");
            }

            int quantity = 0;
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    quantity = Integer.parseInt(quantityStr.trim());
                } catch (NumberFormatException ex) {
                    return new OperationResult(false, "Quantity must be a whole number.");
                }
                if (quantity <= 0) {
                    return new OperationResult(false, "Quantity must be greater than zero.");
                }
            }

            if (expirationDate != null && !expirationDate.trim().isEmpty()) {
                try {
                    Date exp = new SimpleDateFormat("yyyy-MM-dd").parse(expirationDate.trim());
                    if (exp.before(new Date())) {
                        return new OperationResult(false,
                                "Donation items appear expired based on the provided expiration date.");
                    }
                } catch (Exception ignored) {
                }
            }

            String ticketId;

            synchronized (TICKET_LOCK) {
                ticketId = String.valueOf(nextTicketId++);

                File dir = new File(TICKETS_DIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    return new OperationResult(false, "Server error: unable to create tickets directory.");
                }

                File file = new File(dir, ticketId + ".xml");

                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                sb.append("<ticket>\n");
                sb.append("  <ticketId>").append(escapeXml(ticketId)).append("</ticketId>\n");
                sb.append("  <userId>").append(escapeXml(userId)).append("</userId>\n");
                sb.append("  <status>").append("PENDING").append("</status>\n");
                sb.append("  <createdAt>").append(escapeXml(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))).append("</createdAt>\n");
                sb.append("  <itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>\n");
                sb.append("  <quantity>").append(escapeXml(quantityStr != null ? quantityStr : ""))
                        .append("</quantity>\n");
                sb.append("  <condition>").append(escapeXml(condition != null ? condition : ""))
                        .append("</condition>\n");
                sb.append("  <expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                        .append("</expirationDate>\n");
                sb.append("  <pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : ""))
                        .append("</pickupDateTime>\n");
                sb.append("  <pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                        .append("</pickupLocation>\n");
                sb.append("  <photoPath>").append(escapeXml(photoPath != null ? photoPath : ""))
                        .append("</photoPath>\n");
                sb.append("  <notes>").append(escapeXml(notes != null ? notes : "")).append("</notes>\n");
                sb.append("  <donationDrive>").append(escapeXml(donationDrive != null ? donationDrive : ""))
                        .append("</donationDrive>\n");
                sb.append("  <deliveryDestination>")
                        .append(escapeXml(deliveryDestination != null ? deliveryDestination : ""))
                        .append("</deliveryDestination>\n");

                // New field for Rider ID
                sb.append("  <riderId>").append("</riderId>\n");

                if (photoBase64 != null && !photoBase64.isEmpty()) {
                    sb.append("  <photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>\n");
                }
                sb.append("</ticket>\n");

                try (FileWriter fw = new FileWriter(file, false)) {
                    fw.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return new OperationResult(false, "Server error: failed to save donation ticket.");
                }
            }

            String msg = "Donation ticket created successfully. Ticket ID: " + ticketId;
            return new OperationResult(true, msg);
        }

        private String readTickets(String requesterUserId, String requestXml) {
            synchronized (TICKET_LOCK) {
                String filterStatus = extractTagValue(requestXml, "status");

                // Determine Role
                boolean adminUser = isAdminUser(requesterUserId) || "admin".equalsIgnoreCase(requesterUserId);
                boolean riderUser = isRiderUser(requesterUserId) || "rider".equalsIgnoreCase(requesterUserId);

                File dir = new File(TICKETS_DIR);
                File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));

                StringBuilder ticketsBuilder = new StringBuilder();
                ticketsBuilder.append("<tickets>");

                if (files != null) {
                    for (File file : files) {
                        String xml = readWholeFile(file);
                        if (xml == null || xml.trim().isEmpty()) {
                            continue;
                        }

                        String ticketUserId = extractTagValue(xml, "userId");
                        String ticketStatus = extractTagValue(xml, "status");
                        String ticketRiderId = extractTagValue(xml, "riderId");

                        String isDeleted = extractTagValue(xml, "isDeleted");
                        if ("true".equalsIgnoreCase(isDeleted != null ? isDeleted.trim() : "")) {
                            continue;
                        }

                        boolean visible = false;

                        if (adminUser) {
                            // Admins see EVERYTHING
                            visible = true;
                        } else if (riderUser) {
                            // Riders see:
                            // 1. ALL PENDING tickets (so they can accept them)
                            // 2. ANY ticket where they are the assigned rider (Accepted/PickedUp/Delivered)
                            boolean isPending = "PENDING".equalsIgnoreCase(ticketStatus);
                            boolean isAssignedToMe = (ticketRiderId != null && ticketRiderId.equals(requesterUserId));
                            boolean isMine = (ticketUserId != null && requesterUserId != null
                                    && requesterUserId.equals(ticketUserId));

                            visible = isPending || isAssignedToMe || isMine;
                        } else {
                            // Donors see ONLY their own tickets
                            // Or default case for random users
                            visible = (ticketUserId != null && requesterUserId != null
                                    && requesterUserId.equals(ticketUserId));
                        }

                        // Apply status filter if provided
                        if (visible && filterStatus != null && !filterStatus.isEmpty()) {
                            if (!filterStatus.equalsIgnoreCase(ticketStatus)) {
                                visible = false;
                            }
                        }

                        if (!visible) {
                            continue;
                        }

                        String ticketXml = xml.trim();
                        int start = ticketXml.indexOf("<ticket>");
                        if (start >= 0) {
                            ticketXml = ticketXml.substring(start);
                        }

                        ticketsBuilder.append(ticketXml);
                    }
                }

                ticketsBuilder.append("</tickets>");
                return ticketsBuilder.toString();
            }
        }

        private String readWholeFile(File file) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return sb.toString();
        }

        private OperationResult updateTicket(String requesterUserId, String requestXml) {
            synchronized (TICKET_LOCK) {
                String ticketId = extractTagValue(requestXml, "ticketId");
                if (ticketId == null || ticketId.trim().isEmpty()) {
                    return new OperationResult(false, "ticketId is required to update a ticket.");
                }

                String newStatus = extractTagValue(requestXml, "status");
                String qualityStatus = extractTagValue(requestXml, "qualityStatus");
                String qualityReason = extractTagValue(requestXml, "qualityReason");
                String newPickupTime = extractTagValue(requestXml, "pickupDateTime");

                File dir = new File(TICKETS_DIR);
                File file = new File(dir, ticketId + ".xml");
                if (!file.exists()) {
                    return new OperationResult(false, "Ticket " + ticketId + " not found.");
                }

                String xml = readWholeFile(file);
                if (xml == null || xml.trim().isEmpty()) {
                    return new OperationResult(false, "Ticket " + ticketId + " is empty or unreadable.");
                }

                String ticketUserId = extractTagValue(xml, "userId");
                String oldStatus = extractTagValue(xml, "status");
                String createdAt = extractTagValue(xml, "createdAt");
                String itemCategory = extractTagValue(xml, "itemCategory");
                String quantityStr = extractTagValue(xml, "quantity");
                String condition = extractTagValue(xml, "condition");
                String expirationDate = extractTagValue(xml, "expirationDate");
                String pickupDateTime = extractTagValue(xml, "pickupDateTime");
                String pickupLocation = extractTagValue(xml, "pickupLocation");
                String photoPath = extractTagValue(xml, "photoPath");
                String notes = extractTagValue(xml, "notes");
                String donationDrive = extractTagValue(xml, "donationDrive");
                String deliveryDestination = extractTagValue(xml, "deliveryDestination");
                String existingQuality = extractTagValue(xml, "qualityStatus");
                String existingReason = extractTagValue(xml, "qualityReason");
                String statusHistory = extractTagValue(xml, "statusHistory");
                String currentRiderId = extractTagValue(xml, "riderId");
                String photoBase64 = extractTagValueOrCData(xml, "photoBase64");

                boolean adminUser = isAdminUser(requesterUserId);
                boolean riderUser = isRiderUser(requesterUserId);
                boolean donorUser = isDonorUser(requesterUserId);

                // Permissions Check
                if (!adminUser) {
                    if (riderUser) {
                        // Rider logic handled below
                    } else {
                        // Regular donor can only touch own tickets
                        if (requesterUserId == null || !requesterUserId.equals(ticketUserId)) {
                            return new OperationResult(false, "You are not allowed to modify this ticket.");
                        }
                    }
                }

                String nowTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                StringBuilder historyBuilder = new StringBuilder();
                if (statusHistory != null && !statusHistory.isEmpty()) {
                    historyBuilder.append(statusHistory).append(" | ");
                }

                String finalStatus = oldStatus;
                String finalRiderId = currentRiderId;

                // Status Update Logic
                if (newStatus != null && !newStatus.trim().isEmpty()) {
                    String oldNormalized = oldStatus != null ? oldStatus.toUpperCase() : "";
                    String newNormalized = newStatus.toUpperCase();

                    // Donors are not allowed to change ticket status directly
                    if (donorUser && !oldNormalized.equals(newNormalized) && !adminUser) {
                        // Check if it is a cancellation? Donors should be able to cancel.
                        // But we'll stick to strict rules for now unless it's DELETE_TICKET action
                        return new OperationResult(false, "Donors cannot change ticket status directly.");
                    }

                    // Rider accepting a ticket
                    if (riderUser && "ACCEPTED".equals(newNormalized)) {
                        // Can only accept if PENDING
                        if (!"PENDING".equals(oldNormalized)) {
                            return new OperationResult(false,
                                    "This ticket has already been accepted by another rider.");
                        }
                        // Assign rider
                        finalRiderId = requesterUserId;
                    }
                    // Rider updating own ticket (PICKED_UP, DELIVERED, etc)
                    else if (riderUser && !oldNormalized.equals(newNormalized)) {
                        // Rider can only update if they are the assigned rider
                        if (currentRiderId == null || !currentRiderId.equals(requesterUserId)) {
                            return new OperationResult(false, "You are not the assigned rider for this ticket.");
                        }
                    }

                    if (!oldNormalized.equals(newNormalized)) {
                        finalStatus = newNormalized;
                        historyBuilder.append(String.format("%s: %s -> %s by %s", nowTs, oldNormalized, newNormalized,
                                requesterUserId));
                    }
                }

                // If pickup time is updated
                String finalPickupTime = (newPickupTime != null && !newPickupTime.isEmpty()) ? newPickupTime
                        : pickupDateTime;

                // Re-construct XML
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                sb.append("<ticket>\n");
                sb.append("  <ticketId>").append(escapeXml(ticketId)).append("</ticketId>\n");
                sb.append("  <userId>").append(escapeXml(ticketUserId)).append("</userId>\n");
                sb.append("  <status>").append(escapeXml(finalStatus)).append("</status>\n");
                sb.append("  <createdAt>").append(escapeXml(createdAt)).append("</createdAt>\n");
                sb.append("  <itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>\n");
                sb.append("  <quantity>").append(escapeXml(quantityStr)).append("</quantity>\n");
                sb.append("  <condition>").append(escapeXml(condition)).append("</condition>\n");
                sb.append("  <expirationDate>").append(escapeXml(expirationDate)).append("</expirationDate>\n");
                sb.append("  <pickupDateTime>").append(escapeXml(finalPickupTime)).append("</pickupDateTime>\n");
                sb.append("  <pickupLocation>").append(escapeXml(pickupLocation)).append("</pickupLocation>\n");
                sb.append("  <photoPath>").append(escapeXml(photoPath)).append("</photoPath>\n");
                sb.append("  <notes>").append(escapeXml(notes)).append("</notes>\n");
                sb.append("  <donationDrive>").append(escapeXml(donationDrive)).append("</donationDrive>\n");
                sb.append("  <deliveryDestination>").append(escapeXml(deliveryDestination))
                        .append("</deliveryDestination>\n");

                // Write riderId
                sb.append("  <riderId>").append(escapeXml(finalRiderId != null ? finalRiderId : ""))
                        .append("</riderId>\n");

                String fQual = (qualityStatus != null && !qualityStatus.isEmpty()) ? qualityStatus : existingQuality;
                String fReas = (qualityReason != null && !qualityReason.isEmpty()) ? qualityReason : existingReason;

                if (fQual != null && !fQual.isEmpty()) {
                    sb.append("  <qualityStatus>").append(escapeXml(fQual)).append("</qualityStatus>\n");
                }
                if (fReas != null && !fReas.isEmpty()) {
                    sb.append("  <qualityReason>").append(escapeXml(fReas)).append("</qualityReason>\n");
                }

                if (historyBuilder.length() > 0) {
                    sb.append("  <statusHistory>").append(escapeXml(historyBuilder.toString()))
                            .append("</statusHistory>\n");
                }
                if (photoBase64 != null && !photoBase64.isEmpty()) {
                    sb.append("  <photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>\n");
                }

                sb.append("</ticket>\n");

                try (FileWriter fw = new FileWriter(file, false)) {
                    fw.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return new OperationResult(false, "Server error: failed to save ticket.");
                }

                return new OperationResult(true, "Ticket updated successfully.");
            }
        }

        private OperationResult deleteTicket(String requesterUserId, String requestXml) {
            synchronized (TICKET_LOCK) {
                String ticketId = extractTagValue(requestXml, "ticketId");
                String reason = extractTagValue(requestXml, "deleteReason");

                if (ticketId == null || ticketId.isEmpty()) {
                    return new OperationResult(false, "Ticket ID required.");
                }

                File dir = new File(TICKETS_DIR);
                File file = new File(dir, ticketId + ".xml");
                if (!file.exists()) {
                    return new OperationResult(false, "Ticket not found.");
                }

                String xml = readWholeFile(file);
                String ownerId = extractTagValue(xml, "userId");
                boolean isAdmin = isAdminUser(requesterUserId) || "admin".equalsIgnoreCase(requesterUserId);

                if (!isAdmin && (ownerId == null || !ownerId.equals(requesterUserId))) {
                    return new OperationResult(false, "Permission denied.");
                }

                // Construct update to CANCELLED
                String updateXml = "<request><ticketId>" + ticketId + "</ticketId><status>CANCELLED</status>";
                if (reason != null) {
                    updateXml += "<qualityReason>" + escapeXml(reason) + "</qualityReason>";
                }
                updateXml += "</request>";

                return updateTicket(requesterUserId, updateXml);
            }
        }

        private OperationResult permanentDeleteTicket(String requesterUserId, String ticketId) {
            synchronized (TICKET_LOCK) {
                if (!isAdminUser(requesterUserId)) {
                    return new OperationResult(false, "Only admins can permanently delete tickets.");
                }
                File dir = new File(TICKETS_DIR);
                File file = new File(dir, ticketId + ".xml");
                if (file.exists()) {
                    if (file.delete()) {
                        return new OperationResult(true, "Permanently deleted.");
                    } else {
                        return new OperationResult(false, "Failed to delete file.");
                    }
                }
                return new OperationResult(false, "Ticket not found.");
            }
        }

        private void log(String type, String userId, String message) {
            System.out.println(String.format("[%s] [%s] %s: %s",
                    new SimpleDateFormat("HH:mm:ss").format(new Date()),
                    userId != null ? userId : "anonymous",
                    type,
                    message));
        }

        private void logTransaction(String action, String userId, String details) {
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                fw.write(String.format("%s | %s | %s | %s\n", time, action, userId, details));
            } catch (IOException ignored) {
            }
        }
    }
}
