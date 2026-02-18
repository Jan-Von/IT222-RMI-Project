package Controller;

import View.*;
import Network.Client;
import java.io.IOException;
import javax.swing.DefaultListModel;
import java.util.List;

public class RiderRejectedController {

    private final RiderRejectedView view;

    public RiderRejectedController(RiderRejectedView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.myPickupsBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e -> openHome());
        view.helpBtn.addActionListener(e -> openHelp());
        view.deliveredBtn.addActionListener(e -> openDelivered());
        view.acceptBtn.addActionListener(e -> openAccepted());
        view.refreshBtn.addActionListener(e -> loadRejectedTickets());
        view.settingsBtn.addActionListener(e -> openSettings());

        loadRejectedTickets();
    }

    private void loadRejectedTickets() {
        try {
            Client client = Client.getDefault();
            String userId = LoginController.currentUserEmail;
            String responseXml = client.readTickets(userId != null ? userId : "", "REJECTED");
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
                model.addElement("No rejected pickups. Click Refresh to load.");
            }
            view.ticketsList.setModel(model);
        } catch (IOException ex) {
            ex.printStackTrace();
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Error loading tickets. Click Refresh to retry.");
            view.ticketsList.setModel(model);
        }
    }

    private java.util.List<String> parseTicketSummaries(String ticketsXml) {
        java.util.List<String> list = new java.util.ArrayList<>();
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

    private void openDonations() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        setRiderUnavailable();
        RiderDashboard riderDashboard = new RiderDashboard();
        new RiderController(riderDashboard);
        riderDashboard.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
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
        setRiderUnavailable();
        RiderHelpView helpView = new RiderHelpView();
        new RiderHelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openAccepted() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
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
