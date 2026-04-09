package Controller;

import View.RiderDashboard;
import View.RiderNotificationView;
import View.SettingsView;
import View.RiderAcceptedView;
import Network.Client;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.Box;

public class RiderNotificationController {

    private RiderNotificationView view;
    private javax.swing.Timer refreshTimer;

    public RiderNotificationController(RiderNotificationView view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> openHome());
        view.myPickupsBtn.addActionListener(e -> openPickups());
        view.helpBtn.addActionListener(e -> openHelp());
        view.settingsBtn.addActionListener(e -> openSettings());

        loadNotifications();

        refreshTimer = new javax.swing.Timer(5000, e -> loadNotifications());
        refreshTimer.start();
    }

    private void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void loadNotifications() {
        view.notificationsPanel.removeAll();
        try {
            Client client = Client.getDefault();
            String userId = LoginController.currentUserEmail;
            if (userId == null) userId = "rider";

            String responseXml = client.readTickets("admin", null); // fetch all, then filter
            Client.Response response = Client.parseResponse(responseXml);

            boolean hasNotifs = false;

            if (response != null && response.isOk() && response.message != null) {
                String ticketsXml = Client.unescapeXml(response.message);
                int idx = 0;
                while (true) {
                    int start = ticketsXml.indexOf("<ticket>", idx);
                    if (start < 0) break;
                    int end = ticketsXml.indexOf("</ticket>", start);
                    if (end < 0) break;

                    String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
                    String riderId = extract(ticketXml, "riderId");
                    String status = extract(ticketXml, "status");
                    String tId = extract(ticketXml, "ticketId");
                    String drive = extract(ticketXml, "donationDrive");
                    String category = extract(ticketXml, "itemCategory");

                    if (userId.equals(riderId) && !"PENDING".equals(status)) {
                        String title = "Donation Update: " + status;
                        String msg = "Ticket " + tId + (drive != null && !drive.isEmpty() ? " for " + drive : "");
                        String meta = "Category: " + (category != null && !category.isEmpty() ? category : "Items");
                        view.notificationsPanel.add(buildNotificationItem(title, msg, meta));
                        view.notificationsPanel.add(Box.createVerticalStrut(10));
                        hasNotifs = true;
                    }

                    idx = end + "</ticket>".length();
                }
            }

            if (!hasNotifs) {
                JLabel info = new JLabel("No new notifications.");
                info.setFont(new Font("Arial", Font.PLAIN, 16));
                view.notificationsPanel.add(info);
            }

        } catch (Exception ex) {
            JLabel info = new JLabel("Error loading notifications.");
            info.setFont(new Font("Arial", Font.PLAIN, 16));
            view.notificationsPanel.add(info);
        }

        view.notificationsPanel.revalidate();
        view.notificationsPanel.repaint();
    }

    private JPanel buildNotificationItem(String title, String msg, String meta) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setBackground(new Color(248, 248, 250));
        row.setOpaque(true);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 225), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        row.setMaximumSize(new Dimension(800, 80));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Arial", Font.BOLD, 14));
        t.setForeground(new Color(20, 35, 100));
        row.add(t, BorderLayout.NORTH);

        JLabel msgLabel = new JLabel(msg);
        msgLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        row.add(msgLabel, BorderLayout.CENTER);

        JLabel m = new JLabel(meta);
        m.setFont(new Font("Arial", Font.ITALIC, 11));
        m.setForeground(new Color(120, 120, 120));
        row.add(m, BorderLayout.SOUTH);

        return row;
    }

    private String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        if (i < 0) return null;
        int j = xml.indexOf(close, i + open.length());
        if (j < 0) return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private void openHome() {
        stopTimer();
        RiderDashboard riderDashboard = new RiderDashboard();
        new RiderController(riderDashboard);
        riderDashboard.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openPickups() {
        stopTimer();
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        stopTimer();
        View.RiderHelpView helpView = new View.RiderHelpView();
        new RiderHelpController(helpView);
        view.frame.dispose();
    }

    private void openSettings() {
        stopTimer();
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
    }
}
