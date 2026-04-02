package Controller;

import View.SettingsView;
import View.DashboardView;
import Network.Client;
import javax.swing.*;
import java.io.IOException;

public class SettingsController {

    private SettingsView view;

    public SettingsController(SettingsView view) {
        this.view = view;


        view.homeBtn.addActionListener(e -> goHome());
        view.logoutBtn.addActionListener(e -> logout());

        if ("RIDER".equalsIgnoreCase(LoginController.currentUserRole)) {
            view.switchRoleBtn.setVisible(false);
        } else {
            view.switchRoleBtn.setVisible(true);
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(view.frame,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            LoginController.currentUserEmail = null;
            LoginController.currentUserRole = null;

            View.LoginView loginView = new View.LoginView();
            new LoginController(loginView);
            view.frame.dispose();
        }
    }


    private void goHome() {
        String role = LoginController.currentUserRole;
        if ("RIDER".equalsIgnoreCase(role)) {
            View.RiderDashboard riderView = new View.RiderDashboard();
            new Controller.RiderController(riderView);
            riderView.frame.setVisible(true);
        } else {
            DashboardView dashboardView = new DashboardView();
            new DashboardController(dashboardView);
            dashboardView.frame.setVisible(true);
        }
        view.frame.dispose();
    }
}
