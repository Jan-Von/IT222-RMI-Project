package Controller;

import View.*;

public class RiderDeliveredController {

    private final RiderDeliveredView view;

    public RiderDeliveredController(RiderDeliveredView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e-> openHome());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.acceptBtn.addActionListener(e -> openAccepted());
        view.rejectBtn.addActionListener(e -> openRejected());
    }

    private void openDonations() {
        DonationsActiveView donationsActiveView = new DonationsActiveView();
        new DonationsActiveController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        NotificationView dashboardView = new NotificationView();
        new NotificationController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openAccepted() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openRejected() {
        RiderRejectedView rejectedView = new RiderRejectedView();
        new RiderRejectedController(rejectedView);
        rejectedView.frame.setVisible(true);
        view.frame.dispose();
    }
}
