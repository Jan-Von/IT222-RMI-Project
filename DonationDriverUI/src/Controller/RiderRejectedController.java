package Controller;

import View.*;
import Network.Client;
import java.io.IOException;

public class RiderRejectedController {

    private final RiderRejectedView view;

    public RiderRejectedController(RiderRejectedView view) {
        this.view = view;
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.homeBtn.addActionListener(e -> openHome());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());
        view.deliveredBtn.addActionListener(e -> openDelivered());
        view.acceptBtn.addActionListener(e -> openAccepted());
    }

    private void openDonations() {
        RiderAcceptedView acceptedView = new RiderAcceptedView();
        new RiderAcceptedController(acceptedView);
        acceptedView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        setRiderUnavailable();
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHome() {
        setRiderUnavailable();
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        setRiderUnavailable();
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void setRiderUnavailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) return;
        try {
            Client.getDefault().setRiderUnavailable(userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void openHelp() {
        setRiderUnavailable();
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
