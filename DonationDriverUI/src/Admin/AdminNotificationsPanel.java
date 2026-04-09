package Admin;

import Network.Client;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminNotificationsPanel extends JPanel {

    private static final String FILTER_ALL = "All";
    private static final String FILTER_ACTION_REQUIRED = "Action required";
    private static final String FILTER_ACTIVITY = "Activity";

    private DefaultListModel<AdminNotif> notifModel;
    private JList<AdminNotif> notifList;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JLabel connectionStatusLabel;
    private final List<AdminNotif> allNotifs = new ArrayList<>();

    private DefaultListModel<AdminNotif> systemModel;
    private JList<AdminNotif> systemList;
    private final List<AdminNotif> allSystemNotifs = new ArrayList<>();

    private JTable monetaryTable;
    private DefaultTableModel monetaryTableModel;
    private JTable donationBoxesTable;
    private DefaultTableModel donationBoxesTableModel;
    private Timer refreshTimer;

    public AdminNotificationsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(240, 242, 245));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Activity feed and requests across donors and riders");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        header.add(subtitleLabel);

        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        tabs.addTab("System activity", buildSystemActivityTab());
        tabs.addTab("Monetary", buildMonetaryTab());
        tabs.addTab("Boxes", buildBoxesTab());
        add(tabs, BorderLayout.CENTER);

        String[] monetaryColumns = { "Name", "Amount", "Reference No.", "Mode of Payment", "Date" };
        monetaryTableModel = new DefaultTableModel(monetaryColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String[] boxesColumns = { "Name", "Boxes", "Reference No.", "Location", "Date" };
        donationBoxesTableModel = new DefaultTableModel(boxesColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        monetaryTable = createStyledTable(monetaryTableModel);
        donationBoxesTable = createStyledTable(donationBoxesTableModel);

        refreshData();

        refreshTimer = new Timer(5000, e -> refreshData());
        refreshTimer.start();
    }

    public void stopTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(0, 35));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        return table;
    }

    private JPanel buildMonetaryTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JScrollPane sp = new JScrollPane(monetaryTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBoxesTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JScrollPane sp = new JScrollPane(donationBoxesTable);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSystemActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        systemModel = new DefaultListModel<>();
        systemList = new JList<>(systemModel);
        systemList.setCellRenderer(new AdminNotifRenderer());
        systemList.setVisibleRowCount(12);
        systemList.setFixedCellHeight(-1);

        JScrollPane scroll = new JScrollPane(systemList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // fetch data and populate
    public void refreshData() {
        if (!AdminServerWatch.pingOrReturnToLogin(this)) {
            return;
        }
        try {
            List<Ticket> tickets = loadTicketsFromServer();

            monetaryTableModel.setRowCount(0);
            donationBoxesTableModel.setRowCount(0);

            boolean hasMonetary = false;
            boolean hasBoxes = false;
            List<AdminNotif> generated = buildNotifsFromCurrentState(tickets);

            for (Ticket t : tickets) {
            String name = t.userId;
            if (name == null)
                name = "Unknown";
            if (name.contains("@")) {
                name = name.split("@")[0];
            }

            String date = t.createdAt;
            if (date != null && date.length() > 10)
                date = date.substring(0, 10);

            if (t.itemCategory != null && t.itemCategory.toLowerCase().contains("monetary")) {
                hasMonetary = true;
                String amount = "N/A";
                String transactionId = t.ticketId;

                String text = t.notes;
                if (text != null) {
                    // Try old format
                    int amtIdx = text.indexOf("Amount=");
                    if (amtIdx >= 0) {
                        int end = text.indexOf(";", amtIdx);
                        if (end < 0)
                            end = text.length();
                        amount = text.substring(amtIdx + 7, end).trim();
                    } else {
                        // Try new format: "Amount: 100.0"
                        amtIdx = text.indexOf("Amount:");
                        if (amtIdx >= 0) {
                            int end = text.indexOf("|", amtIdx);
                            if (end < 0) end = text.length();
                            amount = text.substring(amtIdx + 7, end).trim();
                        }
                    }
                    
                    int txnIdx = text.indexOf("TransactionId=");
                    if (txnIdx >= 0) {
                        int end = text.indexOf(";", txnIdx);
                        if (end < 0)
                            end = text.length();
                        transactionId = text.substring(txnIdx + 14, end).trim();
                    } else {
                        // Try new format: "ID: 12345"
                        txnIdx = text.indexOf("ID:");
                        if (txnIdx >= 0) {
                            int end = text.indexOf("|", txnIdx);
                            if (end < 0) end = text.length();
                            transactionId = text.substring(txnIdx + 3, end).trim();
                        }
                    }
                }

                monetaryTableModel.addRow(new Object[] {
                        name,
                        amount != null && !amount.startsWith("₱") && !"N/A".equalsIgnoreCase(amount) ? "₱" + amount : amount,
                        transactionId,
                        t.donationDrive != null ? t.donationDrive : "N/A",
                        date
                });
            } else {
                hasBoxes = true;
                // Donation Boxes
                donationBoxesTableModel.addRow(new Object[] {
                        name,
                        t.quantity != null ? t.quantity : "1",
                        t.ticketId,
                        t.pickupLocation != null ? t.pickupLocation : "N/A",
                        date
                });
            }
            }

            if (!hasMonetary) {
                monetaryTableModel.addRow(new Object[] { "-", "-", "-", "-", "-" });
            }
            if (!hasBoxes) {
                donationBoxesTableModel.addRow(new Object[] { "-", "-", "-", "-", "-" });
            }

            // Update the activity feed.
            if (notifModel != null) {
                allNotifs.clear();
                allNotifs.addAll(generated);
                applyNotifFilter();
            }
            if (connectionStatusLabel != null) {
                connectionStatusLabel.setText("");
            }
        } catch (Exception ex) {
            // Offline / transient error: keep last successful data visible.
            if (connectionStatusLabel != null) {
                connectionStatusLabel.setText("Server unreachable — retrying…");
            }
        }

        // System activity is best-effort and should never break the main view.
        refreshSystemActivity();
    }

    private void refreshSystemActivity() {
        if (systemModel == null) return;
        try {
            Client client = Client.getDefault();
            String responseXml = client.getService(false).getServerLogs();
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp == null || !resp.isOk() || resp.message == null) {
                return;
            }
            String raw = resp.message;
            if (raw.trim().isEmpty()) {
                allSystemNotifs.clear();
                allSystemNotifs.add(AdminNotif.info("No system activity yet", "No server log entries.", "", null));
            } else {
                allSystemNotifs.clear();
                String[] lines = raw.split("\\r?\\n");
                // Newest last in logs; show newest first.
                for (int i = lines.length - 1; i >= 0 && allSystemNotifs.size() < 60; i--) {
                    String line = lines[i];
                    if (line == null) continue;
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    AdminNotif n = notifFromServerLogLine(line);
                    if (n != null) allSystemNotifs.add(n);
                }
                if (allSystemNotifs.isEmpty()) {
                    allSystemNotifs.add(AdminNotif.info("No system activity yet", "No parsable server log entries.", "", null));
                }
            }

            systemModel.clear();
            for (AdminNotif n : allSystemNotifs) systemModel.addElement(n);
        } catch (Exception ignored) {
            // keep previous system activity visible
        }
    }

    private AdminNotif notifFromServerLogLine(String line) {
        // Examples:
        // CREATE_TICKET | <id> by <email>
        // UPDATE_TICKET | <id> -> DELIVERED
        // UPDATE_DRIVE_AMOUNT | <drive> +<amount>
        if (line.startsWith("CREATE_TICKET |")) {
            String rest = line.substring("CREATE_TICKET |".length()).trim();
            String title = "New donation created";
            String details = rest;
            String meta = "";
            int byIdx = rest.indexOf(" by ");
            if (byIdx >= 0) {
                String ticketId = rest.substring(0, byIdx).trim();
                String user = rest.substring(byIdx + 4).trim();
                details = shortName(user) + " created a donation request";
                meta = "Ticket: " + ticketId;
            }
            return AdminNotif.info(title, details, meta, null);
        }
        if (line.startsWith("UPDATE_TICKET |")) {
            String rest = line.substring("UPDATE_TICKET |".length()).trim();
            String title = "Donation status updated";
            String details = rest;
            String meta = "";
            int arrow = rest.indexOf("->");
            if (arrow >= 0) {
                String ticketId = rest.substring(0, arrow).trim();
                String status = rest.substring(arrow + 2).trim();
                title = "Status: " + status;
                details = "Ticket updated";
                meta = "Ticket: " + ticketId;
            }
            return AdminNotif.info(title, details, meta, null);
        }
        if (line.startsWith("UPDATE_DRIVE_AMOUNT |")) {
            String rest = line.substring("UPDATE_DRIVE_AMOUNT |".length()).trim();
            return AdminNotif.info("Funds updated", rest, "", null);
        }
        if (line.startsWith("MAINTENANCE_MODE |")) {
            String rest = line.substring("MAINTENANCE_MODE |".length()).trim();
            return AdminNotif.warn("Maintenance mode changed", rest, "", null);
        }
        if (line.startsWith("RIDER_SET_AVAILABLE |")) {
            String rest = line.substring("RIDER_SET_AVAILABLE |".length()).trim();
            return AdminNotif.info("Rider online", shortName(rest) + " is now available", "", null);
        }
        if (line.startsWith("RIDER_SET_UNAVAILABLE |")) {
            String rest = line.substring("RIDER_SET_UNAVAILABLE |".length()).trim();
            return AdminNotif.info("Rider offline", shortName(rest) + " is now unavailable", "", null);
        }

        // fallback: show raw
        return AdminNotif.info("System event", line, "", null);
    }

    private List<Ticket> loadTicketsFromServer() throws Exception {
        List<Ticket> list = new ArrayList<>();
        Client client = Client.getDefault();
        // Admin notifications must pull from the global admin view (all tickets).
        String responseXml = client.readTickets("admin", null);
        Client.Response response = Client.parseResponse(responseXml);
        if (response == null || !response.isOk()) {
            throw new Exception(response != null ? response.message : "readTickets failed");
        }

        String ticketsXml = response.message;
        if (ticketsXml == null || ticketsXml.isEmpty())
            return list;
        ticketsXml = Client.unescapeXml(ticketsXml);
        // Defensive: some responses end up double-escaped in transit.
        if (ticketsXml.contains("&lt;ticket&gt;") || ticketsXml.contains("&amp;lt;ticket&amp;gt;")) {
            ticketsXml = Client.unescapeXml(ticketsXml);
        }

        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0)
                break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0)
                break;

            String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
            Ticket t = new Ticket();
            t.ticketId = extract(ticketXml, "ticketId");
            t.userId = extract(ticketXml, "userId");
            t.riderId = extract(ticketXml, "riderId");
            t.itemCategory = extract(ticketXml, "itemCategory");
            t.quantity = extract(ticketXml, "quantity");
            t.notes = extract(ticketXml, "notes");
            t.status = extract(ticketXml, "status");
            t.createdAt = extract(ticketXml, "createdAt");
            t.pickupLocation = extract(ticketXml, "pickupLocation");
            t.donationDrive = extract(ticketXml, "donationDrive");
            t.deliveryDestination = extract(ticketXml, "deliveryDestination");
            t.deleteReason = extract(ticketXml, "deleteReason");
            t.qualityReason = extract(ticketXml, "qualityReason");
            list.add(t);

            idx = end + "</ticket>".length();
        }
        return list;
    }

    private String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i < 0 || j <= i)
            return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private void applyNotifFilter() {
        if (notifModel == null || notifList == null) return;

        String filter = filterCombo != null ? (String) filterCombo.getSelectedItem() : FILTER_ALL;
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        notifModel.clear();
        for (AdminNotif n : allNotifs) {
            if (FILTER_ACTION_REQUIRED.equals(filter) && !n.actionRequired) continue;
            if (FILTER_ACTIVITY.equals(filter) && n.actionRequired) continue;
            if (q != null && !q.isEmpty()) {
                String hay = (n.title + " " + n.details + " " + n.meta).toLowerCase();
                if (!hay.contains(q)) continue;
            }
            notifModel.addElement(n);
        }

        if (notifModel.isEmpty()) {
            if (allNotifs.isEmpty()) {
                notifModel.addElement(AdminNotif.info("No notifications yet", "Create a donation or update a ticket status to see activity.", ""));
            } else {
                notifModel.addElement(AdminNotif.info("No matches", "Try clearing the filter/search.", ""));
            }
        }
    }

    private List<AdminNotif> buildNotifsFromCurrentState(List<Ticket> tickets) {
        List<AdminNotif> out = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t == null || t.ticketId == null) continue;
            out.add(buildStateNotif(t));
        }

        // Sort: action required first, then newest first (best-effort).
        out.sort((a, b) -> {
            if (a.actionRequired != b.actionRequired) return a.actionRequired ? -1 : 1;
            if (a.createdAt == null && b.createdAt == null) return 0;
            if (a.createdAt == null) return 1;
            if (b.createdAt == null) return -1;
            return b.createdAt.compareTo(a.createdAt);
        });
        return out;
    }

    private AdminNotif buildStateNotif(Ticket t) {
        String st = t.status != null ? t.status.trim().toUpperCase() : "";
        String rider = t.riderId != null && !t.riderId.trim().isEmpty() ? shortName(t.riderId) : null;
        String meta = "Ticket: " + safe(t.ticketId) + (rider != null ? " • Rider: " + rider : "");

        if ("PENDING".equals(st)) {
            return AdminNotif.action("Pending donation request", summarizeTicket(t), meta, t.createdAt);
        }
        if ("CANCELLED".equals(st)) {
            String reason = t.deleteReason != null && !t.deleteReason.trim().isEmpty() ? t.deleteReason.trim() : "No reason provided";
            return AdminNotif.warn("User cancelled donation", summarizeTicket(t) + " • " + reason, meta, t.createdAt);
        }
        if ("REJECTED".equals(st)) {
            String qr = t.qualityReason != null && !t.qualityReason.trim().isEmpty() ? t.qualityReason.trim() : "No reason provided";
            return AdminNotif.warn("Donation rejected", summarizeTicket(t) + " • " + "Reason: " + qr, meta, t.createdAt);
        }
        if ("ACCEPTED".equals(st)) {
            return AdminNotif.info("Pickup accepted", summarizeTicket(t) + (rider != null ? " • " + rider : ""), meta, t.createdAt);
        }
        if ("PICKED_UP".equals(st)) {
            return AdminNotif.info("Donation picked up", summarizeTicket(t) + (rider != null ? " • " + rider : ""), meta, t.createdAt);
        }
        if ("DELIVERED".equals(st)) {
            return AdminNotif.info("Donation delivered", summarizeTicket(t) + (rider != null ? " • " + rider : ""), meta, t.createdAt);
        }

        String title = st.isEmpty() ? "Donation update" : ("Status: " + st);
        return AdminNotif.info(title, summarizeTicket(t) + (rider != null ? " • " + rider : ""), meta, t.createdAt);
    }

    private String summarizeTicket(Ticket t) {
        String who = shortName(t.userId);
        String qty = formatAmountOrQty(t);
        String drive = t.donationDrive != null && !t.donationDrive.trim().isEmpty() ? t.donationDrive.trim() : "No drive";
        String dest = t.deliveryDestination != null && !t.deliveryDestination.trim().isEmpty() ? (" → " + t.deliveryDestination.trim()) : "";
        return who + " • " + qty + " • " + drive + dest;
    }

    private String formatAmountOrQty(Ticket t) {
        String category = t.itemCategory != null ? t.itemCategory : "";
        if (category.toLowerCase().contains("monetary")) {
            String amount = parseAmountFromNotes(t.notes);
            if (amount == null || amount.trim().isEmpty()) return "₱-";
            amount = amount.trim();
            return amount.startsWith("₱") ? amount : "₱" + amount;
        }
        String q = t.quantity != null && !t.quantity.trim().isEmpty() ? t.quantity.trim() : "1";
        return q + " boxes";
    }

    private String parseAmountFromNotes(String notes) {
        if (notes == null) return null;
        int amtIdx = notes.indexOf("Amount=");
        if (amtIdx >= 0) {
            int end = notes.indexOf(";", amtIdx);
            if (end < 0) end = notes.length();
            return notes.substring(amtIdx + 7, end).trim();
        }
        amtIdx = notes.indexOf("Amount:");
        if (amtIdx >= 0) {
            int end = notes.indexOf("|", amtIdx);
            if (end < 0) end = notes.length();
            return notes.substring(amtIdx + 7, end).trim();
        }
        return null;
    }

    private String shortName(String email) {
        if (email == null) return "Unknown";
        int at = email.indexOf("@");
        return at > 0 ? email.substring(0, at) : email;
    }

    private String safe(String s) { return s == null ? "-" : s; }

    private static class AdminNotif {
        final String title;
        final String details;
        final String meta;
        final boolean actionRequired;
        final int level; // 0 info, 1 warn
        final String createdAt;

        private AdminNotif(String title, String details, String meta, boolean actionRequired, int level, String createdAt) {
            this.title = title;
            this.details = details;
            this.meta = meta;
            this.actionRequired = actionRequired;
            this.level = level;
            this.createdAt = createdAt;
        }

        static AdminNotif info(String title, String details, String meta) {
            return new AdminNotif(title, details, meta, false, 0, null);
        }

        static AdminNotif info(String title, String details, String meta, String createdAt) {
            return new AdminNotif(title, details, meta, false, 0, createdAt);
        }

        static AdminNotif warn(String title, String details, String meta, String createdAt) {
            return new AdminNotif(title, details, meta, false, 1, createdAt);
        }

        static AdminNotif action(String title, String details, String meta, String createdAt) {
            return new AdminNotif(title, details, meta, true, 0, createdAt);
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static class AdminNotifRenderer extends JPanel implements ListCellRenderer<AdminNotif> {
        private final JLabel title = new JLabel();
        private final JLabel details = new JLabel();
        private final JLabel meta = new JLabel();

        AdminNotifRenderer() {
            setLayout(new BorderLayout(8, 2));
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            title.setFont(new Font("SansSerif", Font.BOLD, 13));
            details.setFont(new Font("SansSerif", Font.PLAIN, 12));
            details.setForeground(new Color(70, 70, 70));
            meta.setFont(new Font("SansSerif", Font.PLAIN, 11));
            meta.setForeground(new Color(120, 120, 120));

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.add(title);
            center.add(Box.createVerticalStrut(2));
            center.add(details);
            center.add(Box.createVerticalStrut(2));
            center.add(meta);
            add(center, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends AdminNotif> list, AdminNotif value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            if (value == null) value = AdminNotif.info("-", "-", "-");

            title.setText(value.title);
            details.setText(value.details);
            meta.setText(value.meta);

            Color bg = isSelected ? new Color(232, 240, 254) : Color.WHITE;
            setBackground(bg);
            setOpaque(true);

            if (value.actionRequired) {
                title.setForeground(new Color(180, 90, 0));
            } else if (value.level == 1) {
                title.setForeground(new Color(160, 0, 0));
            } else {
                title.setForeground(new Color(20, 35, 100));
            }

            return this;
        }
    }

    private static class Ticket {
        String ticketId;
        String userId;
        String riderId;
        String itemCategory;
        String quantity;
        String notes;
        String status;
        String createdAt;
        String pickupLocation;
        String donationDrive;
        String deliveryDestination;
        String deleteReason;
        String qualityReason;
    }
}
