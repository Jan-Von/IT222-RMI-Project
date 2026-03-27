package Admin;

import Network.Client;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminLogsPanel extends JPanel {

    private JTextArea logsArea;
    private JToggleButton maintenanceToggle;

    public AdminLogsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title and Switch Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("System Logs & Server Control");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(20, 35, 100));
        titlePanel.add(title);

        JLabel subtitle = new JLabel("Monitor server activities and manage connection status");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 100, 100));
        titlePanel.add(subtitle);

        topPanel.add(titlePanel, BorderLayout.WEST);

        // Maintenance Toggle (Simulates Start/Stop)
        maintenanceToggle = new JToggleButton("Server is ONLINE");
        maintenanceToggle.setBackground(new Color(0, 150, 0));
        maintenanceToggle.setForeground(Color.WHITE);
        maintenanceToggle.setFocusPainted(false);

        maintenanceToggle.addActionListener(e -> {
            boolean isMaintenance = maintenanceToggle.isSelected();
            try {
                Client client = Client.getDefault();
                String response = client.setServerMaintenanceMode(isMaintenance);
                Client.Response resp = Client.parseResponse(response);
                if (resp != null && resp.isOk()) {
                    if (isMaintenance) {
                        maintenanceToggle.setText("Server is OFFLINE (Maintenance Mode)");
                        maintenanceToggle.setBackground(new Color(200, 0, 0));
                    } else {
                        maintenanceToggle.setText("Server is ONLINE");
                        maintenanceToggle.setBackground(new Color(0, 150, 0));
                    }
                } else {
                    maintenanceToggle.setSelected(!isMaintenance);
                    JOptionPane.showMessageDialog(this, "Failed to toggle server state.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                maintenanceToggle.setSelected(!isMaintenance);
                JOptionPane.showMessageDialog(this, "Unable to contact server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            refreshData();
        });

        topPanel.add(maintenanceToggle, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Logs text area
        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logsArea.setBackground(new Color(245, 245, 245));

        JScrollPane scrollPane = new JScrollPane(logsArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scrollPane, BorderLayout.CENTER);

        // Real-Time Polling Timer
        new Timer(3000, e -> refreshData()).start();
    }

    public void refreshData() {
        try {
            Client client = Client.getDefault();
            String response = client.getServerLogs();
            Client.Response resp = Client.parseResponse(response);
            if (resp != null && resp.isOk() && resp.message != null) {
                logsArea.setText(resp.message);

                // Adjust switch state if modified outside
                if (resp.message.contains("offline")) {
                    // Do nothing or sync state accurately
                }
            }
        } catch (Exception e) {
            logsArea.setText("Failed to fetch logs: " + e.getMessage());
        }
    }
}
