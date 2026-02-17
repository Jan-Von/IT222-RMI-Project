package Controller;

import View.*;

public class RiderAcceptedController {

    private final RiderAcceptedView view;

    public RiderAcceptedController(RiderAcceptedView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.homeBtn.addActionListener(e -> openHome());
        view.deliveredBtn.addActionListener(e-> openDelivered());
        view.rejectBtn.addActionListener(e -> openRejected());
    }

    private void openDonations() {
        DonationsActiveView donationsActiveView = new DonationsActiveView();
        new DonationsActiveController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openRejected() {
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDelivered() {
        RiderDeliveredView deliveredView = new RiderDeliveredView();
        new RiderDeliveredController(deliveredView);
        deliveredView.frame.setVisible(true);
        view.frame.dispose();
    }
}
