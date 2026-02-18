package Controller;

import View.RiderDashboard;
import View.RiderHelpView;
import View.SettingsView;
import View.RiderAcceptedView;

public class RiderHelpController {

    private RiderHelpView view;

    public RiderHelpController(RiderHelpView view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> openHome());
        view.myPickupsBtn.addActionListener(e -> openPickups());
        view.notifBtn.addActionListener(e -> openNotifications());
        view.settingsBtn.addActionListener(e -> openSettings());
        // helpBtn is current view
    }

    private void openHome() {
        RiderDashboard riderDashboard = new RiderDashboard();
        new RiderController(riderDashboard);
        riderDashboard.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openPickups() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotifications() {
        View.RiderNotificationView notifView = new View.RiderNotificationView();
        new RiderNotificationController(notifView);
        view.frame.dispose();
    }

    private void openSettings() {
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
    }
}
