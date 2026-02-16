import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
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
                            new OutputStreamWriter(socket.getOutputStream(), "UTF-8"))
            ) {
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
            log("REQUEST", userId, requestXml);

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
                            dataAffected = "user session: " + email.trim();
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

                        if (email == null || password == null) {
                            message = "Missing email or password.";
                            dataAffected = "registration attempt (missing fields)";
                        } else if (userEmailExists(email.trim())) {
                            message = "Registration failed: email already exists.";
                            dataAffected = "email already exists: " + email.trim();
                        } else if (saveUserToXmlSingleFile(email.trim(), password.trim(),
                                firstName, lastName, middleName, dateOfBirth, address, phone)) {
                            status = "OK";
                            message = "Registration successful.";
                            dataAffected = "new user registered: " + email.trim();
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
                        dataAffected = "tickets queried" + (filterStatus != null && !filterStatus.isEmpty() ? " (status=" + filterStatus + ")" : "") + " by " + (userId != null && !userId.isEmpty() ? userId : "all");
                        break;
                    }
                    case "UPDATE_TICKET": {
                        String ticketId = extractTagValue(requestXml, "ticketId");
                        String newStatus = extractTagValue(requestXml, "status");
                        OperationResult result = updateTicket(userId, requestXml);
                        if (result.success) {
                            status = "OK";
                            dataAffected = "ticket " + ticketId + " updated" + (newStatus != null && !newStatus.isEmpty() ? " to " + newStatus : "");
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
                            dataAffected = "ticket " + ticketId + " cancelled" + (reason != null && !reason.isEmpty() ? " (reason: " + reason + ")" : "");
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
                    default:
                        message = "Unknown action: " + action;
                        dataAffected = "unknown action: " + action;
                }
            }
            logTransaction(action != null ? action : "UNKNOWN", userId, dataAffected);
            String responseXml =
                    "<response>" +
                            "<status>" + escapeXml(status) + "</status>" +
                            "<message>" + escapeXml(message) + "</message>" +
                            (userId != null && !userId.isEmpty()
                                    ? "<userId>" + escapeXml(userId) + "</userId>"
                                    : "") +
                            "</response>";
            log("RESPONSE", userId, responseXml);
            return responseXml;
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

        /** Extracts tag content and unwraps CDATA if present (e.g. &lt;![CDATA[base64]]&gt;). */
        private String extractTagValueOrCData(String xml, String tag) {
            String raw = extractTagValue(xml, tag);
            if (raw == null) return null;
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
            if (list.getLength() == 0) return null;
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
            if (!file.exists()) return false;
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

        private boolean saveUserToXmlSingleFile(
                String email,
                String password,
                String firstName,
                String lastName,
                String middleName,
                String dateOfBirth,
                String address,
                String phone
        ) {
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

                root.appendChild(userEl);
                writeUsersDocument(doc, file);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private static final String TICKETS_DIR = "tickets";
        private static final Object TICKET_LOCK = new Object();
        private static long nextTicketId = System.currentTimeMillis();

        // Placeholder until rider UI/system exists
        private static boolean areRidersAvailable() {
            return true;
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
            if (!areRidersAvailable()) {
                return new OperationResult(false, "NO_RIDERS_AVAILABLE");
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
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sb.append("<ticket>");
                sb.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
                sb.append("<userId>").append(escapeXml(userId)).append("</userId>");
                sb.append("<status>").append("PENDING").append("</status>");
                sb.append("<createdAt>").append(escapeXml(
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
                )).append("</createdAt>");
                sb.append("<itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>");
                sb.append("<quantity>").append(escapeXml(quantityStr != null ? quantityStr : "")).append("</quantity>");
                sb.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
                sb.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                        .append("</expirationDate>");
                sb.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : ""))
                        .append("</pickupDateTime>");
                sb.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                        .append("</pickupLocation>");
                sb.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
                sb.append("<notes>").append(escapeXml(notes != null ? notes : "")).append("</notes>");
                sb.append("<donationDrive>").append(escapeXml(donationDrive != null ? donationDrive : "")).append("</donationDrive>");
                sb.append("<deliveryDestination>").append(escapeXml(deliveryDestination != null ? deliveryDestination : "")).append("</deliveryDestination>");
                if (photoBase64 != null && !photoBase64.isEmpty()) {
                    sb.append("<photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>");
                }
                sb.append("</ticket>");

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
            String filterStatus = extractTagValue(requestXml, "status");

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

                    String isDeleted = extractTagValue(xml, "isDeleted");
                    if ("true".equalsIgnoreCase(isDeleted != null ? isDeleted.trim() : "")) {
                        continue;
                    }

                    boolean matchesUser =
                            (requesterUserId == null || requesterUserId.isEmpty())
                                    || (ticketUserId != null && requesterUserId.equals(ticketUserId));

                    boolean matchesStatus =
                            (filterStatus == null || filterStatus.isEmpty())
                                    || (ticketStatus != null && filterStatus.equalsIgnoreCase(ticketStatus));

                    if (!matchesUser || !matchesStatus) {
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


            if (ticketUserId != null && requesterUserId != null
                    && !requesterUserId.isEmpty()
                    && requesterUserId.equals(ticketUserId)) {
            }

            String nowTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            StringBuilder historyBuilder = new StringBuilder();
            if (statusHistory != null && !statusHistory.isEmpty()) {
                historyBuilder.append(statusHistory).append(" | ");
            }

            String finalStatus = oldStatus;
            if (newStatus != null && !newStatus.trim().isEmpty()) {
                String oldNormalized = oldStatus != null ? oldStatus.toUpperCase() : "";
                String newNormalized = newStatus.toUpperCase();

                boolean allowed = false;
                if ("PENDING".equals(oldNormalized) && ("ACCEPTED".equals(newNormalized) || "REJECTED".equals(newNormalized))) {
                    allowed = true;
                } else if ("ACCEPTED".equals(oldNormalized) && ("PICKED_UP".equals(newNormalized) || "REJECTED".equals(newNormalized))) {
                    allowed = true;
                } else if ("PICKED_UP".equals(oldNormalized) && "DELIVERED".equals(newNormalized)) {
                    allowed = true;
                } else if (oldNormalized.equals(newNormalized)) {
                    allowed = true;
                }

                if (!allowed) {
                    return new OperationResult(false,
                            "Invalid status transition: " + oldNormalized + " -> " + newNormalized);
                }

                finalStatus = newNormalized;
                historyBuilder.append(nowTs)
                        .append(" ").append(requesterUserId != null ? requesterUserId : "SYSTEM")
                        .append(" changed status from ")
                        .append(oldStatus != null ? oldStatus : "(none)")
                        .append(" to ")
                        .append(newNormalized);
            }

            String finalQualityStatus = existingQuality;
            String finalQualityReason = existingReason;

            if (qualityStatus != null && !qualityStatus.trim().isEmpty()) {
                finalQualityStatus = qualityStatus.toUpperCase();
                finalQualityReason = qualityReason != null ? qualityReason : existingReason;

                if (historyBuilder.length() > 0) {
                    historyBuilder.append(" | ");
                }
                historyBuilder.append(nowTs)
                        .append(" ").append(requesterUserId != null ? requesterUserId : "SYSTEM")
                        .append(" set quality to ").append(finalQualityStatus);
                if (finalQualityReason != null && !finalQualityReason.isEmpty()) {
                    historyBuilder.append(" (reason: ").append(finalQualityReason).append(")");
                }
            }

            String finalPickupTime = pickupDateTime;
            if (newPickupTime != null && !newPickupTime.trim().isEmpty()) {
                finalPickupTime = newPickupTime.trim();

                if (historyBuilder.length() > 0) {
                    historyBuilder.append(" | ");
                }
                historyBuilder.append(nowTs)
                        .append(" ").append(requesterUserId != null ? requesterUserId : "SYSTEM")
                        .append(" rescheduled pickup to ").append(finalPickupTime);
            }

            String finalHistory = historyBuilder.toString();

            synchronized (TICKET_LOCK) {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sb.append("<ticket>");
                sb.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
                sb.append("<userId>").append(escapeXml(ticketUserId != null ? ticketUserId : "")).append("</userId>");
                sb.append("<status>").append(escapeXml(finalStatus != null ? finalStatus : "")).append("</status>");
                sb.append("<createdAt>").append(escapeXml(createdAt != null ? createdAt : "")).append("</createdAt>");
                sb.append("<lastUpdatedAt>").append(escapeXml(nowTs)).append("</lastUpdatedAt>");
                sb.append("<itemCategory>").append(escapeXml(itemCategory != null ? itemCategory : "")).append("</itemCategory>");
                sb.append("<quantity>").append(escapeXml(quantityStr != null ? quantityStr : "")).append("</quantity>");
                sb.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
                sb.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                        .append("</expirationDate>");
                sb.append("<pickupDateTime>").append(escapeXml(finalPickupTime != null ? finalPickupTime : ""))
                        .append("</pickupDateTime>");
                sb.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                        .append("</pickupLocation>");
                sb.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
                sb.append("<notes>").append(escapeXml(notes != null ? notes : "")).append("</notes>");
                sb.append("<donationDrive>").append(escapeXml(donationDrive != null ? donationDrive : "")).append("</donationDrive>");
                sb.append("<deliveryDestination>").append(escapeXml(deliveryDestination != null ? deliveryDestination : "")).append("</deliveryDestination>");
                sb.append("<qualityStatus>").append(escapeXml(finalQualityStatus != null ? finalQualityStatus : ""))
                        .append("</qualityStatus>");
                sb.append("<qualityReason>").append(escapeXml(finalQualityReason != null ? finalQualityReason : ""))
                        .append("</qualityReason>");
                sb.append("<statusHistory>").append(escapeXml(finalHistory != null ? finalHistory : ""))
                        .append("</statusHistory>");
                sb.append("</ticket>");

                try (FileWriter fw = new FileWriter(file, false)) {
                    fw.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return new OperationResult(false, "Server error: failed to update ticket " + ticketId + ".");
                }
            }

            return new OperationResult(true, "Ticket " + ticketId + " updated successfully.");
        }

        private OperationResult deleteTicket(String requesterUserId, String requestXml) {
            String ticketId = extractTagValue(requestXml, "ticketId");
            String reason = extractTagValue(requestXml, "deleteReason");

            if (ticketId == null || ticketId.trim().isEmpty()) {
                return new OperationResult(false, "ticketId is required to delete a ticket.");
            }

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
            String status = extractTagValue(xml, "status");
            String createdAt = extractTagValue(xml, "createdAt");
            String lastUpdatedAt = extractTagValue(xml, "lastUpdatedAt");
            String itemCategory = extractTagValue(xml, "itemCategory");
            String quantityStr = extractTagValue(xml, "quantity");
            String condition = extractTagValue(xml, "condition");
            String expirationDate = extractTagValue(xml, "expirationDate");
            String pickupDateTime = extractTagValue(xml, "pickupDateTime");
            String pickupLocation = extractTagValue(xml, "pickupLocation");
            String photoPath = extractTagValue(xml, "photoPath");
            String notes = extractTagValue(xml, "notes");
            String qualityStatus = extractTagValue(xml, "qualityStatus");
            String qualityReason = extractTagValue(xml, "qualityReason");
            String statusHistory = extractTagValue(xml, "statusHistory");
            String deleteReason = extractTagValue(xml, "deleteReason");
            String deletedAt = extractTagValue(xml, "deletedAt");
            String isDeleted = extractTagValue(xml, "isDeleted");

            String nowTs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            String finalStatus = status;
            if (status == null || !"REJECTED".equalsIgnoreCase(status)) {
                finalStatus = "CANCELLED";
            }

            String finalDeleteReason = reason != null && !reason.isEmpty()
                    ? reason
                    : (deleteReason != null ? deleteReason : "Cancelled by system");

            String finalDeletedAt = nowTs;
            String finalIsDeleted = "true";

            StringBuilder historyBuilder = new StringBuilder();
            if (statusHistory != null && !statusHistory.isEmpty()) {
                historyBuilder.append(statusHistory).append(" | ");
            }
            historyBuilder.append(nowTs)
                    .append(" ").append(requesterUserId != null ? requesterUserId : "SYSTEM")
                    .append(" marked ticket as ").append(finalStatus)
                    .append(" (reason: ").append(finalDeleteReason).append(")");

            String finalHistory = historyBuilder.toString();

            synchronized (TICKET_LOCK) {
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                sb.append("<ticket>");
                sb.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
                sb.append("<userId>").append(escapeXml(ticketUserId != null ? ticketUserId : "")).append("</userId>");
                sb.append("<status>").append(escapeXml(finalStatus)).append("</status>");
                sb.append("<createdAt>").append(escapeXml(createdAt != null ? createdAt : "")).append("</createdAt>");
                sb.append("<lastUpdatedAt>").append(escapeXml(nowTs)).append("</lastUpdatedAt>");
                sb.append("<itemCategory>").append(escapeXml(itemCategory != null ? itemCategory : "")).append("</itemCategory>");
                sb.append("<quantity>").append(escapeXml(quantityStr != null ? quantityStr : "")).append("</quantity>");
                sb.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
                sb.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                        .append("</expirationDate>");
                sb.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : ""))
                        .append("</pickupDateTime>");
                sb.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                        .append("</pickupLocation>");
                sb.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
                sb.append("<notes>").append(escapeXml(notes != null ? notes : "")).append("</notes>");
                sb.append("<qualityStatus>").append(escapeXml(qualityStatus != null ? qualityStatus : ""))
                        .append("</qualityStatus>");
                sb.append("<qualityReason>").append(escapeXml(qualityReason != null ? qualityReason : ""))
                        .append("</qualityReason>");
                sb.append("<statusHistory>").append(escapeXml(finalHistory)).append("</statusHistory>");
                sb.append("<isDeleted>").append(escapeXml(finalIsDeleted)).append("</isDeleted>");
                sb.append("<deletedAt>").append(escapeXml(finalDeletedAt)).append("</deletedAt>");
                sb.append("<deleteReason>").append(escapeXml(finalDeleteReason)).append("</deleteReason>");
                sb.append("</ticket>");

                try (FileWriter fw = new FileWriter(file, false)) {
                    fw.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return new OperationResult(false, "Server error: failed to delete ticket " + ticketId + ".");
                }
            }

            return new OperationResult(true, "Ticket " + ticketId + " cancelled successfully.");
        }

        private void log(String transaction, String userId, String data) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String user = (userId == null || userId.isEmpty()) ? "ANONYMOUS" : userId;

            System.out.printf("[%s] %s %s%n", timestamp, user, transaction);

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(timestamp).append("]").append(System.lineSeparator());
            sb.append("User: ").append(user).append(System.lineSeparator());
            sb.append("Transaction: ").append(transaction).append(System.lineSeparator());
            sb.append("Data:").append(System.lineSeparator());
            sb.append(data).append(System.lineSeparator());
            sb.append("----").append(System.lineSeparator());

            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void logTransaction(String action, String userId, String dataAffected) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String user = (userId == null || userId.isEmpty()) ? "ANONYMOUS" : userId;
            String affected = (dataAffected == null || dataAffected.isEmpty()) ? "(none)" : dataAffected;

            String line = String.format("[%s] User: %s | Transaction: %s | Data affected: %s",
                    timestamp, user, action, affected);
            System.out.println(line);

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(timestamp).append("]").append(System.lineSeparator());
            sb.append("User involved: ").append(user).append(System.lineSeparator());
            sb.append("Transaction performed: ").append(action).append(System.lineSeparator());
            sb.append("Data affected: ").append(affected).append(System.lineSeparator());
            sb.append("----").append(System.lineSeparator());

            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private OperationResult permanentDeleteTicket(String adminUserId, String ticketId) {
            if (adminUserId == null || adminUserId.trim().isEmpty()) {
                return new OperationResult(false, "Admin userId is required for permanent deletion.");
            }

            if (!"admin".equals(adminUserId)) {
                return new OperationResult(false, "Only admin users can perform permanent deletion.");
            }

            File dir = new File(TICKETS_DIR);
            File file = new File(dir, ticketId + ".xml");
            
            if (!file.exists()) {
                return new OperationResult(false, "Ticket " + ticketId + " not found.");
            }

            try {
                if (file.delete()) {
                    return new OperationResult(true, "Ticket " + ticketId + " permanently deleted.");
                } else {
                    return new OperationResult(false, "Failed to delete ticket file.");
                }
            } catch (SecurityException e) {
                return new OperationResult(false, "Permission denied: unable to delete ticket file.");
            }
        }
    }
}
