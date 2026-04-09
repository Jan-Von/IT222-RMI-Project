package Controller;

import Network.DonationDriverService;
import View.*;
import Network.Client;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiderAcceptedController {

    private final RiderAcceptedView view;
    private List<String> ticketIds = new ArrayList<>();
    private List<String> photoData = new ArrayList<>();
    private Timer refreshTimer;

    public RiderAcceptedController(RiderAcceptedView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.myPickupsBtn.addActionListener(e -> openDonations());
        view.helpBtn.addActionListener(e -> openHelp());
        view.homeBtn.addActionListener(e -> openHome());
        view.deliveredBtn.addActionListener(e -> openDelivered());
        view.rejectBtn.addActionListener(e -> openRejected());
        view.refreshBtn.addActionListener(e -> loadAcceptedTickets());
        view.markPickedUpBtn.addActionListener(e -> markSelectedPickedUp());
        view.rejectTicketBtn.addActionListener(e -> rejectSelectedTicket());
        view.viewPhotoBtn.addActionListener(e -> showSelectedPhoto());
        view.settingsBtn.addActionListener(e -> openSettings());

        loadAcceptedTickets();

        refreshTimer = new Timer(5000, e -> loadAcceptedTickets());
        refreshTimer.start();
    }

    private void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void loadAcceptedTickets() {
        ticketIds.clear();
        photoData.clear();
        try {
            DonationDriverService svc = Client.getInstance().getService();
            String userId = LoginController.currentUserEmail;
            String responseXml = svc.readTickets(userId != null ? userId : "", "ACCEPTED");
            Client.Response response = Client.parseResponse(responseXml);
            DefaultListModel<String> model = new DefaultListModel<>();
            if (response != null && response.isOk() && response.message != null && !response.message.isEmpty()) {
                String ticketsXml = Client.unescapeXml(response.message);
                List<String> lines = parseTicketSummaries(ticketsXml);
                for (String line : lines) {
                    model.addElement(line);
                }
            }
            if (model.getSize() == 0) {
                model.addElement("No accepted pickups. Click Refresh to load.");
            }
            view.ticketsList.setModel(model);
        } catch (IOException ex) {
            ex.printStackTrace();
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Error loading tickets. Click Refresh to retry.");
            view.ticketsList.setModel(model);
        }
    }

    private List<String> parseTicketSummaries(String ticketsXml) {
        List<String> list = new ArrayList<>();
        if (ticketsXml == null || ticketsXml.isEmpty())
            return list;
        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0)
                break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0)
                break;
            String oneTicket = ticketsXml.substring(start, end + "</ticket>".length());
            String ticketId = getTag(oneTicket, "ticketId");
            ticketIds.add(ticketId != null ? ticketId : "");

            String photoBase64 = getTag(oneTicket, "photoBase64");
            photoData.add(photoBase64 != null ? photoBase64 : "");

            String category = getTag(oneTicket, "itemCategory");
            String quantity = getTag(oneTicket, "quantity");
            String location = getTag(oneTicket, "pickupLocation");
            String destination = getTag(oneTicket, "deliveryDestination");
            String summary = String.format("ID %s | %s x%s | %s -> %s",
                    or(ticketId, "?"), or(category, "-"), or(quantity, "1"),
                    or(location, "N/A"), or(destination, "N/A"));
            list.add(summary);
            idx = end + "</ticket>".length();
        }
        return list;
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

    private void markSelectedPickedUp() {
        updateSelectedTicketStatus("PICKED_UP");
    }

    private void rejectSelectedTicket() {
        if (updateSelectedTicketStatus("REJECTED")) {
            // Immediately show the rejected list so the rider sees the update.
            openRejected();
        }
    }

    private boolean updateSelectedTicketStatus(String newStatus) {
        int idx = view.ticketsList.getSelectedIndex();
        if (idx < 0 || idx >= ticketIds.size()) {
            JOptionPane.showMessageDialog(view.frame, "Please select a pickup first.");
            return false;
        }
        String ticketId = ticketIds.get(idx);
        if (ticketId == null || ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Invalid ticket selected.");
            return false;
        }
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Please log in to update ticket.");
            return false;
        }
        try {
            Client client = Client.getDefault();
            String responseXml = client.updateTicket(userId, ticketId, newStatus);
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Ticket updated to " + newStatus + ".");
                loadAcceptedTickets();
                return true;
            } else {
                JOptionPane.showMessageDialog(view.frame,
                        resp != null && resp.message != null ? resp.message : "Update failed.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame, "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void openDonations() {
        stopTimer();
        DonationsActiveView donationsActiveView = new DonationsActiveView();
        new DonationsActiveController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        stopTimer();
        RiderNotificationView notificationView = new RiderNotificationView();
        new RiderNotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        stopTimer();
        RiderHelpView helpView = new RiderHelpView();
        new RiderHelpController(helpView);
        helpView.frame.setVisible(true);
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

    private void openRejected() {
        stopTimer();
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDelivered() {
        stopTimer();
        RiderDeliveredView deliveredView = new RiderDeliveredView();
        new RiderDeliveredController(deliveredView);
        deliveredView.frame.setVisible(true);
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

    private void showSelectedPhoto() {
        int idx = view.ticketsList.getSelectedIndex();
        if (idx < 0 || idx >= photoData.size()) {
            JOptionPane.showMessageDialog(view.frame, "Please select a pickup first.");
            return;
        }

        String base64 = photoData.get(idx);
        if (base64 == null || base64.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "No photo available for this donation.");
            return;
        }

        try {
            byte[] imgBytes = java.util.Base64.getDecoder().decode(base64.trim());
            java.awt.Image img = java.awt.Toolkit.getDefaultToolkit().createImage(imgBytes);
            ImageIcon icon = new ImageIcon(img);
            if (icon.getIconWidth() > 800) {
                 java.awt.Image scaled = img.getScaledInstance(800, -1, java.awt.Image.SCALE_SMOOTH);
                 icon = new ImageIcon(scaled);
            }
            JLabel label = new JLabel(icon);
            JScrollPane scroll = new JScrollPane(label);
            scroll.setPreferredSize(new java.awt.Dimension(850, 600));
            JOptionPane.showMessageDialog(view.frame, scroll, "Donation Photo", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.frame, "Error displaying photo: " + ex.getMessage());
        }
    }
}
