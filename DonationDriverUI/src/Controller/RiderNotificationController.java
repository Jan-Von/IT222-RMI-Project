package Controller;

import View.RiderDashboard;
import View.RiderNotificationView;
import View.SettingsView;
import View.RiderAcceptedView;

public class RiderNotificationController {

    private RiderNotificationView view;

    public RiderNotificationController(RiderNotificationView view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> openHome());
        view.myPickupsBtn.addActionListener(e -> openPickups());
        view.helpBtn.addActionListener(e -> openHelp());
        view.settingsBtn.addActionListener(e -> openSettings());
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

    private void openHelp() {
        View.RiderHelpView helpView = new View.RiderHelpView();
        new RiderHelpController(helpView);
        view.frame.dispose();
    }

    private void openSettings() {
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
    }
}
