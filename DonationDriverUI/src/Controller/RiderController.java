package Controller;

import View.*;
import Network.Client;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RiderController {

    private final RiderDashboard view;

    private static final String[][] HARDCODED_DRIVES = {
            { "Super Typhoon Haiyan", "Resources/Images/image1.png" },
            { "6.9 Magnitude in Cebu", "Resources/Images/image2.png" },
            { "Fire Hits Supermarket in Quezon", "Resources/Images/image3.png" },
    };

    private final Map<String, RiderDashboard.DriveCardRefs> driveRefs = new HashMap<>();

    public RiderController(RiderDashboard view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> goHome());
        view.notifBtn.addActionListener(e -> openNotification());
        view.myPickupsBtn.addActionListener(e -> openDonations());
        view.helpBtn.addActionListener(e -> openHelp());
        view.locUpdateBtn.addActionListener(e -> refreshDashboard());
        view.settingsBtn.addActionListener(e -> openSettings());

        view.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setRiderUnavailable();
            }
        });

        setRiderAvailable();
        buildDriveCards(); // build cards first, then load counts
        loadDashboardData();
    }

    private void buildDriveCards() {
        // Hardcoded drives
        for (String[] d : HARDCODED_DRIVES) {
            String title = d[0];
            String img = d[1];
            RiderDashboard.DriveCardRefs refs = view.addDriveCard(title, img);
            driveRefs.put(title, refs);
            refs.actionBtn.addActionListener(e -> openDriveDetails(title));
        }

        // Server drives
        try {
            Client client = Client.getDefault();
            String responseXml = client.readDonationDrives();
            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
                String drivesXml = Client.unescapeXml(response.message);
                if (drivesXml != null) {
                    int idx = 0;
                    while (true) {
                        int start = drivesXml.indexOf("<drive>", idx);
                        if (start < 0)
                            break;
                        int end = drivesXml.indexOf("</drive>", start);
                        if (end < 0)
                            break;
                        String driveXml = drivesXml.substring(start, end + "</drive>".length());
                        String title = extract(driveXml, "title");
                        if (title != null && !title.isEmpty() && !driveRefs.containsKey(title)) {
                            RiderDashboard.DriveCardRefs refs = view.addDriveCard(title, "Resources/Images/image1.png");
                            driveRefs.put(title, refs);
                            final String t = title;
                            refs.actionBtn.addActionListener(e -> openDriveDetails(t));
                        }
                        idx = end + "</drive>".length();
                    }
                }
            }
        } catch (IOException ignored) {
            /* server unavailable */ }
    }

    private void refreshDashboard() {
        loadDashboardData();
    }

    private void openDriveDetails(String driveName) {
        RiderDriveDetailsView detailsView = new RiderDriveDetailsView(driveName);
        new RiderDriveDetailsController(detailsView, driveName, this::refreshDashboard);
        detailsView.frame.setVisible(true);
    }

    private void loadDashboardData() {
        new Thread(() -> {
            try {
                Client client = Client.getDefault();
                String userId = LoginController.currentUserEmail;

                String pendingXml = client.readTickets("rider", "PENDING");
                Client.Response pendingResponse = Client.parseResponse(pendingXml);

                String myTicketsXml = client.readTickets(userId != null ? userId : "", null);
                Client.Response myTicketsResponse = Client.parseResponse(myTicketsXml);

                int pendingCount = 0;
                int inHandCount = 0;

                final Map<String, Integer> driveCounts = new HashMap<>();
                for (String title : driveRefs.keySet()) {
                    driveCounts.put(title, 0);
                }

                if (pendingResponse != null && pendingResponse.isOk() && pendingResponse.message != null) {
                    String ticketsXml = Client.unescapeXml(pendingResponse.message);
                    int idx = 0;
                    while (true) {
                        int start = ticketsXml.indexOf("<ticket>", idx);
                        if (start < 0)
                            break;
                        int end = ticketsXml.indexOf("</ticket>", start);
                        if (end < 0)
                            break;

                        String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
                        String donationDrive = extract(ticketXml, "donationDrive");

                        pendingCount++;
                        if (donationDrive != null) {
                            for (String title : driveCounts.keySet()) {
                                if (donationDrive.equalsIgnoreCase(title)
                                        || donationDrive.contains(title.split(" ")[title.split(" ").length - 1])) {
                                    driveCounts.put(title, driveCounts.get(title) + 1);
                                    break;
                                }
                            }
                        }

                        idx = end + "</ticket>".length();
                    }
                }

                if (myTicketsResponse != null && myTicketsResponse.isOk() && myTicketsResponse.message != null) {
                    String ticketsXml = Client.unescapeXml(myTicketsResponse.message);
                    String currentUserId = LoginController.currentUserEmail;
                    int idx = 0;
                    while (true) {
                        int start = ticketsXml.indexOf("<ticket>", idx);
                        if (start < 0)
                            break;
                        int end = ticketsXml.indexOf("</ticket>", start);
                        if (end < 0)
                            break;

                        String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
                        String status = extract(ticketXml, "status");
                        String riderId = extract(ticketXml, "riderId");

                        if (("ACCEPTED".equalsIgnoreCase(status) || "PICKED_UP".equalsIgnoreCase(status))
                                && (riderId != null && riderId.equals(currentUserId))) {
                            inHandCount++;
                        }

                        idx = end + "</ticket>".length();
                    }
                }

                final int pCount = pendingCount;
                final int hCount = inHandCount;
                final Map<String, Integer> finalCounts = new HashMap<>(driveCounts);

                SwingUtilities.invokeLater(() -> {
                    view.reqCount.setText(String.valueOf(pCount));
                    view.donInHandCount.setText(String.valueOf(hCount));

                    for (Map.Entry<String, RiderDashboard.DriveCardRefs> entry : driveRefs.entrySet()) {
                        int count = finalCounts.getOrDefault(entry.getKey(), 0);
                        entry.getValue().countLabel.setText(count + " donations available");
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String extract(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int i = xml.indexOf(open);
        int j = xml.indexOf(close);
        if (i < 0 || j <= i)
            return null;
        return xml.substring(i + open.length(), j).trim();
    }

    private void setRiderAvailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty())
            return;
        try {
            Client client = Client.getDefault();
            String responseXml = client.setRiderAvailable(userId);
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp == null || !resp.isOk()) {
                System.err.println("Rider set available failed: " + (resp != null ? resp.message : "null"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setRiderUnavailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty())
            return;
        try {
            Client client = Client.getDefault();
            client.setRiderUnavailable(userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void goHome() {
        refreshDashboard();
    }

    private void openNotification() {
        setRiderUnavailable();
        RiderNotificationView notificationView = new RiderNotificationView();
        new RiderNotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        setRiderUnavailable();
        RiderHelpView helpView = new RiderHelpView();
        new RiderHelpController(helpView);
        helpView.frame.setVisible(true);
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
