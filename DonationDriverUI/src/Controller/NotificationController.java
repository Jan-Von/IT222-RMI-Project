package Controller;

import View.*;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationController {

    private NotificationView view;
    private Timer refreshTimer;

    public NotificationController(NotificationView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashboard());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.settingsBtn.addActionListener(e -> openSettings());

        loadNotifications();

        refreshTimer = new Timer(5000, e -> loadNotifications());
        refreshTimer.start();
    }

    private void loadNotifications() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        try {
            Client client = Client.getDefault();
            String responseXml = client.readTickets(userId);
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                view.clearNotifications();
                String ticketsXml = Client.unescapeXml(response.message != null ? response.message : "");
                parseAndAddNotifications(ticketsXml);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void parseAndAddNotifications(String ticketsXml) {
        if (ticketsXml == null || ticketsXml.isEmpty()) return;

        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0) break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0) break;

            String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());

            extractTagValue(ticketXml, "ticketId");
            String status = extractTagValue(ticketXml, "status");
            String category = extractTagValue(ticketXml, "itemCategory");
            extractTagValue(ticketXml, "quantity");
            String quantity = extractTagValue(ticketXml, "quantity");
            String drive = extractTagValue(ticketXml, "donationDrive");
            String pickupLoc = extractTagValue(ticketXml, "pickupLocation");
            extractTagValue(ticketXml, "qualityStatus");
            String qualityReason = extractTagValue(ticketXml, "qualityReason");

            String timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a MMM dd"));
            
            String message = "Your donation of " + quantity + " " + category + " for '" + drive + "' ";
            
            if ("PENDING".equalsIgnoreCase(status)) {
                message += "is currently pending review.";
            } else if ("ACCEPTED".equalsIgnoreCase(status)) {
                message += "has been ACCEPTED! A rider will pick it up soon.";
            } else if ("PICKED_UP".equalsIgnoreCase(status)) {
                message += "has been PICKED UP by a rider.";
            } else if ("DELIVERED".equalsIgnoreCase(status)) {
                message += "has been SUCCESSFULLY DELIVERED. Thank you!";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                message += "was REJECTED.";
                if (qualityReason != null && !qualityReason.isEmpty()) {
                    message += " Reason: " + qualityReason;
                }
            }

            view.addNotificationCard(pickupLoc, status, message, timeStr, category);

            idx = end + "</ticket>".length();
        }
    }

    private String extractTagValue(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i == -1 || j == -1 || j <= i) return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private void openDashboard() {
        stopTimer();
        DashboardView dash = new DashboardView();
        new DashboardController(dash);
        dash.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations() {
        stopTimer();
        DonationsActiveView dav = new DonationsActiveView();
        new DonationsActiveController(dav);
        dav.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        stopTimer();
        DonateView dv = new DonateView();
        new DonateController(dv);
        dv.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        stopTimer();
        HelpView hv = new HelpView();
        new HelpController(hv);
        hv.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSettings() {
        stopTimer();
        SettingsView sv = new SettingsView();
        new SettingsController(sv);
        sv.frame.setVisible(true);
        view.frame.dispose();
    }

    private void stopTimer() {
        if (refreshTimer != null) refreshTimer.stop();
    }
}
