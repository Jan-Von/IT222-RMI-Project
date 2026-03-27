package Admin;

import Controller.LoginController;
import View.LoginView;

import javax.swing.*;

public class AdminLoginController {

    private final AdminLoginView view;
    private final AdminCredentialsStore store;

    public AdminLoginController(AdminLoginView view) {
        this.view = view;
        this.store = new AdminCredentialsStore();

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

        if (store.isValidAdmin(email, password)) {
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
                    "Invalid admin email or password.",
                    "Admin Login",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
