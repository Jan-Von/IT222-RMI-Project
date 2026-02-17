package Controller;

import View.*;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiderDeliveredController {

    private final RiderDeliveredView view;
    private final List<String> ticketIds = new ArrayList<>();

    public RiderDeliveredController(RiderDeliveredView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e -> openHome());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.acceptBtn.addActionListener(e -> openAccepted());
        view.rejectBtn.addActionListener(e -> openRejected());
        view.refreshBtn.addActionListener(e -> loadPickedUpTickets());
        view.markDeliveredBtn.addActionListener(e -> markSelectedDelivered());

        loadPickedUpTickets();
    }

    /** Load all PICKED_UP tickets so rider can mark them delivered. */
    private void loadPickedUpTickets() {
        ticketIds.clear();
        DefaultListModel<String> model = new DefaultListModel<>();
        try {
            Client client = Client.getDefault();
            String responseXml = client.readTickets("", "PICKED_UP");
            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk() && response.message != null && !response.message.isEmpty()) {
                String ticketsXml = Client.unescapeXml(response.message);
                List<String> summaries = parseTicketSummaries(ticketsXml);
                for (String s : summaries) {
                    model.addElement(s);
                }
            }
            if (model.isEmpty()) {
                model.addElement("No picked-up donations to deliver. Click Refresh to reload.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            model.clear();
            model.addElement("Error loading tickets. Click Refresh to retry.");
        }
        view.ticketsList.setModel(model);
    }

    private List<String> parseTicketSummaries(String ticketsXml) {
        List<String> list = new ArrayList<>();
        if (ticketsXml == null || ticketsXml.isEmpty()) return list;
        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0) break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0) break;

            String oneTicket = ticketsXml.substring(start, end + "</ticket>".length());
            String ticketId = getTag(oneTicket, "ticketId");
            ticketIds.add(ticketId != null ? ticketId : "");
            String category = getTag(oneTicket, "itemCategory");
            String quantity = getTag(oneTicket, "quantity");
            String location = getTag(oneTicket, "pickupLocation");
            String destination = getTag(oneTicket, "deliveryDestination");

            String summary = String.format("ID %s | %s x%s | %s → %s",
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
        if (i < 0 || j <= i) return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private String or(String v, String def) {
        return (v != null && !v.isEmpty()) ? v : def;
    }

    private void markSelectedDelivered() {
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
            JOptionPane.showMessageDialog(view.frame, "Please log in to update ticket status.");
            return;
        }
        try {
            Client client = Client.getDefault();
            String responseXml = client.updateTicket(userId, ticketId, "DELIVERED");
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Ticket marked as DELIVERED.");
                loadPickedUpTickets();
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
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        setRiderUnavailable();
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        setRiderUnavailable();
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void setRiderUnavailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) return;
        try {
            Client.getDefault().setRiderUnavailable(userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void openDonate() {
        setRiderUnavailable();
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        setRiderUnavailable();
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openAccepted() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openRejected() {
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }
}
