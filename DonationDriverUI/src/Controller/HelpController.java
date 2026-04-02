package Controller;

import View.*;

public class HelpController {

    private HelpView view;
    private BoxDonationView viewBox;


    public HelpController(HelpView view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> openDashBoard());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e ->openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.settingsBtn.addActionListener(e -> openSettings());

    }

    private void openSettings(){
        SettingsView Settingview = new SettingsView();
        new SettingsController(Settingview);
        Settingview.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDashBoard(){
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification (){
        NotificationView notifView = new NotificationView();
        new NotificationController(notifView);
        notifView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations(){
        DonationsActiveView donationsView = new DonationsActiveView();
        new DonationsActiveController(donationsView);
        donationsView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate (){
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

}
