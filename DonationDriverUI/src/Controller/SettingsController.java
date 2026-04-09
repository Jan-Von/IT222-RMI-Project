package Controller;

import Network.Client;
import Util.RmiClientConfig;
import View.DashboardView;
import View.SettingsView;

import javax.swing.*;
import java.io.IOException;

public class SettingsController {

    private SettingsView view;

    public SettingsController(SettingsView view) {
        this.view = view;

        Client c = Client.getInstance();
        view.rmiHostField.setText(c.getHost());
        view.rmiPortField.setText(String.valueOf(c.getPort()));

        view.homeBtn.addActionListener(e -> goHome());
        view.logoutBtn.addActionListener(e -> logout());
        view.rmiSaveBtn.addActionListener(e -> saveRmiSettings());
        view.rmiTestBtn.addActionListener(e -> testRmiConnection());

        if ("RIDER".equalsIgnoreCase(LoginController.currentUserRole)) {
            view.switchRoleBtn.setVisible(false);
        } else {
            view.switchRoleBtn.setVisible(true);
        }
    }

    private void saveRmiSettings() {
        String host = view.rmiHostField.getText();
        String portStr = view.rmiPortField.getText();
        int port;
        try {
            port = Integer.parseInt(portStr != null ? portStr.trim() : "");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view.frame,
                    "Port must be a number between 1 and 65535.",
                    "Invalid port",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (port < 1 || port > 65535) {
            JOptionPane.showMessageDialog(view.frame,
                    "Port must be between 1 and 65535.",
                    "Invalid port",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            RmiClientConfig.save(host, port);
            Client.configure(host, port);
            JOptionPane.showMessageDialog(view.frame,
                    "RMI settings saved. The client will use this host and port until you change them again.",
                    "Settings",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(view.frame,
                    "Could not save settings: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void testRmiConnection() {
        String host = view.rmiHostField.getText();
        String portStr = view.rmiPortField.getText();
        int port;
        try {
            port = Integer.parseInt(portStr != null ? portStr.trim() : "");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view.frame,
                    "Enter a valid port before testing.",
                    "Invalid port",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (host == null || host.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame,
                    "Enter a host name or address.",
                    "Invalid host",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (port < 1 || port > 65535) {
            JOptionPane.showMessageDialog(view.frame,
                    "Port must be between 1 and 65535.",
                    "Invalid port",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = Client.tryPingEndpoint(host, port);
        if (ok) {
            JOptionPane.showMessageDialog(view.frame,
                    "Connection OK — server responded to ping.",
                    "Test connection",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(view.frame,
                    "Could not reach DonationDriverService at " + host + ":" + port + ".\n"
                            + "Check the host, port, and that the server is running.",
                    "Test connection",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(view.frame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String email = LoginController.currentUserEmail;
            LoginController.currentUserEmail = null;
            LoginController.currentUserRole = null;
            if (email != null) {
                Client.getInstance().logoutQuiet(email);
            }

            View.LoginView loginView = new View.LoginView();
            new LoginController(loginView);
            view.frame.dispose();
        }
    }

    private void goHome() {
        String role = LoginController.currentUserRole;
        if ("RIDER".equalsIgnoreCase(role)) {
            View.RiderDashboard riderView = new View.RiderDashboard();
            new RiderController(riderView);
            riderView.frame.setVisible(true);
        } else {
            DashboardView dashboardView = new DashboardView();
            new DashboardController(dashboardView);
            dashboardView.frame.setVisible(true);
        }
        view.frame.dispose();
    }
}
