package Controller;

import View.*;
import Network.Client;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonationsActiveController {

    private static final String PICKUP_DATETIME_FORMAT_HINT = "yyyy-MM-dd HH:mm (e.g. 2026-02-20 14:30)";

    private DonationsActiveView view;
    private List<String> ticketIds = new ArrayList<>();
    private Timer refreshTimer;

    public DonationsActiveController(DonationsActiveView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.notifBtn.addActionListener(e -> openNotification());
        view.DeliveredButton.addActionListener(e -> openDonationsDelivered());
        view.RejectedButton.addActionListener(e -> openDonationsRejected());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.settingsBtn.addActionListener(e -> openSettings());
        view.PendingButton.addActionListener(e -> openDonationPending());
        view.helpBtn.addActionListener(e -> openHelp());
        view.changePickupTimeBtn.addActionListener(e -> showReschedulePickupDialog());

        loadActiveTickets();

        refreshTimer = new Timer(5000, e -> loadActiveTickets());
        refreshTimer.start();
    }


    private void loadActiveTickets() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            DefaultListModel<String> model = new DefaultListModel<>();
            model.addElement("Please log in to see your active donations.");
            view.ticketsList.setModel(model);
            ticketIds.clear();
            return;
        }

        try {
            int selectedIdx = view.ticketsList.getSelectedIndex();

            Client client = Client.getDefault();

            String acceptedResponseXml = client.readTickets(userId, "ACCEPTED");
            Client.Response acceptedResponse = Client.parseResponse(acceptedResponseXml);

            String pickedUpResponseXml = client.readTickets(userId, "PICKED_UP");
            Client.Response pickedUpResponse = Client.parseResponse(pickedUpResponseXml);

            DefaultListModel<String> model = new DefaultListModel<>();

            if ((acceptedResponse != null && acceptedResponse.isOk()) || 
                (pickedUpResponse != null && pickedUpResponse.isOk())) {
                
                List<String> summaries = new ArrayList<>();

                ticketIds.clear();
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
                ticketIds.clear();
                String msg = "Failed to load active donations.";
                if (acceptedResponse != null && acceptedResponse.message != null && !acceptedResponse.message.isEmpty()) {
                    msg = acceptedResponse.message;
                } else if (pickedUpResponse != null && pickedUpResponse.message != null && !pickedUpResponse.message.isEmpty()) {
                    msg = pickedUpResponse.message;
                }
                model.addElement(msg);
            }

            view.ticketsList.setModel(model);
            if (selectedIdx >= 0 && selectedIdx < model.getSize()) {
                view.ticketsList.setSelectedIndex(selectedIdx);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            ticketIds.clear();
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
            ticketIds.add(ticketId != null ? ticketId : "");
            String status       = extractTagValue(ticketXml, "status");
            String itemCategory = extractTagValue(ticketXml, "itemCategory");
            String quantity     = extractTagValue(ticketXml, "quantity");
            String notes        = extractTagValue(ticketXml, "notes");
            String pickupLoc    = extractTagValue(ticketXml, "pickupLocation");
            String drive        = extractTagValue(ticketXml, "donationDrive");
            String destination  = extractTagValue(ticketXml, "deliveryDestination");

            String displayQty = (quantity != null && !quantity.isEmpty()) ? quantity : "1";
            String displayCategory = itemCategory != null ? itemCategory : "Unknown";
            if (itemCategory != null && itemCategory.toLowerCase().contains("monetary")) {
                String amount = parseAmountFromNotes(notes);
                if (amount != null && !amount.trim().isEmpty()) {
                    displayQty = amount.startsWith("₱") ? amount : ("₱" + amount);
                } else {
                    displayQty = "₱—";
                }
                displayCategory = "Monetary";
            }

            String extra = "";
            if ((drive != null && !drive.isEmpty()) || (destination != null && !destination.isEmpty())) {
                extra = " | " + (drive != null ? drive : "—") + " → " + (destination != null ? destination : "—");
            }
            String summary = String.format(
                    "ID %s | %s x%s | Status: %s | Location: %s%s",
                    ticketId != null ? ticketId : "?",
                    displayCategory,
                    displayQty,
                    status != null ? status : "UNKNOWN",
                    pickupLoc != null ? pickupLoc : "N/A",
                    extra
            );
            list.add(summary);

            idx = end + "</ticket>".length();
        }
        return list;
    }

    private String parseAmountFromNotes(String notes) {
        if (notes == null) return null;
        int amtIdx = notes.indexOf("Amount=");
        if (amtIdx >= 0) {
            int end = notes.indexOf(";", amtIdx);
            if (end < 0) end = notes.length();
            return notes.substring(amtIdx + 7, end).trim();
        }
        amtIdx = notes.indexOf("Amount:");
        if (amtIdx >= 0) {
            int end = notes.indexOf("|", amtIdx);
            if (end < 0) end = notes.length();
            return notes.substring(amtIdx + 7, end).trim();
        }
        return null;
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

    private void openSettings(){
        SettingsView Settingview = new SettingsView();
        new SettingsController(Settingview);
        Settingview.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDashBoard(){
        if (refreshTimer != null) refreshTimer.stop();
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification(){
        if (refreshTimer != null) refreshTimer.stop();
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationPending(){
        if (refreshTimer != null) refreshTimer.stop();
        DonationsPendingView donationsPendingView = new DonationsPendingView();
        new DonationsPendingController(donationsPendingView);
        donationsPendingView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsDelivered(){
        if (refreshTimer != null) refreshTimer.stop();
        DonationsDeliveredView donationsDeliverView = new DonationsDeliveredView();
        new DonationsDeliveredController(donationsDeliverView);
        donationsDeliverView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsRejected(){
        if (refreshTimer != null) refreshTimer.stop();
        DonationsRejectedView donationsRejectedView = new DonationsRejectedView();
        new DonationsRejectedController(donationsRejectedView);
        donationsRejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate (){
        if (refreshTimer != null) refreshTimer.stop();
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp(){
        if (refreshTimer != null) refreshTimer.stop();
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void showReschedulePickupDialog() {
        int idx = view.ticketsList.getSelectedIndex();
        if (idx < 0 || idx >= ticketIds.size()) {
            JOptionPane.showMessageDialog(view.frame, "Please select a donation first.");
            return;
        }
        String ticketId = ticketIds.get(idx);
        if (ticketId == null || ticketId.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "No valid ticket selected.");
            return;
        }
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Please log in to reschedule.");
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(view.frame), "Change Pickup Time", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialog.add(content, BorderLayout.CENTER);

        JPanel fieldPanel = new JPanel(new BorderLayout(5, 5));
        fieldPanel.add(new JLabel("New pickup date/time (" + PICKUP_DATETIME_FORMAT_HINT + "):"), BorderLayout.NORTH);
        JTextField pickupField = new JTextField(25);
        pickupField.setToolTipText(PICKUP_DATETIME_FORMAT_HINT);
        fieldPanel.add(pickupField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Update pickup time");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        content.add(fieldPanel, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            String newPickup = pickupField.getText().trim();
            if (newPickup.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a pickup date/time.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Client client = Client.getDefault();
                String responseXml = client.updateTicketPickupTime(userId, ticketId, newPickup);
                Client.Response resp = Client.parseResponse(responseXml);
                if (resp != null && resp.isOk()) {
                    JOptionPane.showMessageDialog(view.frame, "Pickup time updated successfully.");
                    loadActiveTickets();
                    dialog.dispose();
                } else {
                    String msg = (resp != null && resp.message != null && !resp.message.isEmpty())
                            ? resp.message : "Failed to update pickup time.";
                    JOptionPane.showMessageDialog(dialog, msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(view.frame);
        dialog.setVisible(true);
    }
}
