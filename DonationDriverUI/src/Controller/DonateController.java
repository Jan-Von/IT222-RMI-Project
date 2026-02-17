package Controller;

import View.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DonateController {

    private DonateView view;
    private BoxDonationView viewBox;

    public DonateController(DonateView view) {
        this.view = view;

        view.monetaryBtn.addActionListener(e -> openMonetaryDonation());
        view.goodsBtn.addActionListener(e -> openBoxDonation());
        view.homeBtn.addActionListener(e -> {openDashboard();});
        view.notifBtn.addActionListener(e -> openNotificaton());
        view.donationBtn.addActionListener(e ->openDonations());
        view.helpBtn.addActionListener(e -> openHelp());


        setupCardClick(view.card1);
        setupCardClick(view.card2);
        setupCardClick(view.card3);
    }

    private void setupCardClick(JPanel card) {
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                // Remove from old parent
                if (view.monetaryBtn.getParent() != null) {
                    view.monetaryBtn.getParent().remove(view.monetaryBtn);
                    view.goodsBtn.getParent().remove(view.goodsBtn);
                }

                // Add to the clicked card
                card.add(view.monetaryBtn);
                card.add(view.goodsBtn);

                // Show buttons
                view.monetaryBtn.setVisible(true);
                view.goodsBtn.setVisible(true);

                // Refresh card
                card.revalidate();
                card.repaint();
                view.frame.revalidate();
                view.frame.repaint();
            }
        });
    }

    private void openMonetaryDonation() {
        MonetaryDonationView moneyView = new MonetaryDonationView();
        new MonetaryDonationController(moneyView);
        moneyView.frame.setVisible(true); // open new frame
        view.frame.dispose(); // close current dashboard
    }

    private void openBoxDonation(){
        BoxDonationView boxView = new BoxDonationView();
        new BoxDonationController(boxView);
        boxView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDashboard(){
        DashboardView dashboardview = new DashboardView();
        new DashboardController(dashboardview);
        dashboardview.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotificaton (){
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

    private void openHelp(){
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }


}
