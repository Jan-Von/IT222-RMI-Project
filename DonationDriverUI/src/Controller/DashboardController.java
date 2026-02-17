package Controller;

import View.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DashboardController {

    private DashboardView view;
    private BoxDonationView viewBox;


    public DashboardController(DashboardView view) {
        this.view = view;
        view.monetaryBtn.addActionListener(e -> openMonetaryDonation());
        view.goodsBtn.addActionListener(e -> openBoxDonation());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e ->openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
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

    private void openHelp(){
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

}
