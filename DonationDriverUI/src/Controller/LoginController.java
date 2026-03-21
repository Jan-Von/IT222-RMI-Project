package Controller;

import View.LoginView;
import View.RegistrationView;
import View.DashboardView;
import Network.Client;
import javax.swing.*;
import java.io.IOException;
import java.rmi.RemoteException;

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
            String responseXml = Client.getInstance().getService().login(email, password);
            Client.Response response = Client.parseResponse(responseXml);
            String status = response != null ? response.status : "";
            String role = response != null ? response.role : "";
            String message = response != null ? response.message : "";

            if ("OK".equalsIgnoreCase(status)) {
                currentUserEmail = email;
                currentUserRole = role == null ? "" : role;

                JOptionPane.showMessageDialog(view.frame, "Login Success!");

                if ("RIDER".equalsIgnoreCase(currentUserRole)) {
                    View.RiderDashboard riderView = new View.RiderDashboard();
                    new RiderController(riderView);
                    riderView.frame.setVisible(true);
                } else {
                    DashboardView dashboardView = new DashboardView();
                    new DashboardController(dashboardView);
                }
                view.frame.dispose();
            } else {
                String msg = (message != null && !message.isEmpty())
                        ? message
                        : "Invalid email or password!";
                JOptionPane.showMessageDialog(view.frame, msg);
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Authentication failed due to remote server error.",
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Cannot contact server. Make sure the DonationDriver server is running (port 5267).",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}