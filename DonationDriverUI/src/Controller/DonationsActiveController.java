package Controller;

import View.*;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonationsActiveController {

    private DonationsActiveView view;
    private MonetaryDonationView view1;

    public DonationsActiveController(DonationsActiveView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.notifBtn.addActionListener(e -> openNotification());
        view.DeliveredButton.addActionListener(e -> openDonationsDelivered());
        view.RejectedButton.addActionListener(e -> openDonationsRejected());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.PendingButton.addActionListener(e -> openDonationPending());

        loadActiveTickets();
    }
    private void loadActiveTickets() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Please log in to see your active donations.");
            view.ticketsList.setModel(model);
            return;
        }

        try {
            Client client = Client.getDefault();

            String acceptedResponseXml = client.readTickets(userId, "ACCEPTED");
            Client.Response acceptedResponse = Client.parseResponse(acceptedResponseXml);

            String pickedUpResponseXml = client.readTickets(userId, "PICKED_UP");
            Client.Response pickedUpResponse = Client.parseResponse(pickedUpResponseXml);

            DefaultListModel<String> model = new DefaultListModel<>();

            if ((acceptedResponse != null && acceptedResponse.isOk()) || 
                (pickedUpResponse != null && pickedUpResponse.isOk())) {
                
                List<String> summaries = new ArrayList<>();

                if (acceptedResponse != null && acceptedResponse.isOk()) {
                    String acceptedTicketsXml = Client.unescapeXml(acceptedResponse.message != null ? acceptedResponse.message : "");
                    summaries.addAll(parseTicketSummaries(acceptedTicketsXml));
                }

                if (pickedUpResponse != null && pickedUpResponse.isOk()) {
                    String pickedUpTicketsXml = Client.unescapeXml(pickedUpResponse.message != null ? pickedUpResponse.message : "");
                    summaries.addAll(parseTicketSummaries(pickedUpTicketsXml));
                }

                if (summaries.isEmpty()) {
                    model.addElement("You have no active donation tickets yet.");
                } else {
                    for (String s : summaries) {
                        model.addElement(s);
                    }
                }
            } else {
                String msg = "Failed to load active donations.";
                if (acceptedResponse != null && acceptedResponse.message != null && !acceptedResponse.message.isEmpty()) {
                    msg = acceptedResponse.message;
                } else if (pickedUpResponse != null && pickedUpResponse.message != null && !pickedUpResponse.message.isEmpty()) {
                    msg = pickedUpResponse.message;
                }
                model.addElement(msg);
            }

            view.ticketsList.setModel(model);

        } catch (IOException ex) {
            ex.printStackTrace();
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Error: Unable to contact server to load donations.");
            view.ticketsList.setModel(model);
        }
    }

    private List<String> parseTicketSummaries(String ticketsXml) {
        List<String> list = new ArrayList<>();
        if (ticketsXml == null || ticketsXml.isEmpty()) {
            return list;
        }

        int idx = 0;
        while (true) {
            int start = ticketsXml.indexOf("<ticket>", idx);
            if (start < 0) break;
            int end = ticketsXml.indexOf("</ticket>", start);
            if (end < 0) break;

            String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());

            String ticketId     = extractTagValue(ticketXml, "ticketId");
            String status       = extractTagValue(ticketXml, "status");
            String itemCategory = extractTagValue(ticketXml, "itemCategory");
            String quantity     = extractTagValue(ticketXml, "quantity");
            String pickupLoc    = extractTagValue(ticketXml, "pickupLocation");
            String drive        = extractTagValue(ticketXml, "donationDrive");
            String destination  = extractTagValue(ticketXml, "deliveryDestination");

            String extra = "";
            if ((drive != null && !drive.isEmpty()) || (destination != null && !destination.isEmpty())) {
                extra = " | " + (drive != null ? drive : "—") + " → " + (destination != null ? destination : "—");
            }
            String summary = String.format(
                    "ID %s | %s x%s | Status: %s | Location: %s%s",
                    ticketId != null ? ticketId : "?",
                    itemCategory != null ? itemCategory : "Unknown",
                    (quantity != null && !quantity.isEmpty()) ? quantity : "1",
                    status != null ? status : "UNKNOWN",
                    pickupLoc != null ? pickupLoc : "N/A",
                    extra
            );
            list.add(summary);

            idx = end + "</ticket>".length();
        }
        return list;
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

    private void openDonationPending(){
        DonationsPendingView donationsPendingView = new DonationsPendingView();
        new DonationsPendingController(donationsPendingView);
        donationsPendingView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsDelivered(){
        DonationsDeliveredView donationsDeliverView = new DonationsDeliveredView();
        new DonationsDeliveredController(donationsDeliverView);
        donationsDeliverView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsRejected(){
        DonationsRejectedView donationsRejectedView = new DonationsRejectedView();
        new DonationsRejectedController(donationsRejectedView);
        donationsRejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate (){
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }
}
