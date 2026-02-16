package Network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5267;
    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final String host;
    private final int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static Client getDefault() {
        return new Client(DEFAULT_HOST, DEFAULT_PORT);
    }

    public String sendRequest(String requestXml) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            out.write(requestXml);
            out.newLine();
            out.flush();

            String responseXml = in.readLine();
            if (responseXml == null) {
                return null;
            }
            return responseXml.trim();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
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
                           String email, String password) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>REGISTER</action><userId></userId>");
        request.append("<firstName>").append(escapeXml(firstName)).append("</firstName>");
        request.append("<lastName>").append(escapeXml(lastName)).append("</lastName>");
        request.append("<middleName>").append(escapeXml(middleName != null ? middleName : "")).append("</middleName>");
        request.append("<dateOfBirth>").append(escapeXml(dateOfBirth != null ? dateOfBirth : "")).append("</dateOfBirth>");
        request.append("<address>").append(escapeXml(address != null ? address : "")).append("</address>");
        request.append("<phone>").append(escapeXml(phone != null ? phone : "")).append("</phone>");
        request.append("<email>").append(escapeXml(email)).append("</email>");
        request.append("<password>").append(escapeXml(password)).append("</password>");
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
            String photoBase64
    ) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>CREATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>");
        request.append("<quantity>").append(quantity).append("</quantity>");
        request.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
        request.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : "")).append("</expirationDate>");
        request.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : "")).append("</pickupDateTime>");
        request.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : "")).append("</pickupLocation>");
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
            String photoBase64
    ) throws IOException {
        StringBuilder request = new StringBuilder();
        request.append("<request><action>CREATE_TICKET</action>");
        request.append("<userId>").append(escapeXml(userId)).append("</userId>");
        request.append("<itemCategory>").append(escapeXml(itemCategory)).append("</itemCategory>");
        request.append("<quantity>").append(quantity).append("</quantity>");
        request.append("<condition>").append(escapeXml(condition != null ? condition : "")).append("</condition>");
        request.append("<expirationDate>").append(escapeXml(expirationDate != null ? expirationDate : "")).append("</expirationDate>");
        request.append("<pickupDateTime>").append(escapeXml(pickupDateTime != null ? pickupDateTime : "")).append("</pickupDateTime>");
        request.append("<pickupLocation>").append(escapeXml(pickupLocation != null ? pickupLocation : "")).append("</pickupLocation>");
        request.append("<photoPath>").append(escapeXml(photoPath != null ? photoPath : "")).append("</photoPath>");
        request.append("<details>").append(escapeXml(notes != null ? notes : "")).append("</details>");
        if (donationDrive != null && !donationDrive.isEmpty()) {
            request.append("<donationDrive>").append(escapeXml(donationDrive)).append("</donationDrive>");
        }
        if (deliveryDestination != null && !deliveryDestination.isEmpty()) {
            request.append("<deliveryDestination>").append(escapeXml(deliveryDestination)).append("</deliveryDestination>");
        }
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            request.append("<photoBase64><![CDATA[").append(photoBase64).append("]]></photoBase64>");
        }
        request.append("</request>");
        return sendRequest(request.toString());
    }

    public String readTickets(String userId) throws IOException {
        return readTickets(userId, null);
    }

    public String readTickets(String userId, String status) throws IOException {
        String request = "<request><action>READ_TICKETS</action>"
                + "<userId>" + escapeXml(userId) + "</userId>";
        if (status != null) {
            request += "<status>" + escapeXml(status) + "</status>";
        }
        request += "</request>";
        return sendRequest(request);
    }

    public String updateTicket(String userId, String ticketId, String status) throws IOException {
        String request = "<request><action>UPDATE_TICKET</action>"
                + "<userId>" + escapeXml(userId) + "</userId>"
                + "<ticketId>" + escapeXml(ticketId) + "</ticketId>"
                + "<status>" + escapeXml(status) + "</status></request>";
        return sendRequest(request);
    }

    public String updateTicket(String userId, String ticketId, String status, String qualityStatus, String qualityReason) throws IOException {
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

    public String deleteTicket(String userId, String ticketId) throws IOException {
        String request = "<request><action>DELETE_TICKET</action>"
                + "<userId>" + escapeXml(userId) + "</userId>"
                + "<ticketId>" + escapeXml(ticketId) + "</ticketId></request>";
        return sendRequest(request);
    }

    //Overload method for optional reason from client when cancelling a ticket
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

    public static Response parseResponse(String responseXml) {
        if (responseXml == null || responseXml.isEmpty()) {
            return null;
        }
        String status = extractTagValue(responseXml, "status");
        String message = extractTagValue(responseXml, "message");
        return new Response(status != null ? status : "", message != null ? message : "");
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

    public static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    /** Reverse of escapeXml so XML embedded in a response message can be parsed. */
    public static String unescapeXml(String s) {
        if (s == null) return "";
        return s.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&");
    }

    public String ping() throws IOException {
        return sendRequest("<request><action>PING</action><userId></userId></request>");
    }

    public static class Response {
        public final String status;
        public final String message;

        public Response(String status, String message) {
            this.status = status != null ? status : "";
            this.message = message != null ? message : "";
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

