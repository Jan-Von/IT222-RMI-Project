package Controller;

import View.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DonateController {

    private DonateView view;
    private String selectedOrg;

    public DonateController(DonateView view) {
        this.view = view;

        view.monetaryBtn.addActionListener(e -> openMonetaryDonation());
        view.goodsBtn.addActionListener(e -> openBoxDonation());
        view.homeBtn.addActionListener(e -> {
            openDashboard();
        });
        view.notifBtn.addActionListener(e -> openNotificaton());
        view.donationBtn.addActionListener(e -> openDonations());
        view.settingsBtn.addActionListener(e -> openSettings());

        view.helpBtn.addActionListener(e -> openHelp());

        setupCardClick(view.card1, "The Sunflower Center");
        setupCardClick(view.card2, "The Children's Home of Eucharistic Love");
        setupCardClick(view.card3, "Maharlika Charity Foundation Inc.");
    }

    private void setupCardClick(JPanel card, String orgName) {
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                selectedOrg = orgName;
                if (view.monetaryBtn.getParent() != null) {
                    view.monetaryBtn.getParent().remove(view.monetaryBtn);
                    view.goodsBtn.getParent().remove(view.goodsBtn);
                }

                card.add(view.monetaryBtn);
                card.add(view.goodsBtn);

                view.monetaryBtn.setVisible(true);
                view.goodsBtn.setVisible(true);

                card.revalidate();
                card.repaint();
                view.frame.revalidate();
                view.frame.repaint();
            }
        });
    }

    private void openSettings(){
        SettingsView Settingview = new SettingsView();
        new SettingsController(Settingview);
        Settingview.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openMonetaryDonation() {
        MonetaryDonationView moneyView = new MonetaryDonationView();
        new MonetaryDonationController(moneyView, selectedOrg);
        moneyView.frame.setVisible(true); // open new frame
        view.frame.dispose(); // close current dashboard
    }

    private void openBoxDonation() {
        BoxDonationView boxView = new BoxDonationView();
        new BoxDonationController(boxView, selectedOrg);
        boxView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDashboard() {
        DashboardView dashboardview = new DashboardView();
        new DashboardController(dashboardview);
        dashboardview.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotificaton() {
        NotificationView notifView = new NotificationView();
        new NotificationController(notifView);
        notifView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations() {
        DonationsActiveView donationsView = new DonationsActiveView();
        new DonationsActiveController(donationsView);
        donationsView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

}
