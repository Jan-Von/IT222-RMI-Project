package Controller;

import View.LoginView;
import View.RegistrationView;
import View.DashboardView;
import Network.Client;
import javax.swing.*;
import java.io.IOException;

public class LoginController {

    private LoginView view;
    public static String currentUserEmail;
    public static String currentUserRole;

    public LoginController(LoginView view) {
        this.view = view;

        // Login button Function
        view.loginBtn.addActionListener(e -> login());

        // Sign up button Function
        view.signupBtn.addActionListener(e -> {
            view.frame.dispose();
            RegistrationView regView = new RegistrationView();
            new RegistrationController(regView);
        });
    }

    private void login() {
        String email = view.emailField.getText();
        String password = new String(view.passField.getPassword());

        try {
            Client client = Client.getDefault();
            String responseXml = client.login(email, password);
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                currentUserEmail = email;
                currentUserRole = response.role;

                JOptionPane.showMessageDialog(view.frame, "Login Success!");

                if ("RIDER".equalsIgnoreCase(response.role)) {
                    View.RiderDashboard riderView = new View.RiderDashboard();
                    new RiderController(riderView);
                    riderView.frame.setVisible(true);
                } else {
                    DashboardView dashboardView = new DashboardView();
                    new DashboardController(dashboardView);
                }
                view.frame.dispose();
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Invalid email or password!";
                JOptionPane.showMessageDialog(view.frame, msg);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Cannot contact server. Make sure the DonationDriver server is running (port 5267).",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}