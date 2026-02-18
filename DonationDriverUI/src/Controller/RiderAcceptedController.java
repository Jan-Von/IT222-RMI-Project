package Controller;

import View.*;
import Network.Client;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiderAcceptedController {

    private final RiderAcceptedView view;
    private List<String> ticketIds = new ArrayList<>();

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
        view.settingsBtn.addActionListener(e -> openSettings());

        loadAcceptedTickets();
    }

    private void loadAcceptedTickets() {
        ticketIds.clear();
        try {
            Client client = Client.getDefault();
            String userId = LoginController.currentUserEmail;
            String responseXml = client.readTickets(userId != null ? userId : "", "ACCEPTED");
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
            String category = getTag(oneTicket, "itemCategory");
            String quantity = getTag(oneTicket, "quantity");
            String location = getTag(oneTicket, "pickupLocation");
            String destination = getTag(oneTicket, "deliveryDestination");
            String donor = getTag(oneTicket, "userId");
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
        updateSelectedTicketStatus("REJECTED");
    }

    private void updateSelectedTicketStatus(String newStatus) {
        int idx = view.ticketsList.getSelectedIndex();
        if (idx < 0 || idx >= ticketIds.size()) {
            JOptionPane.showMessageDialog(view.frame, "Please select a pickup first.");
            return;
        }
        String ticketId = ticketIds.get(idx);
        if (ticketId == null || ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Invalid ticket selected.");
            return;
        }
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Please log in to update ticket.");
            return;
        }
        try {
            Client client = Client.getDefault();
            String responseXml = client.updateTicket(userId, ticketId, newStatus);
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Ticket updated to " + newStatus + ".");
                loadAcceptedTickets();
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
        DonationsActiveView donationsActiveView = new DonationsActiveView();
        new DonationsActiveController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        RiderNotificationView notificationView = new RiderNotificationView();
        new RiderNotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        RiderHelpView helpView = new RiderHelpView();
        new RiderHelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
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
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDelivered() {
        RiderDeliveredView deliveredView = new RiderDeliveredView();
        new RiderDeliveredController(deliveredView);
        deliveredView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSettings() {
        setRiderUnavailable();
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
        view.frame.dispose();
    }
}
