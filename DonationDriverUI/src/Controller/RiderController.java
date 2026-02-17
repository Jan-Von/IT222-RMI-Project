package Controller;

import View.*;
import Network.Client;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class RiderController {

    private final RiderDashboard view;

    public RiderController(RiderDashboard view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> goHome());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());

        view.frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setRiderUnavailable();
            }
        });

        setRiderAvailable();
    }

    private void setRiderAvailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) return;
        try {
            Client client = Client.getDefault();
            String responseXml = client.setRiderAvailable(userId);
            Client.Response resp = Client.parseResponse(responseXml);
            if (resp == null || !resp.isOk()) {
                System.err.println("Rider set available failed: " + (resp != null ? resp.message : "null"));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setRiderUnavailable() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) return;
        try {
            Client client = Client.getDefault();
            client.setRiderUnavailable(userId);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void goHome() {
        setRiderUnavailable();
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification(){
        setRiderUnavailable();
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
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

    private void openHelp() {
        setRiderUnavailable();
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

}
