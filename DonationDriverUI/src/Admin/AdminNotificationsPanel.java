package Admin;

import Network.Client;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminNotificationsPanel extends JPanel {

    private static final String FILTER_ALL = "All";
    private static final String FILTER_ACTION_REQUIRED = "Action required";
    private static final String FILTER_ACTIVITY = "Activity";

    private DefaultListModel<AdminNotif> notifModel;
    private JList<AdminNotif> notifList;
    private JTextField searchField;
    private JComboBox<String> filterCombo;

    private JTable monetaryTable;
    private DefaultTableModel monetaryTableModel;
    private JTable donationBoxesTable;
    private DefaultTableModel donationBoxesTableModel;
    private Timer refreshTimer;

    // Used to detect changes between refreshes (status updates, reschedules, etc).
    private final Map<String, TicketSnapshot> lastSnapshotByTicketId = new HashMap<>();

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
        tabs.addTab("Activity", buildActivityTab());
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

    private JPanel buildActivityTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setOpaque(true);
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        top.add(new JLabel("Filter:"));
        filterCombo = new JComboBox<>(new String[] { FILTER_ALL, FILTER_ACTION_REQUIRED, FILTER_ACTIVITY });
        filterCombo.addActionListener(e -> applyNotifFilter());
        top.add(filterCombo);

        top.add(new JLabel("Search:"));
        searchField = new JTextField(24);
        searchField.setToolTipText("Search by ticket ID, donor, rider, drive, destination");
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { applyNotifFilter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { applyNotifFilter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { applyNotifFilter(); }
        });
        top.add(searchField);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            if (filterCombo != null) filterCombo.setSelectedItem(FILTER_ALL);
            if (searchField != null) searchField.setText("");
            applyNotifFilter();
        });
        top.add(clearBtn);

        panel.add(top, BorderLayout.NORTH);

        notifModel = new DefaultListModel<>();
        notifList = new JList<>(notifModel);
        notifList.setCellRenderer(new AdminNotifRenderer());
        notifList.setVisibleRowCount(12);
        notifList.setFixedCellHeight(-1);

        JScrollPane scroll = new JScrollPane(notifList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
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

    // fetch data and populate
    public void refreshData() {
        if (!AdminServerWatch.pingOrReturnToLogin(this)) {
            return;
        }
        monetaryTableModel.setRowCount(0);
        donationBoxesTableModel.setRowCount(0);

        List<Ticket> tickets = loadTicketsFromServer();

        boolean hasMonetary = false;
        boolean hasBoxes = false;
        List<AdminNotif> generated = generateNotifications(tickets);

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
            notifModel.clear();
            if (generated.isEmpty()) {
                notifModel.addElement(AdminNotif.info("No recent activity", "No ticket changes detected yet.", ""));
            } else {
                for (AdminNotif n : generated) notifModel.addElement(n);
            }
            applyNotifFilter();
        }
    }

    private List<Ticket> loadTicketsFromServer() {
        List<Ticket> list = new ArrayList<>();
        try {
            Client client = Client.getDefault();
            // Admin notifications must pull from the global admin view (all tickets).
            String responseXml = client.readTickets("admin", null);
            Client.Response response = Client.parseResponse(responseXml);
            if (response == null || !response.isOk()) {
                return list;
            }

            String ticketsXml = response.message;
            if (ticketsXml == null || ticketsXml.isEmpty())
                return list;
            ticketsXml = Client.unescapeXml(ticketsXml);

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
                t.pickupDateTime = extract(ticketXml, "pickupDateTime");
                t.donationDrive = extract(ticketXml, "donationDrive");
                t.deliveryDestination = extract(ticketXml, "deliveryDestination");
                t.deleteReason = extract(ticketXml, "deleteReason");
                t.qualityReason = extract(ticketXml, "qualityReason");
                list.add(t);

                idx = end + "</ticket>".length();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        // We filter by rebuilding a filtered view in-place (simple + reliable for small feeds).
        // Keep the most recent generated list by storing it as client properties on the list.
        Object allObj = notifList.getClientProperty("allNotifs");
        @SuppressWarnings("unchecked")
        List<AdminNotif> all = allObj instanceof List ? (List<AdminNotif>) allObj : null;
        if (all == null) {
            all = new ArrayList<>();
            for (int i = 0; i < notifModel.size(); i++) all.add(notifModel.get(i));
            notifList.putClientProperty("allNotifs", all);
        }

        String filter = filterCombo != null ? (String) filterCombo.getSelectedItem() : FILTER_ALL;
        String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        notifModel.clear();
        for (AdminNotif n : all) {
            if (FILTER_ACTION_REQUIRED.equals(filter) && !n.actionRequired) continue;
            if (FILTER_ACTIVITY.equals(filter) && n.actionRequired) continue;
            if (q != null && !q.isEmpty()) {
                String hay = (n.title + " " + n.details + " " + n.meta).toLowerCase();
                if (!hay.contains(q)) continue;
            }
            notifModel.addElement(n);
        }

        if (notifModel.isEmpty()) {
            notifModel.addElement(AdminNotif.info("No matches", "Try clearing the filter/search.", ""));
        }
    }

    private List<AdminNotif> generateNotifications(List<Ticket> tickets) {
        List<AdminNotif> out = new ArrayList<>();

        // Build current snapshot map (and generate diffs vs previous snapshot).
        Map<String, TicketSnapshot> next = new HashMap<>();
        for (Ticket t : tickets) {
            if (t == null || t.ticketId == null) continue;
            TicketSnapshot snap = TicketSnapshot.from(t);
            next.put(t.ticketId, snap);

            TicketSnapshot prev = lastSnapshotByTicketId.get(t.ticketId);
            if (prev == null) {
                // New ticket seen
                out.add(buildNewTicketNotif(t));
            } else {
                if (!eq(prev.status, snap.status)) {
                    out.add(buildStatusChangeNotif(t, prev.status, snap.status));
                } else if (!eq(prev.pickupDateTime, snap.pickupDateTime) && snap.pickupDateTime != null && !snap.pickupDateTime.trim().isEmpty()) {
                    out.add(AdminNotif.info(
                            "Pickup rescheduled",
                            summarizeTicket(t) + " → " + snap.pickupDateTime,
                            "Ticket: " + t.ticketId));
                }
            }

            // Always add “action required” entry for pending tickets (keeps admin on top of workload).
            if (t.status != null && "PENDING".equalsIgnoreCase(t.status)) {
                out.add(AdminNotif.action(
                        "Pending donation request",
                        summarizeTicket(t),
                        "Ticket: " + t.ticketId));
            }
        }

        lastSnapshotByTicketId.clear();
        lastSnapshotByTicketId.putAll(next);

        // Sort: action required first, then newest-ish (createdAt as best available).
        out.sort((a, b) -> {
            if (a.actionRequired != b.actionRequired) return a.actionRequired ? -1 : 1;
            // Desc by meta time string if present (fallback stable).
            return 0;
        });

        // Store the full list for filtering.
        if (notifList != null) {
            notifList.putClientProperty("allNotifs", out);
        }
        return out;
    }

    private AdminNotif buildNewTicketNotif(Ticket t) {
        String qty = formatAmountOrQty(t);
        String who = shortName(t.userId);
        String what = (t.itemCategory != null ? t.itemCategory : "Donation");
        String title = "New request: " + what;
        String details = who + " • " + qty + " • " + (t.donationDrive != null ? t.donationDrive : "No drive")
                + (t.deliveryDestination != null && !t.deliveryDestination.trim().isEmpty() ? " → " + t.deliveryDestination : "");
        return AdminNotif.info(title, details, "Ticket: " + safe(t.ticketId));
    }

    private AdminNotif buildStatusChangeNotif(Ticket t, String from, String to) {
        String fromU = from != null ? from.toUpperCase() : "-";
        String toU = to != null ? to.toUpperCase() : "-";

        String who = shortName(t.userId);
        String rider = t.riderId != null && !t.riderId.trim().isEmpty() ? shortName(t.riderId) : null;
        String base = summarizeTicket(t);
        String meta = "Ticket: " + safe(t.ticketId) + (rider != null ? " • Rider: " + rider : "");

        if ("CANCELLED".equalsIgnoreCase(toU)) {
            String reason = t.deleteReason != null && !t.deleteReason.trim().isEmpty() ? t.deleteReason.trim() : "No reason provided";
            return AdminNotif.warn("User cancelled donation", base + " • " + who + " • " + reason, meta);
        }
        if ("REJECTED".equalsIgnoreCase(toU)) {
            String qr = t.qualityReason != null && !t.qualityReason.trim().isEmpty() ? t.qualityReason.trim() : "No reason provided";
            return AdminNotif.warn("Donation rejected", base + " • " + "Reason: " + qr, meta);
        }
        if ("ACCEPTED".equalsIgnoreCase(toU)) {
            return AdminNotif.info("Pickup accepted", base + (rider != null ? " • Accepted by " + rider : ""), meta);
        }
        if ("PICKED_UP".equalsIgnoreCase(toU)) {
            return AdminNotif.info("Donation picked up", base + (rider != null ? " • Picked up by " + rider : ""), meta);
        }
        if ("DELIVERED".equalsIgnoreCase(toU)) {
            return AdminNotif.info("Donation delivered", base + (rider != null ? " • Delivered by " + rider : ""), meta);
        }

        return AdminNotif.info("Status changed (" + fromU + " → " + toU + ")", base, meta);
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

    private boolean eq(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
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

        private AdminNotif(String title, String details, String meta, boolean actionRequired, int level) {
            this.title = title;
            this.details = details;
            this.meta = meta;
            this.actionRequired = actionRequired;
            this.level = level;
        }

        static AdminNotif info(String title, String details, String meta) {
            return new AdminNotif(title, details, meta, false, 0);
        }

        static AdminNotif warn(String title, String details, String meta) {
            return new AdminNotif(title, details, meta, false, 1);
        }

        static AdminNotif action(String title, String details, String meta) {
            return new AdminNotif(title, details, meta, true, 0);
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

    private static class TicketSnapshot {
        final String status;
        final String pickupDateTime;

        private TicketSnapshot(String status, String pickupDateTime) {
            this.status = status;
            this.pickupDateTime = pickupDateTime;
        }

        static TicketSnapshot from(Ticket t) {
            return new TicketSnapshot(t.status, t.pickupDateTime);
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
        String pickupDateTime;
        String donationDrive;
        String deliveryDestination;
        String deleteReason;
        String qualityReason;
    }
}
