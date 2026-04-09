package Network;

import Util.RmiClientConfig;

import javax.swing.JOptionPane;
import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {

    private static final String DEFAULT_HOST = RmiClientConfig.DEFAULT_HOST;
    private static final int DEFAULT_PORT = RmiClientConfig.DEFAULT_PORT;

    private static Client instance;
    private volatile String host;
    private volatile int port;

    private transient DonationDriverService service;

    /** Prevents multiple concurrent "return to login" flows from pollers (dashboard / rider). */
    private static final AtomicBoolean disconnectedSessionHandling = new AtomicBoolean(false);

    private static final Object ERROR_DIALOG_LOCK = new Object();
    private static volatile long lastErrorDialogAtMs;
    private static final long ERROR_DIALOG_COOLDOWN_MS = 4000L;

    private Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private static void showErrorDialogWithCooldown(String message, String title) {
        long now = System.currentTimeMillis();
        synchronized (ERROR_DIALOG_LOCK) {
            if (now - lastErrorDialogAtMs < ERROR_DIALOG_COOLDOWN_MS) {
                return;
            }
            lastErrorDialogAtMs = now;
        }
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static synchronized Client getInstance() {
        if (instance == null) {
            RmiClientConfig.Endpoint ep = RmiClientConfig.load();
            instance = new Client(ep.host, ep.port);
        }
        return instance;
    }

    /**
     * Point the client at a new registry endpoint and drop any cached stub.
     * Does not persist; use {@link RmiClientConfig#save} from settings when the user saves.
     */
    public static synchronized void configure(String newHost, int newPort) {
        String h = (newHost == null || newHost.trim().isEmpty()) ? DEFAULT_HOST : newHost.trim();
        int p = (newPort < 1 || newPort > 65535) ? DEFAULT_PORT : newPort;
        if (instance == null) {
            instance = new Client(h, p);
        } else {
            instance.host = h;
            instance.port = p;
            clearCachedService();
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * Try a single lookup and {@link DonationDriverService#ping()} without mutating {@link #getInstance()}.
     */
    public static boolean tryPingEndpoint(String endpointHost, int endpointPort) {
        if (endpointHost == null || endpointHost.trim().isEmpty() || endpointPort < 1 || endpointPort > 65535) {
            return false;
        }
        try {
            String url = "rmi://" + endpointHost.trim() + ":" + endpointPort + "/DonationDriverService";
            DonationDriverService s = (DonationDriverService) Naming.lookup(url);
            String pong = s.ping();
            return pong != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static Client getDefault() {
        return getInstance();
    }

    public String sendRequest(String requestXml) throws IOException {
        return sendRequest(requestXml, true);
    }

    public String sendRequest(String requestXml, boolean notifyOnFailure) throws IOException {
        if (requestXml == null) return null;

        String action = extractTagValue(requestXml, "action");
        if (action == null) {
            throw new IOException("Missing <action> in request.");
        }

        IOException last = null;
        for (int attempt = 0; attempt < 2; attempt++) {
            boolean isRetry = attempt > 0;
            DonationDriverService svc;
            try {
                // On retry, don't spam dialogs; the second failure will surface.
                svc = getService(notifyOnFailure && !isRetry);
            } catch (IOException e) {
                last = e;
                clearCachedService();
                continue;
            }

            try {
                switch (action) {
                case "LOGIN": {
                    String email = unescapeXml(extractTagValue(requestXml, "email"));
                    String password = unescapeXml(extractTagValue(requestXml, "password"));
                    return svc.login(email, password);
                }
                case "REGISTER": {
                    String firstName = extractTagValue(requestXml, "firstName");
                    if (firstName == null) {
                        String email = unescapeXml(extractTagValue(requestXml, "email"));
                        String password = unescapeXml(extractTagValue(requestXml, "password"));
                        return svc.register(email, password);
                    }

                    String lastName = unescapeXml(extractTagValue(requestXml, "lastName"));
                    String middleName = unescapeXml(extractTagValue(requestXml, "middleName"));
                    String dateOfBirth = unescapeXml(extractTagValue(requestXml, "dateOfBirth"));
                    String address = unescapeXml(extractTagValue(requestXml, "address"));
                    String phone = unescapeXml(extractTagValue(requestXml, "phone"));
                    String email = unescapeXml(extractTagValue(requestXml, "email"));
                    String password = unescapeXml(extractTagValue(requestXml, "password"));
                    String role = unescapeXml(extractTagValue(requestXml, "role"));

                    return svc.register(
                            unescapeXml(firstName),
                            lastName == null ? "" : lastName,
                            middleName == null ? "" : middleName,
                            dateOfBirth == null ? "" : dateOfBirth,
                            address == null ? "" : address,
                            phone == null ? "" : phone,
                            email,
                            password,
                            role == null ? "" : role
                    );
                }
                case "UPDATE_USER_ROLE": {
                    String email = unescapeXml(extractTagValue(requestXml, "email"));
                    String role = unescapeXml(extractTagValue(requestXml, "role"));
                    return svc.updateUserRole(email, role);
                }
                case "CREATE_TICKET": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));

                    // Optional 3-arg overload fields
                    String typeRaw = extractTagValue(requestXml, "type");
                    String detailsRaw = extractTagValue(requestXml, "details");
                    if (typeRaw != null && detailsRaw != null) {
                        return svc.createTicket(userId, unescapeXml(typeRaw), unescapeXml(detailsRaw));
                    }

                    String itemCategory = unescapeXml(extractTagValue(requestXml, "itemCategory"));
                    int quantity = parseIntOrZero(extractTagValue(requestXml, "quantity"));
                    String condition = unescapeXml(extractTagValue(requestXml, "condition"));
                    String expirationDate = unescapeXml(extractTagValue(requestXml, "expirationDate"));
                    String pickupDateTime = unescapeXml(extractTagValue(requestXml, "pickupDateTime"));
                    String pickupLocation = unescapeXml(extractTagValue(requestXml, "pickupLocation"));
                    String photoPath = unescapeXml(extractTagValue(requestXml, "photoPath"));
                    String notes = unescapeXml(extractTagValue(requestXml, "details"));
                    String photoBase64 = extractCdataTag(requestXml, "photoBase64");

                    String donationDriveRaw = extractTagValue(requestXml, "donationDrive");
                    String deliveryDestinationRaw = extractTagValue(requestXml, "deliveryDestination");
                    boolean hasDonationDrive = donationDriveRaw != null;
                    boolean hasDeliveryDestination = deliveryDestinationRaw != null;

                    if (hasDonationDrive || hasDeliveryDestination) {
                        String donationDrive = hasDonationDrive ? unescapeXml(donationDriveRaw) : "";
                        String deliveryDestination = hasDeliveryDestination ? unescapeXml(deliveryDestinationRaw) : "";
                        return svc.createTicket(
                                userId,
                                itemCategory,
                                quantity,
                                condition,
                                expirationDate,
                                pickupDateTime,
                                pickupLocation,
                                photoPath,
                                notes,
                                donationDrive,
                                deliveryDestination,
                                photoBase64
                        );
                    }

                    return svc.createTicket(
                            userId,
                            itemCategory,
                            quantity,
                            condition,
                            expirationDate,
                            pickupDateTime,
                            pickupLocation,
                            photoPath,
                            notes,
                            photoBase64
                    );
                }
                case "READ_TICKETS": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String status = unescapeXml(extractTagValue(requestXml, "status"));
                    if (status == null) {
                        return svc.readTickets(userId);
                    }
                    return svc.readTickets(userId, status);
                }
                case "UPDATE_TICKET": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String ticketId = unescapeXml(extractTagValue(requestXml, "ticketId"));
                    String status = unescapeXml(extractTagValue(requestXml, "status"));
                    String pickupDateTime = unescapeXml(extractTagValue(requestXml, "pickupDateTime"));

                    if (status == null && pickupDateTime != null) {
                        return svc.updateTicketPickupTime(userId, ticketId, pickupDateTime);
                    }

                    String qualityStatus = unescapeXml(extractTagValue(requestXml, "qualityStatus"));
                    String qualityReason = unescapeXml(extractTagValue(requestXml, "qualityReason"));

                    if (qualityStatus != null || qualityReason != null) {
                        return svc.updateTicket(
                                userId,
                                ticketId,
                                status,
                                qualityStatus == null ? "" : qualityStatus,
                                qualityReason == null ? "" : qualityReason
                        );
                    }

                    return svc.updateTicket(userId, ticketId, status);
                }
                case "DELETE_TICKET": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String ticketId = unescapeXml(extractTagValue(requestXml, "ticketId"));
                    String deleteReason = unescapeXml(extractTagValue(requestXml, "deleteReason"));
                    if (deleteReason == null || deleteReason.trim().isEmpty()) {
                        return svc.deleteTicket(userId, ticketId);
                    }
                    return svc.deleteTicket(userId, ticketId, deleteReason);
                }
                case "PERMANENT_DELETE_TICKET": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String ticketId = unescapeXml(extractTagValue(requestXml, "ticketId"));
                    return svc.permanentDeleteTicket(userId, ticketId);
                }
                case "CREATE_DONATION_DRIVE": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String title = unescapeXml(extractTagValue(requestXml, "title"));
                    String description = unescapeXml(extractTagValue(requestXml, "description"));
                    String targetAmount = extractTagValue(requestXml, "targetAmount");
                    double amount = parseDoubleOrZero(targetAmount);
                    String drivePhoto = extractCdataTag(requestXml, "photoBase64");
                    return svc.createDonationDrive(userId, title, description, String.valueOf(amount), drivePhoto != null ? drivePhoto : "");
                }
                case "READ_DONATION_DRIVES": {
                    return svc.readDonationDrives();
                }
                case "DELETE_DONATION_DRIVE": {
                    // Not currently used by UI socket client, keep for completeness.
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    String title = unescapeXml(extractTagValue(requestXml, "driveTitle"));
                    return svc.deleteDonationDrive(userId, title);
                }
                case "UPDATE_DRIVE_AMOUNT": {
                    String driveTitle = unescapeXml(extractTagValue(requestXml, "driveTitle"));
                    double amount = parseDoubleOrZero(extractTagValue(requestXml, "amount"));
                    return svc.updateDriveAmount(driveTitle, amount);
                }
                case "RIDER_SET_AVAILABLE": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    return svc.setRiderAvailable(userId);
                }
                case "RIDER_SET_UNAVAILABLE": {
                    String userId = unescapeXml(extractTagValue(requestXml, "userId"));
                    return svc.setRiderUnavailable(userId);
                }
                case "GET_SERVER_LOGS": {
                    return svc.getServerLogs();
                }
                case "SET_SERVER_MAINTENANCE_MODE": {
                    String enabledStr = unescapeXml(extractTagValue(requestXml, "enabled"));
                    boolean enabled = enabledStr != null && enabledStr.equalsIgnoreCase("true");
                    return svc.setServerMaintenanceMode(enabled);
                }
                case "PING": {
                    return svc.ping();
                }
                case "LOGOUT": {
                    String logoutEmail = unescapeXml(extractTagValue(requestXml, "email"));
                    return svc.logout(logoutEmail);
                }
                default:
                    throw new IOException("Unsupported action: " + action);
            }
            } catch (RemoteException e) {
                // Common after restarting server in IDE: stub is stale. Drop it and retry once.
                last = new IOException("RMI call failed", e);
                clearCachedService();
                if (!isRetry) {
                    continue;
                }
                if (notifyOnFailure) {
                    showErrorDialogWithCooldown(
                            "Server connection lost. Please verify the server is running.",
                            "Network Error");
                }
                throw last;
            }
        }
        if (last != null) throw last;
        throw new IOException("Request failed.");
    }

    /** Drop cached RMI stub so the next call performs a fresh registry lookup. */
    public static void clearCachedService() {
        Client c = instance;
        if (c != null) {
            c.service = null;
        }
    }

    public static boolean tryAcquireDisconnectedSessionHandling() {
        return disconnectedSessionHandling.compareAndSet(false, true);
    }

    public static void resetDisconnectedSessionHandling() {
        disconnectedSessionHandling.set(false);
    }

    public DonationDriverService getService() throws IOException {
        return getService(true);
    }

    public DonationDriverService getService(boolean notifyLookupFailure) throws IOException {
        if (service != null) return service;
        try {
            String url = "rmi://" + host + ":" + port + "/DonationDriverService";
            service = (DonationDriverService) Naming.lookup(url);
            return service;
        } catch (Exception e) {
            if (notifyLookupFailure) {
                showErrorDialogWithCooldown(
                        "Cannot contact RMI server at " + host + ":" + port + ".\nPlease make sure the server is started.",
                        "Connection Error");
            }
            throw new IOException("Cannot contact RMI server at " + host + ":" + port, e);
        }
    }

    public String login(String email, String password) throws IOException {
        String request = "<request><action>LOGIN</action><userId></userId>"
                + "<email>" + escapeXml(email) + "</email>"
                + "<password>" + escapeXml(password) + "</password></request>";
        return sendRequest(request);
    }

    public String register(String email, String password) throws IOException {
        String request = "<request><action>REGISTER</action><userId></userId>"
                + "<email>" + escapeXml(email) + "</email>"
                + "<password>" + escapeXml(password) + "</password></request>";
        return sendRequest(request);
    }

    public String register(String firstName, String lastName, String middleName,
            String dateOfBirth, String address, String phone,
            String email, String password, String role) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>REGISTER</action><userId></userId>");
        request.append("<firstName>").append(escapeXml(firstName)).append("</firstName>");
        request.append("<lastName>").append(escapeXml(lastName)).append("</lastName>");
        request.append("<middleName>").append(escapeXml(middleName != null ? middleName : "")).append("</middleName>");
        request.append("<dateOfBirth>").append(escapeXml(dateOfBirth != null ? dateOfBirth : ""))
                .append("</dateOfBirth>");
        request.append("<address>").append(escapeXml(address != null ? address : "")).append("</address>");
        request.append("<phone>").append(escapeXml(phone != null ? phone : "")).append("</phone>");
        request.append("<email>").append(escapeXml(email)).append("</email>");
        request.append("<password>").append(escapeXml(password)).append("</password>");
        request.append("<role>").append(escapeXml(role)).append("</role>");
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String createTicket(String userId, String type, String details) throws IOException {
        String request = "<request><action>CREATE_TICKET</action>"
                + "<userId>" + escapeXml(userId) + "</userId>"
                + "<type>" + escapeXml(type) + "</type>"
                + "<details>" + escapeXml(details) + "</details></request>";
        return sendRequest(request);
    }

    public String createTicket(
            String userId,
            String itemCategory,
            int quantity,
            String condition,
            String expirationDate,
            String pickupDateTime,
            String pickupLocation,
            String photoPath,
            String notes,
            String photoBase64) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>CREATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>");
        request.append("<quantity>").append(quantity).append("</quantity>");
        request.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
        request.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                .append("</expirationDate>");
        request.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : ""))
                .append("</pickupDateTime>");
        request.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                .append("</pickupLocation>");
        request.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
        request.append("<details>").append(escapeXml(notes != null ? notes : "")).append("</details>");
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            request.append("<photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String createTicket(
            String userId,
            String itemCategory,
            int quantity,
            String condition,
            String expirationDate,
            String pickupDateTime,
            String pickupLocation,
            String photoPath,
            String notes,
            String donationDrive,
            String deliveryDestination,
            String photoBase64) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>CREATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>");
        request.append("<quantity>").append(quantity).append("</quantity>");
        request.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
        request.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : ""))
                .append("</expirationDate>");
        request.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : ""))
                .append("</pickupDateTime>");
        request.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : ""))
                .append("</pickupLocation>");
        request.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
        request.append("<details>").append(escapeXml(notes != null ? notes : "")).append("</details>");
        if (donationDrive != null && !donationDrive.isEmpty()) {
            request.append("<donationDrive>").append(escapeXml(donationDrive)).append("</donationDrive>");
        }
        if (deliveryDestination != null && !deliveryDestination.isEmpty()) {
            request.append("<deliveryDestination>").append(escapeXml(deliveryDestination))
                    .append("</deliveryDestination>");
        }
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            request.append("<photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String updateUserRole(String email, String newRole) throws IOException {
        String msg = "<request><action>UPDATE_USER_ROLE</action>" +
                "<email>" + escapeXml(email) + "</email>" +
                "<role>" + escapeXml(newRole) + "</role>" +
                "</request>";
        return sendRequest(msg);
    }

    public String readTickets(String userId) throws IOException {
        return readTickets(userId, null);
    }

    public String readTickets(String userId, String status) throws IOException {
        return readTickets(userId, status, true);
    }

    public String readTicketsSilently(String userId, String status) throws IOException {
        return readTickets(userId, status, false);
    }

    private String readTickets(String userId, String status, boolean notifyOnFailure) throws IOException {
        String request = "<request><action>READ_TICKETS</action>"
                + "<userId>" + escapeXml(userId) + "</userId>";
        if (status != null) {
            request += "<status>" + escapeXml(status) + "</status>";
        }
        request += "</request>";
        return sendRequest(request, notifyOnFailure);
    }

    public String updateTicket(String userId, String ticketId, String status) throws IOException {
        String request = "<request><action>UPDATE_TICKET</action>"
                + "<userId>" + escapeXml(userId) + "</userId>"
                + "<ticketId>" + escapeXml(ticketId) + "</ticketId>"
                + "<status>" + escapeXml(status) + "</status></request>";
        return sendRequest(request);
    }

    public String updateTicket(String userId, String ticketId, String status, String qualityStatus,
            String qualityReason) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>UPDATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
        request.append("<status>").append(escapeXml(status)).append("</status>");
        if (qualityStatus != null && !qualityStatus.trim().isEmpty()) {
            request.append("<qualityStatus>").append(escapeXml(qualityStatus.trim())).append("</qualityStatus>");
        }
        if (qualityReason != null && !qualityReason.trim().isEmpty()) {
            request.append("<qualityReason>").append(escapeXml(qualityReason.trim())).append("</qualityReason>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    /**
     * Update pickup date and time only (no status change). Format: {@code yyyy-MM-dd HH:mm} (e.g. 2026-02-20 14:30).
     */
    public String updateTicketPickupTime(String userId, String ticketId, String pickupDateTime) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>UPDATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
        if (pickupDateTime != null && !pickupDateTime.trim().isEmpty()) {
            request.append("<pickupDateTime>").append(escapeXml(pickupDateTime.trim())).append("</pickupDateTime>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String deleteTicket(String userId, String ticketId) throws IOException {
        String request = "<request><action>DELETE_TICKET</action>"
                + "<userId>" + escapeXml(userId) + "</userId>"
                + "<ticketId>" + escapeXml(ticketId) + "</ticketId></request>";
        return sendRequest(request);
    }

    // Overload method for optional reason from client when cancelling a ticket
    public String deleteTicket(String userId, String ticketId, String reason) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>DELETE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
        if (reason != null && !reason.trim().isEmpty()) {
            request.append("<deleteReason>").append(escapeXml(reason.trim())).append("</deleteReason>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String permanentDeleteTicket(String adminUserId, String ticketId) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>PERMANENT_DELETE_TICKET</action>");
        request.append("<userId>").append(escapeXml(adminUserId)).append("</userId>");
        request.append("<ticketId>").append(escapeXml(ticketId)).append("</ticketId>");
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String createDonationDrive(String userId, String title, String description, String targetAmount, String photoBase64)
            throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>CREATE_DONATION_DRIVE</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<title>").append(escapeXml(title)).append("</title>");
        request.append("<description>").append(escapeXml(description != null ? description : ""))
                .append("</description>");
        request.append("<targetAmount>").append(escapeXml(targetAmount)).append("</targetAmount>");
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            request.append("<photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String readDonationDrives() throws IOException {
        return sendRequest("<request><action>READ_DONATION_DRIVES</action><userId></userId></request>");
    }

    public String readDonationDrivesSilently() throws IOException {
        return sendRequest("<request><action>READ_DONATION_DRIVES</action><userId></userId></request>", false);
    }

    public String updateDriveAmount(String driveTitle, double amount) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>UPDATE_DRIVE_AMOUNT</action>");
        request.append("<driveTitle>").append(escapeXml(driveTitle)).append("</driveTitle>");
        request.append("<amount>").append(amount).append("</amount>");
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public static Response parseResponse(String responseXml) {
        if (responseXml == null || responseXml.isEmpty()) {
            return null;
        }
        String status = extractTagValue(responseXml, "status");
        String message = extractCdataTag(responseXml, "message");
        String role = extractCdataTag(responseXml, "role");
        return new Response(status != null ? status : "", message != null ? message : "", role != null ? role : "");
    }

    private static String extractTagValue(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i == -1 || j == -1 || j <= i) {
            return null;
        }
        return xml.substring(i + open.length(), j).trim();
    }

    private static String extractCdataTag(String xml, String tag) {
        String openC = "<" + tag + "><![CDATA[";
        String closeC = "]]></" + tag + ">";
        int i = xml.indexOf(openC);
        if (i == -1) {
            // Fallback: not CDATA-wrapped.
            return extractTagValue(xml, tag);
        }
        int j = xml.indexOf(closeC, i + openC.length());
        if (j == -1) return null;
        return xml.substring(i + openC.length(), j);
    }

    private static int parseIntOrZero(String s) {
        try {
            if (s == null) return 0;
            return Integer.parseInt(s.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static double parseDoubleOrZero(String s) {
        try {
            if (s == null) return 0.0;
            return Double.parseDouble(s.trim());
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    public static String escapeXml(String s) {
        if (s == null)
            return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /** Reverse of escapeXml so XML embedded in a response message can be parsed. */
    public static String unescapeXml(String s) {
        if (s == null)
            return "";
        // Handle normal and double-escaped XML (e.g. "&amp;lt;ticket&amp;gt;").
        String out = s;
        for (int i = 0; i < 3; i++) {
            String prev = out;
            // Decode '&' first so double-escaped sequences become decodable in the same pass.
            out = out.replace("&amp;", "&")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'");
            if (out.equals(prev)) {
                break;
            }
        }
        return out;
    }

    public String ping() throws IOException {
        return sendRequest("<request><action>PING</action><userId></userId></request>");
    }

    /** Live server check without showing {@code Client}'s default connection error dialog. */
    public String pingSilent() throws IOException {
        return sendRequest("<request><action>PING</action><userId></userId></request>", false);
    }

    public String logout(String email) throws IOException {
        String request = "<request><action>LOGOUT</action><userId></userId>"
                + "<email>" + escapeXml(email != null ? email : "") + "</email></request>";
        return sendRequest(request);
    }

    /** Best-effort session cleanup; ignores failures (e.g. server already down). */
    public void logoutQuiet(String email) {
        try {
            String request = "<request><action>LOGOUT</action><userId></userId>"
                    + "<email>" + escapeXml(email != null ? email : "") + "</email></request>";
            sendRequest(request, false);
        } catch (IOException ignored) {
        }
    }

    /** Mark the rider as available (go online). User must be logged in. */
    public String setRiderAvailable(String userId) throws IOException {
        String request = "<request><action>RIDER_SET_AVAILABLE</action>"
                + "<userId>" + escapeXml(userId != null ? userId : "") + "</userId></request>";
        return sendRequest(request);
    }

    /** Mark the rider as unavailable (go offline). */
    public String setRiderUnavailable(String userId) throws IOException {
        String request = "<request><action>RIDER_SET_UNAVAILABLE</action>"
                + "<userId>" + escapeXml(userId != null ? userId : "") + "</userId></request>";
        return sendRequest(request);
    }

    public static class Response {
        public final String status;
        public final String message;
        public final String role;

        public Response(String status, String message) {
            this(status, message, "");
        }

        public Response(String status, String message, String role) {
            this.status = status != null ? status : "";
            this.message = message != null ? message : "";
            this.role = role != null ? role : "";
        }

        public boolean isOk() {
            return "OK".equalsIgnoreCase(status);
        }
    }

    public static void main(String[] args) {
        Client client = getDefault();

        try {
            String donorId = "donor@gmail.com";
            String responseXml = client.readTickets(donorId);
            System.out.println("READ_TICKETS (donor) response: " + responseXml);

            Response r = parseResponse(responseXml);
            if (r != null) {
                System.out.println("Parsed status: " + r.status);
                System.out.println("Tickets XML from message:");
                System.out.println(r.message);
            }

            String responsePending = client.readTickets("", "PENDING");
            System.out.println("READ_TICKETS (all PENDING) response: " + responsePending);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
