package Controller;

import View.*;
import Network.Client;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DonationsPendingController {

    private static final String PICKUP_DATETIME_FORMAT_HINT = "yyyy-MM-dd HH:mm (e.g. 2026-02-20 14:30)";

    private DonationsPendingView view;
    private List<String> ticketIds = new ArrayList<>();

    public DonationsPendingController(DonationsPendingView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.notifBtn.addActionListener(e -> openNotification());
        view.ActiveDeliveryButton.addActionListener(e -> openDonationsActive());
        view.DeliveredButton.addActionListener(e -> openDonationsDelivered());
        view.RejectedButton.addActionListener(e -> openDonationsRejected());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.changePickupTimeBtn.addActionListener(e -> showReschedulePickupDialog());

        loadPendingTickets();
    }

    private void loadPendingTickets() {
        String user = LoginController.currentUserEmail;
        if (user == null || user.trim().isEmpty()) {
            showMessageInList("Please log in to see your pending donations.");
            return;
        }

        try {
            Client client = Client.getDefault();
            String responseXml = client.readTickets(user, "PENDING");
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                String ticketsXml = Client.unescapeXml(response.message != null ? response.message : "");
                List<String> lines = buildTicketLines(ticketsXml);
                if (lines.isEmpty()) {
                    showMessageInList("No pending donations.");
                    ticketIds.clear();
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
        ticketIds.clear();
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
            String ticketId = getTag(oneTicket, "ticketId");
            ticketIds.add(ticketId != null ? ticketId : "");
            String line = formatOneTicket(oneTicket);
            lines.add(line);

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

    private void openDashBoard() {
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsActive() {
        DonationsActiveView donationsactiveView = new DonationsActiveView();
        new DonationsActiveController(donationsactiveView);
        donationsactiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsRejected() {
        DonationsRejectedView donationsRejectedView = new DonationsRejectedView();
        new DonationsRejectedController(donationsRejectedView);
        donationsRejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonationsDelivered() {
        DonationsDeliveredView donationsdeliveredView = new DonationsDeliveredView();
        new DonationsDeliveredController(donationsdeliveredView);
        donationsdeliveredView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
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
                    loadPendingTickets();
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
