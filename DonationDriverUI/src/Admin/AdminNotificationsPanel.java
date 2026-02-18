package Admin;

import Network.Client;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdminNotificationsPanel extends JPanel {

    private JTable monetaryTable;
    private DefaultTableModel monetaryTableModel;
    private JTable donationBoxesTable;
    private DefaultTableModel donationBoxesTableModel;

    public AdminNotificationsPanel() {
        setLayout(null);
        setBackground(new Color(240, 242, 245));

        // Title
        JLabel titleLabel = new JLabel("Notifications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setBounds(30, 20, 300, 30);
        add(titleLabel);

        JLabel subtitleLabel = new JLabel("Manage user requests");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setBounds(30, 50, 300, 20);
        add(subtitleLabel);

        JLabel monetaryLabel = new JLabel("Monetary Donations");
        monetaryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        monetaryLabel.setBounds(30, 90, 200, 25);
        add(monetaryLabel);

        String[] monetaryColumns = { "Name", "Amount", "Reference No.", "Mode of Payment", "Date" };
        monetaryTableModel = new DefaultTableModel(monetaryColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        monetaryTable = createStyledTable(monetaryTableModel);
        JScrollPane monetaryScrollPane = new JScrollPane(monetaryTable);
        monetaryScrollPane.setBounds(30, 120, 800, 150);
        add(monetaryScrollPane);

        JLabel boxesLabel = new JLabel("Donation Boxes");
        boxesLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        boxesLabel.setBounds(30, 300, 200, 25);
        add(boxesLabel);

        String[] boxesColumns = { "Name", "Boxes", "Reference No.", "Location", "Date" };
        donationBoxesTableModel = new DefaultTableModel(boxesColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        donationBoxesTable = createStyledTable(donationBoxesTableModel);
        JScrollPane boxesScrollPane = new JScrollPane(donationBoxesTable);
        boxesScrollPane.setBounds(30, 330, 800, 150);
        add(boxesScrollPane);

        refreshData();
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(Color.WHITE);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(0, 35));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        return table;
    }

    // fetch data and populate
    public void refreshData() {
        monetaryTableModel.setRowCount(0);
        donationBoxesTableModel.setRowCount(0);

        List<Ticket> tickets = loadTicketsFromServer();

        boolean hasMonetary = false;
        boolean hasBoxes = false;

        for (Ticket t : tickets) {
            String name = t.userId;
            if (name == null)
                name = "Unknown";
            if (name.contains("@")) {
                name = name.split("@")[0];
            }

            String date = t.createdAt;
            if (date != null && date.length() > 10)
                date = date.substring(0, 10);

            if (t.itemCategory != null && t.itemCategory.toLowerCase().contains("monetary")) {
                hasMonetary = true;
                String amount = "N/A";
                String transactionId = t.ticketId;

                String text = t.notes;
                if (text != null) {
                    int amtIdx = text.indexOf("Amount=");
                    if (amtIdx >= 0) {
                        int end = text.indexOf(";", amtIdx);
                        if (end < 0)
                            end = text.length();
                        amount = text.substring(amtIdx + 7, end).trim();
                    }
                    int txnIdx = text.indexOf("TransactionId=");
                    if (txnIdx >= 0) {
                        int end = text.indexOf(";", txnIdx);
                        if (end < 0)
                            end = text.length();
                        transactionId = text.substring(txnIdx + 14, end).trim();
                    }
                }

                monetaryTableModel.addRow(new Object[] {
                        name,
                        amount,
                        transactionId,
                        t.donationDrive != null ? t.donationDrive : "N/A",
                        date
                });
            } else {
                hasBoxes = true;
                // Donation Boxes
                donationBoxesTableModel.addRow(new Object[] {
                        name,
                        t.quantity != null ? t.quantity : "1",
                        t.ticketId,
                        t.pickupLocation != null ? t.pickupLocation : "N/A",
                        date
                });
            }
        }

        if (!hasMonetary) {
            monetaryTableModel.addRow(new Object[] { "-", "-", "-", "-", "-" });
        }
        if (!hasBoxes) {
            donationBoxesTableModel.addRow(new Object[] { "-", "-", "-", "-", "-" });
        }
    }

    private List<Ticket> loadTicketsFromServer() {
        List<Ticket> list = new ArrayList<>();
        try {
            Client client = Client.getDefault();
            // Client.readTickets uses the currently logged in user context
            String userId = Controller.LoginController.currentUserEmail;
            if (userId == null || userId.isEmpty()) {
                userId = "admin";
            }
            String responseXml = client.readTickets(userId, null);
            Client.Response response = Client.parseResponse(responseXml);
            if (response == null || !response.isOk()) {
                return list;
            }

            String ticketsXml = Client.unescapeXml(response.message);
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

                String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());
                Ticket t = new Ticket();
                t.ticketId = extract(ticketXml, "ticketId");
                t.userId = extract(ticketXml, "userId");
                t.itemCategory = extract(ticketXml, "itemCategory");
                t.quantity = extract(ticketXml, "quantity");
                t.notes = extract(ticketXml, "notes");
                t.createdAt = extract(ticketXml, "createdAt");
                t.pickupLocation = extract(ticketXml, "pickupLocation");
                t.donationDrive = extract(ticketXml, "donationDrive");
                list.add(t);

                idx = end + "</ticket>".length();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
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

    private static class Ticket {
        String ticketId;
        String userId;
        String itemCategory;
        String quantity;
        String notes;
        String createdAt;
        String pickupLocation;
        String donationDrive;
    }
}
