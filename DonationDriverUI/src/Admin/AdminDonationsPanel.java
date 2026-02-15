package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import Network.Client;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AdminDonationsPanel extends JPanel {

    private static final Color TABLE_HEADER_BG = new Color(240, 240, 240);
    private static final int MAX_PHOTO_DISPLAY_WIDTH = 800;
    private static final int MAX_PHOTO_DISPLAY_HEIGHT = 600;

    private DefaultTableModel donationsTableModel;
    private JTable donationsTable;
    private List<String> photoBase64ByRow = new ArrayList<>();

    public AdminDonationsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Donations");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(20, 35, 100));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        JLabel subtitle = new JLabel("Review and update status of all donation tickets (monetary and goods)");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 100, 100));
        titlePanel.add(subtitle);
        add(titlePanel, BorderLayout.NORTH);

        donationsTableModel = new DefaultTableModel(
                new Object[]{"ID", "Type", "Donor", "Amount/Quantity", "Status", "Date", "Destination"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        donationsTable = new JTable(donationsTableModel);
        styleTable(donationsTable);
        add(new JScrollPane(donationsTable), BorderLayout.CENTER);
        add(buildActionsPanel(), BorderLayout.SOUTH);
        refreshData();
    }

    private JPanel buildActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));

        JButton viewPhotoBtn = new JButton("View photo");
        JButton acceptBtn   = new JButton("Accept");
        JButton pickedUpBtn = new JButton("Picked Up");
        JButton deliveredBtn= new JButton("Delivered");
        JButton rejectBtn   = new JButton("Reject");
        JButton cancelBtn   = new JButton("Cancel Request");

        viewPhotoBtn.addActionListener(e -> viewSelectedPhoto());
        acceptBtn.addActionListener(e -> updateSelectedTicketStatus("ACCEPTED"));
        pickedUpBtn.addActionListener(e -> updateSelectedTicketStatus("PICKED_UP"));
        deliveredBtn.addActionListener(e -> updateSelectedTicketStatus("DELIVERED"));
        rejectBtn.addActionListener(e -> showQualityDialog("REJECTED"));
        cancelBtn.addActionListener(e -> cancelSelectedTicket());

        panel.add(viewPhotoBtn);
        panel.add(acceptBtn);
        panel.add(pickedUpBtn);
        panel.add(deliveredBtn);
        panel.add(rejectBtn);
        panel.add(cancelBtn);

        return panel;
    }

    private void updateSelectedTicketStatus(String newStatus) {
        int row = donationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return;
        }
        String ticketId = String.valueOf(donationsTableModel.getValueAt(row, 0));
        String adminUserId = "admin";

        try {
            Client client = Client.getDefault();
            String responseXml = client.updateTicket(adminUserId, ticketId, newStatus);
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(this, "Status updated: " + newStatus);
                refreshData();
            } else {
                String msg = (resp != null && resp.message != null && !resp.message.isEmpty())
                        ? resp.message : "Failed to update ticket.";
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedTicket() {
        int row = donationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return;
        }
        String ticketId = String.valueOf(donationsTableModel.getValueAt(row, 0));
        String reason = JOptionPane.showInputDialog(this,
                "Reason for cancellation:", "Cancel Request", JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) {
            return;
        }

        String adminUserId = "admin";

        try {
            Client client = Client.getDefault();
            String responseXml = client.deleteTicket(adminUserId, ticketId, reason.trim());
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp != null && resp.isOk()) {
                JOptionPane.showMessageDialog(this, "Ticket cancelled.");
                refreshData();
            } else {
                String msg = (resp != null && resp.message != null && !resp.message.isEmpty())
                        ? resp.message : "Failed to cancel ticket.";
                JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQualityDialog(String newStatus) {
        int row = donationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return;
        }
        String ticketId = String.valueOf(donationsTableModel.getValueAt(row, 0));
        String adminUserId = "admin";

        // Create quality dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Quality Check", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel dialogContent = new JPanel(new BorderLayout(10, 10));
        dialogContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        dialog.add(dialogContent, BorderLayout.CENTER);

        // Quality status panel
        JPanel qualityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qualityPanel.add(new JLabel("Quality Status:"));
        String[] qualityOptions = {"Not set", "Approved", "Rejected"};
        JComboBox<String> qualityDropdown = new JComboBox<>(qualityOptions);
        qualityPanel.add(qualityDropdown);

        // Reason panel
        JPanel reasonPanel = new JPanel(new BorderLayout(5, 5));
        reasonPanel.add(new JLabel("Quality Reason:"), BorderLayout.NORTH);
        JTextArea reasonField = new JTextArea(3, 30);
        reasonField.setLineWrap(true);
        reasonField.setWrapStyleWord(true);
        reasonPanel.add(new JScrollPane(reasonField), BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(qualityPanel, BorderLayout.NORTH);
        mainPanel.add(reasonPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialogContent.add(mainPanel, BorderLayout.CENTER);

        // Action listeners
        okBtn.addActionListener(e -> {
            String qualityStatus = (String) qualityDropdown.getSelectedItem();
            String qualityReason = reasonField.getText().trim();
            
            try {
                Client client = Client.getDefault();
                String responseXml = client.updateTicket(adminUserId, ticketId, newStatus, qualityStatus, qualityReason);
                Client.Response resp = Client.parseResponse(responseXml);
                if (resp != null && resp.isOk()) {
                    JOptionPane.showMessageDialog(this, "Status and quality updated: " + newStatus);
                    refreshData();
                } else {
                    String msg = (resp != null && resp.message != null && !resp.message.isEmpty())
                            ? resp.message : "Failed to update ticket.";
                    JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
    }

    public void refreshData() {
        if (donationsTableModel == null) return;
        donationsTableModel.setRowCount(0);
        photoBase64ByRow.clear();

        List<Object[]> rows = loadDonationsFromServer();
        if (rows.isEmpty()) {
            donationsTableModel.addRow(new Object[]{"-", "-", "-", "-", "No data", "-", "-"});
            photoBase64ByRow.add(null);
        } else {
            for (Object[] row : rows) {
                donationsTableModel.addRow(row);
            }
        }
    }

    private List<Object[]> loadDonationsFromServer() {
        List<Object[]> rows = new ArrayList<>();
        try {
            Client client = Client.getDefault();
            String responseXml = client.readTickets("", null);
            Client.Response response = Client.parseResponse(responseXml);
            if (response == null || !response.isOk()) {
                return rows;
            }
            String ticketsXml = response.message;
            if (ticketsXml == null || ticketsXml.isEmpty()) {
                return rows;
            }
            ticketsXml = Client.unescapeXml(ticketsXml);

            int idx = 0;
            while (true) {
                int start = ticketsXml.indexOf("<ticket>", idx);
                if (start < 0) break;
                int end = ticketsXml.indexOf("</ticket>", start);
                if (end < 0) break;

                String ticketXml = ticketsXml.substring(start, end + "</ticket>".length());

                String id = extractTagValue(ticketXml, "ticketId");
                String type = extractTagValue(ticketXml, "itemCategory");
                String donor = extractTagValue(ticketXml, "userId");
                String quantity = extractTagValue(ticketXml, "quantity");
                String status = extractTagValue(ticketXml, "status");
                String createdAt = extractTagValue(ticketXml, "createdAt");
                String drive = extractTagValue(ticketXml, "donationDrive");
                String destination = extractTagValue(ticketXml, "deliveryDestination");
                String photoBase64 = extractPhotoBase64(ticketXml);

                String amountOrQty = (quantity != null && !quantity.isEmpty())
                        ? quantity + " boxes"
                        : "-";

                String dest = "-";
                if (drive != null && !drive.isEmpty() && destination != null && !destination.isEmpty()) {
                    dest = drive + " → " + destination;
                } else if (destination != null && !destination.isEmpty()) {
                    dest = destination;
                } else if (drive != null && !drive.isEmpty()) {
                    dest = drive;
                }

                rows.add(new Object[]{
                        id != null ? id : "-",
                        type != null ? type : "Goods",
                        donor != null ? donor : "-",
                        amountOrQty,
                        status != null ? status : "-",
                        createdAt != null ? createdAt : "-",
                        dest
                });
                photoBase64ByRow.add(photoBase64);

                idx = end + "</ticket>".length();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /** Extract photoBase64 from ticket XML, stripping CDATA if present. */
    private String extractPhotoBase64(String ticketXml) {
        String raw = extractTagValue(ticketXml, "photoBase64");
        if (raw == null || raw.isEmpty()) return null;
        if (raw.startsWith("<![CDATA[") && raw.endsWith("]]>")) {
            return raw.substring(9, raw.length() - 3);
        }
        return raw;
    }

    private void viewSelectedPhoto() {
        int row = donationsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket first.");
            return;
        }
        if (row >= photoBase64ByRow.size()) {
            JOptionPane.showMessageDialog(this, "No photo data for this row.");
            return;
        }
        String base64 = photoBase64ByRow.get(row);
        if (base64 == null || base64.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No photo attached to this donation.");
            return;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(base64);
            ImageIcon icon = new ImageIcon(bytes);
            Image img = icon.getImage();
            if (img.getWidth(null) > MAX_PHOTO_DISPLAY_WIDTH || img.getHeight(null) > MAX_PHOTO_DISPLAY_HEIGHT) {
                double scale = Math.min(
                        (double) MAX_PHOTO_DISPLAY_WIDTH / img.getWidth(null),
                        (double) MAX_PHOTO_DISPLAY_HEIGHT / img.getHeight(null));
                int w = (int) (img.getWidth(null) * scale);
                int h = (int) (img.getHeight(null) * scale);
                img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                icon = new ImageIcon(img);
            }
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Donation photo", true);
            dialog.getContentPane().setLayout(new BorderLayout());
            JLabel label = new JLabel(icon);
            label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            dialog.getContentPane().add(new JScrollPane(label), BorderLayout.CENTER);
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            JPanel south = new JPanel();
            south.add(closeBtn);
            dialog.getContentPane().add(south, BorderLayout.SOUTH);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Could not display photo (invalid image data).", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}
