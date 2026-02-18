package Controller;

import View.RiderDashboard;
import View.RiderDriveDetailsView;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RiderDriveDetailsController {

    private RiderDriveDetailsView view;
    private String driveName;
    private Runnable onCloseCallback;

    public RiderDriveDetailsController(RiderDriveDetailsView view, String driveName, Runnable onCloseCallback) {
        this.view = view;
        this.driveName = driveName;
        this.onCloseCallback = onCloseCallback;

        view.backBtn.addActionListener(e -> close());
        view.setOnAcceptListener(this::acceptTicket);

        loadData();
    }

    private void close() {
        view.frame.dispose();
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Client client = Client.getDefault();
                String responseXml = client.readTickets("rider", "PENDING"); // Get ALL pending for any rider
                Client.Response response = Client.parseResponse(responseXml);

                List<RiderDashboard.TicketStub> filteredList = new ArrayList<>();

                if (response != null && response.isOk() && response.message != null) {
                    String ticketsXml = Client.unescapeXml(response.message);

                    int idx = 0;
                    while (true) {
                        int start = ticketsXml.indexOf("<ticket>", idx);
                        if (start < 0)
                            break;
                        int end = ticketsXml.indexOf("</ticket>", start);
                        if (end < 0)
                            break;

                        String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
                        String ticketId = extract(ticketXml, "ticketId");
                        String category = extract(ticketXml, "itemCategory");
                        String quantity = extract(ticketXml, "quantity");
                        String location = extract(ticketXml, "pickupLocation");
                        String drive = extract(ticketXml, "donationDrive");

                        // Filter by Drive
                        if (drive != null && drive.contains(driveName)) {
                            filteredList.add(new RiderDashboard.TicketStub(
                                    ticketId,
                                    category != null ? category : "Item",
                                    quantity != null ? quantity : "1",
                                    location != null ? location : "Location N/A",
                                    drive));
                        }

                        idx = end + "</ticket>".length();
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    view.updateCards(filteredList);
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void acceptTicket(String ticketId) {
        String userId = LoginController.currentUserEmail;
        if (userId == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(view.frame,
                "Accept this ticket?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        new Thread(() -> {
            try {
                Client client = Client.getDefault();
                String resp = client.updateTicket(userId, ticketId, "ACCEPTED");
                Client.Response r = Client.parseResponse(resp);
                SwingUtilities.invokeLater(() -> {
                    if (r != null && r.isOk()) {
                        JOptionPane.showMessageDialog(view.frame, "Ticket Accepted!");
                        loadData(); // Refresh list
                    } else {
                        JOptionPane.showMessageDialog(view.frame, "Failed: " + (r != null ? r.message : ""));
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
}
