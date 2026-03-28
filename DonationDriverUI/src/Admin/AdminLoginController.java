package Admin;

import Controller.LoginController;
import Network.Client;
import View.LoginView;

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

        // Check if placeholder text is still present
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
                JOptionPane.showMessageDialog(view.frame,
                        "Admin login successful!",
                        "Admin Login",
                        JOptionPane.INFORMATION_MESSAGE);
                view.frame.dispose();

                AdminDashboardView adminDashboardView = new AdminDashboardView();
                adminDashboardView.logoutBtn.addActionListener(e -> {
                    adminDashboardView.frame.dispose();
                    LoginView loginView = new LoginView();
                    new LoginController(loginView);
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
            throw new RuntimeException(e);
        }
    }
}
