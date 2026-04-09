package Controller;

import Admin.AdminDashboardView;
import Admin.AdminLoginController;
import Admin.AdminLoginView;
import Network.Client;
import View.DashboardView;
import View.LoginView;
import View.RegistrationView;

import javax.swing.*;
import java.awt.Window;
import java.io.IOException;
import java.rmi.RemoteException;

public class LoginController {

    private LoginView view;
    public static String currentUserEmail;
    public static String currentUserRole;

    /**
     * After a server disconnect while the admin dashboard is open: {@code true} → {@link LoginView},
     * {@code false} → {@link AdminLoginView} (user originally signed in via {@link Admin.AdminMain}).
     */
    private static volatile boolean reconnectToMainLoginAfterDisconnect = true;

    public static boolean isReconnectToMainLogin() {
        return reconnectToMainLoginAfterDisconnect;
    }

    public static void setReconnectToMainLoginAfterDisconnect(boolean useMainLogin) {
        reconnectToMainLoginAfterDisconnect = useMainLogin;
    }

    public LoginController(LoginView view) {
        Client.resetDisconnectedSessionHandling();
        this.view = view;

        view.loginBtn.addActionListener(e -> login());

        view.signupBtn.addActionListener(e -> {
            view.frame.dispose();
            RegistrationView regView = new RegistrationView();
            new RegistrationController(regView);
        });
    }

    public static void scheduleReturnToLoginAfterDisconnect(Window parent) {
        scheduleReturnToLoginAfterDisconnect(parent, null, true);
    }

    public static void scheduleReturnToLoginAfterDisconnect(Window parent, Runnable edtPreamble) {
        scheduleReturnToLoginAfterDisconnect(parent, edtPreamble, true);
    }

    /**
     * @param useMainLoginAfterDisconnect {@code true} open main {@link LoginView}; {@code false} open admin login
     */
    public static void scheduleReturnToLoginAfterDisconnect(Window parent, Runnable edtPreamble,
            boolean useMainLoginAfterDisconnect) {
        if (!Client.tryAcquireDisconnectedSessionHandling()) {
            return;
        }
        final boolean openMainLogin = useMainLoginAfterDisconnect;
        SwingUtilities.invokeLater(() -> {
            if (edtPreamble != null) {
                edtPreamble.run();
            }
            JOptionPane.showMessageDialog(parent,
                    "Server connection lost. You have been logged out.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            String emailForLogout = currentUserEmail;
            currentUserEmail = null;
            currentUserRole = null;
            reconnectToMainLoginAfterDisconnect = true;
            if (emailForLogout != null) {
                Client.getInstance().logoutQuiet(emailForLogout);
            }
            Client.clearCachedService();
            if (parent != null) {
                parent.dispose();
            }
            if (openMainLogin) {
                LoginView loginView = new LoginView();
                new LoginController(loginView);
            } else {
                AdminLoginView adminLoginView = new AdminLoginView();
                new AdminLoginController(adminLoginView);
            }
        });
    }

    private void openAdminDashboardFromUserClient(String email) {
        setReconnectToMainLoginAfterDisconnect(true);
        AdminDashboardView adminView = new AdminDashboardView();
        adminView.logoutBtn.addActionListener(e -> {
            adminView.stopAllTimers();
            Client.getInstance().logoutQuiet(email);
            currentUserEmail = null;
            currentUserRole = null;
            setReconnectToMainLoginAfterDisconnect(true);
            adminView.frame.dispose();
            LoginView loginView = new LoginView();
            new LoginController(loginView);
        });
    }

    private void login() {
        String email = view.emailField.getText();
        String password = new String(view.passField.getPassword());

        try {
            Client client = Client.getInstance();
            client.pingSilent();
            String responseXml = client.login(email, password);
            Client.Response response = Client.parseResponse(responseXml);
            String status = response != null ? response.status : "";
            String role = response != null ? response.role : "";
            String message = response != null ? response.message : "";

            if ("OK".equalsIgnoreCase(status)) {
                currentUserEmail = email;
                currentUserRole = role == null ? "" : role;

                if ("ADMIN".equalsIgnoreCase(currentUserRole)) {
                    JOptionPane.showMessageDialog(view.frame, "Login Success!");
                    view.frame.dispose();
                    openAdminDashboardFromUserClient(email);
                    return;
                }

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
            Client.clearCachedService();
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Authentication failed due to remote server error.",
                    "Server Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Client.clearCachedService();
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Cannot contact server. Make sure the DonationDriver server is running (port 5267).",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
