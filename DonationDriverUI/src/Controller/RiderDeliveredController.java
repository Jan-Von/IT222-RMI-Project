package Controller;

import Network.DonationDriverService;
import View.*;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiderDeliveredController {

    private final RiderDeliveredView view;
    private Timer refreshTimer;
    private final List<String> ticketIds = new ArrayList<>();
    private final List<String> ticketStatuses = new ArrayList<>();

    public RiderDeliveredController(RiderDeliveredView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.myPickupsBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e -> openHome());
        view.helpBtn.addActionListener(e -> openHelp());
        view.acceptBtn.addActionListener(e -> openAccepted());
        view.rejectBtn.addActionListener(e -> openRejected());
        view.refreshBtn.addActionListener(e -> loadTickets());
        view.markDeliveredBtn.addActionListener(e -> markSelectedDelivered());
        view.settingsBtn.addActionListener(e -> openSettings());

        loadTickets();

        refreshTimer = new Timer(5000, e -> loadTickets());
        refreshTimer.start();
    }

    private void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    /**
     * Load rider tickets relevant to the Delivered screen.
     * - PICKED_UP: can be marked DELIVERED
     * - DELIVERED: already completed (shown for confirmation/history)
     */
    private void loadTickets() {
        ticketIds.clear();
        ticketStatuses.clear();
        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            DonationDriverService svc = Client.getInstance().getService();
            String userId = LoginController.currentUserEmail;
            String uid = userId != null ? userId : "";

            // Show both in one list so delivered updates are visible immediately.
            addTicketsToModel(svc.readTickets(uid, "PICKED_UP"), model, "PICKED_UP");
            addTicketsToModel(svc.readTickets(uid, "DELIVERED"), model, "DELIVERED");

            if (model.isEmpty()) {
                model.addElement("No picked-up or delivered donations. Click Refresh to reload.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            model.clear();
            model.addElement("Error loading tickets. Click Refresh to retry.");
        }
        view.ticketsList.setModel(model);
    }

    private void addTicketsToModel(String responseXml, DefaultListModel<String> model, String statusLabel) {
        Client.Response response = Client.parseResponse(responseXml);
        if (response == null || !response.isOk() || response.message == null || response.message.isEmpty()) {
            return;
        }
        String ticketsXml = Client.unescapeXml(response.message);
        if (ticketsXml == null || ticketsXml.isEmpty()) {
            return;
        }

        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0) break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0) break;

            String oneTicket = ticketsXml.substring(start, end + "</ticket>".length());
            String ticketId = getTag(oneTicket, "ticketId");
            String category = getTag(oneTicket, "itemCategory");
            String quantity = getTag(oneTicket, "quantity");
            String location = getTag(oneTicket, "pickupLocation");
            String destination = getTag(oneTicket, "deliveryDestination");

            ticketIds.add(ticketId != null ? ticketId : "");
            ticketStatuses.add(statusLabel);

            String summary = String.format("[%s] ID %s | %s x%s | %s → %s",
                    statusLabel,
                    or(ticketId, "?"),
                    or(category, "-"),
                    or(quantity, "1"),
                    or(location, "N/A"),
                    or(destination, "N/A"));
            model.addElement(summary);

            idx = end + "</ticket>".length();
        }
    }

    private String getTag(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i < 0 || j <= i)
            return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private String or(String v, String def) {
        return (v != null && !v.isEmpty()) ? v : def;
    }

    private void markSelectedDelivered() {
        int idx = view.ticketsList.getSelectedIndex();
        if (idx < 0 || idx >= ticketIds.size() || idx >= ticketStatuses.size()) {
            JOptionPane.showMessageDialog(view.frame, "Please select a pickup first.");
            return;
        }
        if (!"PICKED_UP".equalsIgnoreCase(ticketStatuses.get(idx))) {
            JOptionPane.showMessageDialog(view.frame, "Only PICKED_UP tickets can be marked DELIVERED.");
            return;
        }
        String ticketId = ticketIds.get(idx);
        if (ticketId == null || ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Invalid ticket selected.");
            return;
        }
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Please log in to update ticket status.");
            return;
        }
        try {
            Client client = Client.getDefault();
            String responseXml = client.updateTicket(userId, ticketId, "DELIVERED");
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Ticket marked as DELIVERED.");
                loadTickets();
            } else {
                JOptionPane.showMessageDialog(view.frame,
                        resp != null && resp.message != null ? resp.message : "Update failed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame, "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDonations() {
        stopTimer();
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        stopTimer();
        setRiderUnavailable();
        RiderDashboard riderDashboard = new RiderDashboard();
        new RiderController(riderDashboard);
        riderDashboard.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        stopTimer();
        setRiderUnavailable();
        RiderNotificationView notificationView = new RiderNotificationView();
        new RiderNotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void setRiderUnavailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty())
            return;
        try {
            Client.getDefault().setRiderUnavailable(userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void openHelp() {
        stopTimer();
        setRiderUnavailable();
        RiderHelpView helpView = new RiderHelpView();
        new RiderHelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openAccepted() {
        stopTimer();
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openRejected() {
        stopTimer();
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSettings() {
        stopTimer();
        setRiderUnavailable();
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
        view.frame.dispose();
    }
}
