package Controller;

import View.*;
import Network.Client;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonationsRejectedController {

    private DonationsRejectedView view;

    public DonationsRejectedController(DonationsRejectedView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.notifBtn.addActionListener(e -> openNotification());
        view.ActiveDeliveryButton.addActionListener(e -> openDonationsActive());
        view.DeliveredButton.addActionListener(e -> openDonationsDelivered());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.PendingButton.addActionListener(e -> openDonationPending());
        view.helpBtn.addActionListener(e -> openHelp());


        loadRejectedTickets();
    }

    private void loadRejectedTickets() {
        String user = LoginController.currentUserEmail;
        if (user == null || user.trim().isEmpty()) {
            showMessageInList("Please log in to see your rejected donations.");
            return;
        }

        try {
            Client client = Client.getDefault();
            String responseXml = client.readTickets(user, "REJECTED");
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                List<String> lines = buildTicketLines(Client.unescapeXml(response.message != null ? response.message : ""));
                if (lines.isEmpty()) {
                    showMessageInList("No rejected donations.");
                } else {
                    setListLines(lines);
                }
            } else {
                String msg = response != null && hasText(response.message) ? response.message : "Failed to load.";
                showMessageInList(msg);
            }
        } catch (IOException e) {
            showMessageInList("Error: Could not reach the server.");
        }
    }

    private List<String> buildTicketLines(String ticketsXml) {
        List<String> lines = new ArrayList<>();
        if (ticketsXml == null || ticketsXml.isEmpty()) {
            return lines;
        }

        int from = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", from);
            if (start < 0) break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0) break;

            String oneTicket = ticketsXml.substring(start, end + "</ticket>".length());
            lines.add(formatOneTicket(oneTicket));
            from = end + "</ticket>".length();
        }
        return lines;
    }

    private String formatOneTicket(String ticketXml) {
        String id = getTag(ticketXml, "ticketId");
        String status = getTag(ticketXml, "status");
        String category = getTag(ticketXml, "itemCategory");
        String quantity = getTag(ticketXml, "quantity");
        String location = getTag(ticketXml, "pickupLocation");
        String drive = getTag(ticketXml, "donationDrive");
        String destination = getTag(ticketXml, "deliveryDestination");

        String extra = "";
        if ((drive != null && !drive.isEmpty()) || (destination != null && !destination.isEmpty())) {
            extra = " | " + or(drive, "—") + " → " + or(destination, "—");
        }
        return String.format("ID %s | %s x%s | Status: %s | Location: %s%s",
                or(id, "?"),
                or(category, "Unknown"),
                or(quantity, "1"),
                or(status, "—"),
                or(location, "N/A"),
                extra);
    }

    private String getTag(String xml, String tagName) {
        String open = "<" + tagName + ">";
        String close = "</" + tagName + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i < 0 || j <= i) return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private String or(String value, String fallback) {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private void showMessageInList(String message) {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement(message);
        view.ticketsList.setModel(model);
    }

    private void setListLines(List<String> lines) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String line : lines) {
            model.addElement(line);
        }
        view.ticketsList.setModel(model);
    }

    private void openDashBoard(){
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification(){
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsActive(){
        DonationsActiveView donationsactiveView = new DonationsActiveView();
        new DonationsActiveController(donationsactiveView);
        donationsactiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationPending(){
        DonationsPendingView donationsPendingView = new DonationsPendingView();
        new DonationsPendingController(donationsPendingView);
        donationsPendingView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsDelivered(){
        DonationsDeliveredView donationsdeliveredView = new DonationsDeliveredView();
        new DonationsDeliveredController(donationsdeliveredView);
        donationsdeliveredView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate (){
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp(){
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

}
