package Admin;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import Network.Client;

public class AdminHomePanel extends JPanel {

    private JLabel shippedValueLabel;
    private JLabel ridersOnlineValueLabel;
    private JLabel ongoingShipmentValueLabel;
    private JPanel urgentCardsPanel; // rebuild on refresh
    private AdminLogsPanel adminLogsPanel;
    private Timer refreshTimer;

    private Runnable onShowNotifications;
    private Runnable onShowDonations;

    private static final int PAD = 20;
    private static final int GAP = 16;

    public AdminHomePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);
        main.add(buildMetricsBar());
        main.add(Box.createVerticalStrut(GAP));
        main.add(buildViewAllDonationsBar());
        main.add(Box.createVerticalStrut(GAP));
        main.add(buildUrgentDonations());
        main.add(Box.createVerticalStrut(GAP));
        main.add(buildBottomRow());
        main.add(Box.createVerticalStrut(GAP));

        adminLogsPanel = new AdminLogsPanel();
        adminLogsPanel.setPreferredSize(new Dimension(800, 300));
        adminLogsPanel.setMinimumSize(new Dimension(800, 300));
        adminLogsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        adminLogsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        main.add(adminLogsPanel);

        main.add(Box.createVerticalGlue());

        add(new JScrollPane(main), BorderLayout.CENTER);
        refreshData();

        refreshTimer = new Timer(5000, e -> refreshData());
        refreshTimer.start();
    }

    public void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        if (adminLogsPanel != null) {
            adminLogsPanel.stopTimer();
        }
    }

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, GAP, 0));
        row.setOpaque(false);
        row.add(buildActiveDeliveryPanel());
        row.add(buildNotificationsPanel());
        return row;
    }

    private JPanel buildMetricsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 3, GAP, 0));
        bar.setOpaque(false);

        shippedValueLabel = new JLabel("0");
        ridersOnlineValueLabel = new JLabel("0");
        ongoingShipmentValueLabel = new JLabel("0");

        JPanel c1 = buildMetricCard("Shipped Donations", shippedValueLabel, new Color(234, 248, 255));
        JPanel c2 = buildMetricCard("Pending Requests", ridersOnlineValueLabel, new Color(238, 247, 239));
        JPanel c3 = buildMetricCard("Ongoing Shipment", ongoingShipmentValueLabel, new Color(253, 244, 230));
        c1.setMinimumSize(new Dimension(180, 80));
        c2.setMinimumSize(new Dimension(180, 80));
        c3.setMinimumSize(new Dimension(180, 80));
        bar.add(c1);
        bar.add(c2);
        bar.add(c3);
        return bar;
    }

    private JPanel buildMetricCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(90, 90, 90));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(new Color(20, 35, 100));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshData() {
        Metrics m = loadMetricsFromServer();

        if (shippedValueLabel != null) {
            shippedValueLabel.setText(String.valueOf(m.deliveredCount));
        }
        if (ridersOnlineValueLabel != null) {
            ridersOnlineValueLabel.setText(String.valueOf(m.pendingCount));
        }
        if (ongoingShipmentValueLabel != null) {
            ongoingShipmentValueLabel.setText(String.valueOf(m.activeCount));
        }

        if (urgentCardsPanel != null) {
            urgentCardsPanel.removeAll();
            populateUrgentCards(urgentCardsPanel);
            urgentCardsPanel.revalidate();
            urgentCardsPanel.repaint();
        }
    }

    private static class Metrics {
        int pendingCount;
        int deliveredCount;
        int activeCount;
    }

    private Metrics loadMetricsFromServer() {
        Metrics m = new Metrics();
        try {
            Client client = Client.getDefault();
            // Use "admin" to ensure we get all data
            String responseXml = client.readTickets("admin", null);
            Client.Response response = Client.parseResponse(responseXml);
            if (response == null || !response.isOk()) {
                return m;
            }

            String ticketsXml = response.message;
            if (ticketsXml == null || ticketsXml.isEmpty()) {
                return m;
            }
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

                String status = extractTagValue(ticketXml, "status");
                String isDeleted = extractTagValue(ticketXml, "isDeleted");
                String st = status != null ? status.toUpperCase() : "";
                boolean deleted = "true".equalsIgnoreCase(isDeleted);

                if (deleted || "CANCELLED".equals(st) || "REJECTED".equals(st)) {
                    // Ignore
                } else if ("DELIVERED".equals(st)) {
                    m.deliveredCount++;
                } else if ("PENDING".equals(st)) {
                    m.pendingCount++;
                } else if ("ACCEPTED".equals(st) || "PICKED_UP".equals(st)) {
                    m.activeCount++;
                }

                idx = end + "</ticket>".length();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return m;
    }

    private JPanel buildUrgentDonations() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);

        JLabel header = new JLabel("Urgent Donations");
        header.setFont(new Font("Arial", Font.BOLD, 14));
        header.setForeground(new Color(20, 35, 100));
        panel.add(header, BorderLayout.NORTH);

        urgentCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        urgentCardsPanel.setOpaque(false);
        populateUrgentCards(urgentCardsPanel);

        panel.add(urgentCardsPanel, BorderLayout.CENTER);
        return panel;
    }

    private void populateUrgentCards(JPanel cards) {
        java.util.List<Drive> drives = loadDrivesFromServer();
        for (Drive d : drives) {
            int percentage = 0;
            if (d.targetAmount > 0) {
                percentage = (int) ((d.currentAmount / d.targetAmount) * 100);
            }
            final String driveTitle = d.title;
            cards.add(buildUrgentCard(d.title,
                    String.format("%,.2f", d.currentAmount),
                    String.valueOf(percentage),
                    "-",
                    percentage,
                    d.description,
                    () -> deleteDrive(driveTitle)));
        }

        JButton addNew = new JButton("+");
        addNew.setPreferredSize(new Dimension(120, 140));
        addNew.setBackground(new Color(20, 35, 100));
        addNew.setForeground(Color.WHITE);
        addNew.setFont(new Font("Arial", Font.PLAIN, 36));
        addNew.setFocusPainted(false);
        addNew.setToolTipText("Add new urgent donation campaign");
        addNew.addActionListener(e -> {
            AddDonationDriveView addView = new AddDonationDriveView((JFrame) SwingUtilities.getWindowAncestor(this));
            new AddDonationDriveController(addView, this::refreshData);
            addView.frame.setVisible(true);
        });
        cards.add(addNew);
    }

    private static class Drive {
        String title;
        String description;
        double targetAmount;
        double currentAmount;
    }

    private java.util.List<Drive> loadDrivesFromServer() {
        java.util.List<Drive> list = new java.util.ArrayList<>();
        try {
            Client client = Client.getDefault();
            String xml = client.readDonationDrives();
            if (xml == null || xml.isEmpty())
                return list; // Empty or error

            xml = Client.unescapeXml(xml);

            Client.Response resp = Client.parseResponse(xml);
            if (resp == null || !resp.isOk())
                return list;

            String drivesXml = resp.message;
            if (drivesXml == null)
                return list;

            int idx = 0;
            while (true) {
                int start = drivesXml.indexOf("<drive>", idx);
                if (start < 0)
                    break;
                int end = drivesXml.indexOf("</drive>", start);
                if (end < 0)
                    break;

                String driveXml = drivesXml.substring(start, end + "</drive>".length());
                Drive d = new Drive();
                d.title = extractTagValue(driveXml, "title");
                d.description = extractTagValue(driveXml, "description");
                String tAmt = extractTagValue(driveXml, "targetAmount");
                String cAmt = extractTagValue(driveXml, "currentAmount");
                try {
                    d.targetAmount = Double.parseDouble(tAmt != null ? tAmt : "0");
                    d.currentAmount = Double.parseDouble(cAmt != null ? cAmt : "0");
                } catch (NumberFormatException ignored) {
                }

                list.add(d);
                idx = end + "</drive>".length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private JPanel buildUrgentCard(String title, String monetary, String current, String incoming,
            int progressPercent, String whatsHappening) {
        return buildUrgentCard(title, monetary, current, incoming, progressPercent, whatsHappening, null);
    }

    private JPanel buildUrgentCard(String title, String monetary, String current, String incoming,
            int progressPercent, String whatsHappening, Runnable onDelete) {
        Color cardBg = new Color(246, 249, 255);
        Color borderColor = new Color(200, 210, 230);
        Color hoverBg = new Color(235, 242, 252);
        Color hoverBorder = new Color(20, 35, 100);

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(220, 140));
        card.setBackground(cardBg);
        card.setBorder(new LineBorder(borderColor, 1));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(220, 24));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        t.setForeground(new Color(20, 35, 100));
        topRow.add(t);
        if (onDelete != null) {
            JButton delBtn = new JButton("Delete");
            delBtn.setFont(new Font("Arial", Font.PLAIN, 10));
            delBtn.setForeground(new Color(180, 0, 0));
            delBtn.setContentAreaFilled(false);
            delBtn.setBorderPainted(false);
            delBtn.setFocusPainted(false);
            delBtn.addActionListener(ev -> {
                if (JOptionPane.showConfirmDialog(card, "Delete donation drive \"" + title + "\"?", "Delete Drive",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    onDelete.run();
                }
            });
            topRow.add(Box.createHorizontalGlue());
            topRow.add(delBtn);
        }
        card.add(topRow);
        card.add(Box.createVerticalStrut(4));
        card.add(new JLabel("Collected: " + monetary));
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(progressPercent);
        bar.setPreferredSize(new Dimension(200, 6));
        card.add(Box.createVerticalStrut(4));
        card.add(bar);

        final String what = whatsHappening;
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
                card.setBorder(new LineBorder(hoverBorder, 1));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(cardBg);
                card.setBorder(new LineBorder(borderColor, 1));
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!(e.getSource() instanceof JButton))
                    showUrgentDetailDialog(title, monetary, current, incoming, progressPercent, what);
            }
        });

        return card;
    }

    private void deleteDrive(String driveTitle) {
        try {
            Client client = Client.getDefault();
            String userId = Controller.LoginController.currentUserEmail;
            if (userId == null)
                userId = "admin";
            String responseXml = client.getService().deleteDonationDrive(userId, driveTitle);
            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
                JOptionPane.showMessageDialog(this, response.message, "Drive Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                String msg = (response != null && response.message != null) ? response.message
                        : "Failed to delete drive.";
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUrgentDetailDialog(String title, String monetary, String current, String incoming,
            int progressPercent, String whatsHappening) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(20, 35, 100));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(12));

        content.add(new JLabel("Monetary Donations: " + monetary));
        content.add(new JLabel("Current Donations: " + current));
        content.add(new JLabel("Incoming Donations: " + incoming));
        content.add(Box.createVerticalStrut(8));

        JLabel progressLabel = new JLabel("Progress: " + progressPercent + "% of goal");
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        content.add(progressLabel);
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(progressPercent);
        bar.setPreferredSize(new Dimension(280, 8));
        content.add(bar);
        content.add(Box.createVerticalStrut(12));

        JLabel whatLabel = new JLabel("<html>What's happening: " + whatsHappening + "</html>");
        whatLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        whatLabel.setForeground(new Color(70, 70, 70));
        whatLabel.setMaximumSize(new Dimension(320, 80));
        content.add(whatLabel);

        JButton viewNotifBtn = new JButton("View in Notifications");
        viewNotifBtn.setFocusPainted(false);
        JButton closeBtn = new JButton("Close");
        closeBtn.setFocusPainted(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(viewNotifBtn);
        buttons.add(closeBtn);
        content.add(Box.createVerticalStrut(16));
        content.add(buttons);

        JDialog dialog = new JDialog((Frame) null, "Urgent Donation Details", true);
        dialog.getContentPane().add(content);
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);

        closeBtn.addActionListener(e -> dialog.dispose());
        viewNotifBtn.addActionListener(e -> {
            dialog.dispose();
            if (onShowNotifications != null) {
                onShowNotifications.run();
            }
        });

        dialog.setVisible(true);
    }

    public void setOnShowNotifications(Runnable onShowNotifications) {
        this.onShowNotifications = onShowNotifications;
    }

    public void setOnShowDonations(Runnable onShowDonations) {
        this.onShowDonations = onShowDonations;
    }

    private JPanel buildViewAllDonationsBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setOpaque(false);

        JLabel label = new JLabel("Donation tickets from the app (monetary and goods)");
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        label.setForeground(new Color(60, 60, 60));
        bar.add(label, BorderLayout.WEST);

        JButton viewAllBtn = new JButton("View all donations");
        viewAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        viewAllBtn.setForeground(new Color(20, 35, 100));
        viewAllBtn.setBackground(new Color(234, 248, 255));
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setBorderPainted(true);
        viewAllBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(20, 35, 100), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        viewAllBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> {
            if (onShowDonations != null) {
                onShowDonations.run();
            }
        });
        bar.add(viewAllBtn, BorderLayout.EAST);

        return bar;
    }

    private JPanel buildActiveDeliveryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(250, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Active Delivery");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(20, 35, 100));
        header.add(title, BorderLayout.WEST);
        JButton viewAll = new JButton("View All");
        viewAll.setFocusPainted(false);
        viewAll.setFont(new Font("Arial", Font.PLAIN, 12));
        header.add(viewAll, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel status = new JLabel("");
        status.setForeground(new Color(200, 120, 0));
        status.setFont(new Font("Arial", Font.BOLD, 12));
        card.add(status, BorderLayout.CENTER);
        JButton viewDetails = new JButton("View Details");
        viewDetails.setFocusPainted(false);
        viewDetails.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(viewDetails, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotificationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(250, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 235), 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(new Color(20, 35, 100));
        header.add(title, BorderLayout.WEST);
        JButton viewAll = new JButton("View All");
        viewAll.setFocusPainted(false);
        viewAll.setFont(new Font("Arial", Font.PLAIN, 12));
        header.add(viewAll, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.add(buildNotificationItem("Donation Delivered", "Baguio City - Tacloban", "3:00 PM Feb. 21 Delivered"));
        list.add(Box.createVerticalStrut(8));
        list.add(buildNotificationItem("In Transit", "Baguio City - Tacloban", "Today 9:22 AM"));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildNotificationItem(String title, String msg, String meta) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 12));
        row.add(t, BorderLayout.NORTH);
        JLabel msgLabel = new JLabel(msg);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        row.add(msgLabel, BorderLayout.CENTER);
        JLabel m = new JLabel(meta);
        m.setFont(new Font("Arial", Font.PLAIN, 11));
        m.setForeground(new Color(120, 120, 120));
        row.add(m, BorderLayout.SOUTH);
        return row;
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

}
