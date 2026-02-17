package Controller;

import View.*;

public class RiderRejectedController {

    private final RiderRejectedView view;

    public RiderRejectedController(RiderRejectedView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e-> openHome());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.deliveredBtn.addActionListener(e -> openDelivered());
        view.acceptBtn.addActionListener(e -> openAccepted());
    }

    private void openDonations() {
        DonationsActiveView donationsActiveView = new DonationsActiveView();
        new DonationsActiveController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
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
        NotificationView donationsActiveView = new NotificationView();
        new NotificationController(donationsActiveView);
        donationsActiveView.frame.setVisible(true);
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

    private void openDelivered() {
        RiderDeliveredView deliveredView = new RiderDeliveredView();
        new RiderDeliveredController(deliveredView);
        deliveredView.frame.setVisible(true);
        view.frame.dispose();
    }
}
