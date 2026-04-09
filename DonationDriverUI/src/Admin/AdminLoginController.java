package Admin;

import Controller.LoginController;
import Network.Client;

import javax.swing.*;
import java.io.IOException;

public class AdminLoginController {

    private final AdminLoginView view;

    public AdminLoginController(AdminLoginView view) {
        this.view = view;
        this.view.loginBtn.addActionListener(e -> handleLogin());
        this.view.cancelBtn.addActionListener(e -> view.frame.dispose());
    }

    private void handleLogin() {
        String email = view.emailField.getText();
        String password = new String(view.passField.getPassword());

        if (email == null || email.trim().isEmpty() || email.equals("Admin Email")
                || password == null || password.trim().isEmpty() || password.equals("Password")) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please enter both email and password.",
                    "Admin Login",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            String responseXml = Client.getInstance().getService().login(email, password);
            Client.Response response = Client.parseResponse(responseXml);
            String status = response != null ? response.status : "";
            String message = response != null ? response.message : "";

            if ("OK".equalsIgnoreCase(status)) {
                String role = response != null ? response.role : "";
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    JOptionPane.showMessageDialog(view.frame,
                            "Access Denied: You do not have administrator privileges.",
                            "Admin Login",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LoginController.currentUserEmail = email;
                LoginController.currentUserRole = "ADMIN";
                LoginController.setReconnectToMainLoginAfterDisconnect(false);

                JOptionPane.showMessageDialog(view.frame,
                        "Admin login successful!",
                        "Admin Login",
                        JOptionPane.INFORMATION_MESSAGE);
                view.frame.dispose();

                AdminDashboardView adminDashboardView = new AdminDashboardView();
                adminDashboardView.logoutBtn.addActionListener(e -> {
                    adminDashboardView.stopAllTimers();
                    Client.getInstance().logoutQuiet(email);
                    LoginController.currentUserEmail = null;
                    LoginController.currentUserRole = null;
                    LoginController.setReconnectToMainLoginAfterDisconnect(true);
                    adminDashboardView.frame.dispose();
                    AdminLoginView adminLoginView = new AdminLoginView();
                    new AdminLoginController(adminLoginView);
                });
            } else {
                JOptionPane.showMessageDialog(view.frame,
                        (message != null && !message.isEmpty())
                                ? message
                                : "Invalid admin email or password.",
                        "Admin Login",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view.frame,
                    "Cannot contact server. Make sure the DonationDriver server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
