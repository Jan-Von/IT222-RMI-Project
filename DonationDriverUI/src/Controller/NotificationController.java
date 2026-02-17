package Controller;

import View.*;

public class NotificationController {

    private NotificationView view;
    private BoxDonationView viewBox;

    public NotificationController(NotificationView view) {
        this.view = view;
        view.homeBtn.addActionListener(e -> {openDashboard();});
        view.donationBtn.addActionListener(e ->openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());


    }

    private void openDashboard(){
        DashboardView dashboardview = new DashboardView();
        new DashboardController(dashboardview);
        dashboardview.frame.setVisible(true);
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

    private void openHelp(){
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

}
