package Admin;

import Controller.LoginController;
import Network.Client;

import javax.swing.*;
import java.awt.Component;
import java.awt.Window;
import java.io.IOException;

final class AdminServerWatch {

    private AdminServerWatch() {}

    /**
     * @return {@code false} if server is unreachable and the app was routed back to login
     */
    static boolean pingOrReturnToLogin(Component source) {
        try {
            Client.getDefault().pingSilent();
            return true;
        } catch (IOException e) {
            Window w = SwingUtilities.getWindowAncestor(source);
            if (w instanceof JFrame) {
                Object ref = ((JFrame) w).getRootPane().getClientProperty(AdminDashboardView.FRAME_PROP_KEY);
                if (ref instanceof AdminDashboardView) {
                    ((AdminDashboardView) ref).stopAllTimers();
                }
            }
            LoginController.scheduleReturnToLoginAfterDisconnect(w, null, LoginController.isReconnectToMainLogin());
            return false;
        }
    }
}
