package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;


public class RejectedSelectedUrgentPickUps {

    public JFrame frame;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JButton homeBtn;
    public JButton locUpdateBtn;
    public JPanel card1;
    public JPanel card2;
    public JPanel card3;

    public RejectedSelectedUrgentPickUps() {
        frame = new JFrame("DonationDriver - Dashboard");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(new Color(245, 245, 245));
        header.setBounds(0, 0, 1400, 80);

        JLabel title = new JLabel("DonationDriver");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(80, 18, 200, 20);
        header.add(title);

        JLabel subtitle = new JLabel("Accelerated Giving");
        subtitle.setFont(new Font("Arial", Font.BOLD, 12));
        subtitle.setForeground(new Color(20, 35, 100));
        subtitle.setBounds(80, 38, 200, 20);
        header.add(subtitle);

        // Search and Profile icons on top right
        ImageIcon searchImg = new ImageIcon("Resources/Images/search.png");
        Image scaledSearch = searchImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel searchIcon = new JLabel(new ImageIcon(scaledSearch));
        searchIcon.setBounds(1220, 25, 25, 25);
        header.add(searchIcon);

        ImageIcon profileImg = new ImageIcon("Resources/Images/profilepic.png");
        Image scaledProfile = profileImg.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel profileIcon = new JLabel(new ImageIcon(scaledProfile));
        profileIcon.setBounds(1340, 25, 25, 25);
        header.add(profileIcon);

        frame.add(header);

        //left sidebar

        JPanel sidebar = new JPanel();
        sidebar.setLayout(null);
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBounds(0, 80, 200, 720);

        homeBtn = new JButton("Home");
        homeBtn.setBounds(65, 40, 80, 40);
        homeBtn.setBorderPainted(false);
        homeBtn.setFocusPainted(false);
        homeBtn.setBackground(Color.lightGray);
        sidebar.add(homeBtn);

        ImageIcon Home = new ImageIcon("Resources/Images/home.png");
        Image scaledImg = Home.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarHome = new JLabel(new ImageIcon(scaledImg));
        sidebarHome.setBounds(30, 45, 25, 25);
        sidebar.add(sidebarHome);

        notifBtn = new JButton("Notifications");
        notifBtn.setBounds(55, 90, 110, 40);
        notifBtn.setBorderPainted(false);
        notifBtn.setFocusPainted(false);
        notifBtn.setContentAreaFilled(false);
        sidebar.add(notifBtn);

        ImageIcon Notif = new ImageIcon("Resources/Images/notification.png");
        scaledImg = Notif.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarNotif = new JLabel(new ImageIcon(scaledImg));
        sidebarNotif.setBounds(30, 95, 25, 25);
        sidebar.add(sidebarNotif);

        donationBtn = new JButton("Donations");
        donationBtn.setBounds(55, 140, 110, 40);
        donationBtn.setBorderPainted(false);
        donationBtn.setFocusPainted(false);
        donationBtn.setContentAreaFilled(false);
        sidebar.add(donationBtn);

        ImageIcon donation = new ImageIcon("Resources/Images/charity.png");
        scaledImg = donation.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarDonation = new JLabel(new ImageIcon(scaledImg));
        sidebarDonation.setBounds(30, 145, 25, 25);
        sidebar.add(sidebarDonation);

        DonateBtn = new JButton("Donate");
        DonateBtn.setBounds(45, 190, 120, 40);
        DonateBtn.setBorderPainted(false);
        DonateBtn.setFocusPainted(false);
        DonateBtn.setContentAreaFilled(false);
        sidebar.add(DonateBtn);

        ImageIcon donate = new ImageIcon("Resources/Images/heart.png");
        scaledImg = donate.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarDonate = new JLabel(new ImageIcon(scaledImg));
        sidebarDonate.setBounds(30, 195, 25, 25);
        sidebar.add(sidebarDonate);

        helpBtn = new JButton("Help");
        helpBtn.setBounds(45, 550, 120, 40);
        helpBtn.setBorderPainted(false);
        helpBtn.setFocusPainted(false);
        helpBtn.setContentAreaFilled(false);
        sidebar.add(helpBtn);

        settingsBtn = new JButton("Settings");
        settingsBtn.setBounds(45, 600, 120, 40);
        settingsBtn.setBorderPainted(false);
        settingsBtn.setFocusPainted(false);
        settingsBtn.setContentAreaFilled(false);
        sidebar.add(settingsBtn);

        ImageIcon help = new ImageIcon("Resources/Images/information.png");
        scaledImg = help.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel sidebarHelp = new JLabel(new ImageIcon(scaledImg));
        sidebarHelp.setBounds(27, 555, 30, 30);
        sidebar.add(sidebarHelp);

        ImageIcon setting = new ImageIcon("Resources/Images/settings.png");
        scaledImg = setting.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarSetting = new JLabel(new ImageIcon(scaledImg));
        sidebarSetting.setBounds(30, 605, 25, 25);
        sidebar.add(sidebarSetting);

        frame.add(sidebar);

//ung ano na alam mo na iyan


        JPanel urgentPick = new JPanel();
        urgentPick.setLayout(null);
        urgentPick.setBounds(480, 100, 650, 30);
        urgentPick.setBackground(new Color(20, 35, 100));
        urgentPick.setBorder(new LineBorder(Color.WHITE));
        frame.add(urgentPick);

        JLabel urgentPickText = new JLabel(" Urgent Pick-Ups                    Super Typhoon Haiyan as the storm plowed across the...");
        urgentPickText.setForeground(Color.WHITE);
        urgentPickText.setFont(new Font("Arial", Font.CENTER_BASELINE, 13));
        urgentPickText.setBounds(10, -10, 800, 50);
        urgentPick.add(urgentPickText);

        JPanel reqID = new JPanel();
        reqID.setLayout(null);
        reqID.setBounds(550, 170, 500, 285);
        reqID.setBackground(new Color(245, 245, 245));
        reqID.setBorder(new LineBorder(Color.WHITE));
        frame.add(reqID);

        JLabel reqIDText = new JLabel("<html>Name: Dela Cruz, Jaun <br> Mobile Number: 09346734629 <br> Boxes: 8");
        reqIDText.setForeground(Color.BLACK);
        reqIDText.setFont(new Font("Arial", Font.BOLD, 10));
        reqIDText.setBounds(10, -15, 250, 100);
        reqID.add(reqIDText);

        JLabel reqIDText2 = new JLabel("<html>PICK-UP LOCATION:<br>DSWD Field Office IV-A Alabang, Muntinlupa City<br><br><br>DROP-OF<br>Barangay Bayanan Hall, Muntinlupa City");
        reqIDText2.setForeground(Color.BLACK);
        reqIDText2.setFont(new Font("Arial", Font.BOLD, 10));
        reqIDText2.setBounds(10, 60, 250, 100);
        reqID.add(reqIDText2);

        JLabel reqIDText3 = new JLabel("Reasons: ");
        reqIDText3.setForeground(Color.GRAY);
        reqIDText3.setFont(new Font("Arial", Font.PLAIN, 10));
        reqIDText3.setBounds(10, 140, 250, 100);
        reqID.add(reqIDText3);

        JButton reqIDAcc2Btn = new JButton();
        reqIDAcc2Btn.setLayout(null);
        reqIDAcc2Btn.setBounds(395, 250, 85, 25);
        reqIDAcc2Btn.setBackground(new Color(192, 192, 192, 128));
        reqIDAcc2Btn.setBorder(null);
        reqID.add(reqIDAcc2Btn);

        JLabel reqIDAcc2Text = new JLabel("Send & Reject");
        reqIDAcc2Text.setForeground(Color.BLACK);
        reqIDAcc2Text.setFont(new Font("Arial", Font.PLAIN, 10));
        reqIDAcc2Text.setBounds(8, 1, 80, 20);
        reqIDAcc2Btn.add(reqIDAcc2Text);

        JButton callBtn = new JButton("📞");
        callBtn.setBackground(new Color(46, 204, 113));
        callBtn.setLayout(null);
        callBtn.setForeground(Color.WHITE);
        callBtn.setBounds(350, 20, 50, 27);
        callBtn.setBorder(null);
        reqID.add(callBtn);

        JButton chatBtn = new JButton("💬");
        chatBtn.setBackground(new Color(120, 140, 255));
        chatBtn.setLayout(null);
        chatBtn.setForeground(Color.WHITE);
        chatBtn.setBounds(410, 20, 50, 27);
        chatBtn.setBorder(null);
        reqID.add(chatBtn);

        JPanel reqID2 = new JPanel();
        reqID2.setLayout(null);
        reqID2.setBounds(550, 465, 500, 285);
        reqID2.setBackground(new Color(245, 245, 245));
        reqID2.setBorder(new LineBorder(Color.WHITE));
        frame.add(reqID2);

        JLabel reqID2Text = new JLabel("<html>Name: Gapuz, John Paul<br> Mobile Number: 0968345636<br> Boxes: 4");
        reqID2Text.setForeground(Color.BLACK);
        reqID2Text.setFont(new Font("Arial", Font.BOLD, 10));
        reqID2Text.setBounds(10, -15, 250, 100);
        reqID2.add(reqID2Text);

        JLabel reqID2Text2 = new JLabel("<html>PICK-UP LOCATION:<br>Philippine Red Cross Warehouse, Subic, Zambales<br><br><br>DROP-OF<br>BCastillejos National High School, Castillejos, Zambales");
        reqID2Text2.setForeground(Color.BLACK);
        reqID2Text2.setFont(new Font("Arial", Font.BOLD, 10));
        reqID2Text2.setBounds(10, 60, 250, 100);
        reqID2.add(reqID2Text2);

        JLabel reqID2Text3 = new JLabel("Reasons: ");
        reqID2Text3.setForeground(Color.GRAY);
        reqID2Text3.setFont(new Font("Arial", Font.PLAIN, 10));
        reqID2Text3.setBounds(10, 140, 250, 100);
        reqID2.add(reqID2Text3);

        JButton reqIDAccBtn = new JButton();
        reqIDAccBtn.setLayout(null);
        reqIDAccBtn.setBounds(395, 250, 85, 25);
        reqIDAccBtn.setBackground(new Color(192, 192, 192, 128));
        reqIDAccBtn.setBorder(null);
        reqID2.add(reqIDAccBtn);

        JLabel reqIDAccText = new JLabel("Send & Reject");
        reqIDAccText.setForeground(Color.BLACK);
        reqIDAccText.setFont(new Font("Arial", Font.PLAIN, 10));
        reqIDAccText.setBounds(8, 1, 80, 20);
        reqIDAccBtn.add(reqIDAccText);

        JButton call2Btn = new JButton("📞");
        call2Btn.setBackground(new Color(46, 204, 113));
        call2Btn.setLayout(null);
        call2Btn.setForeground(Color.WHITE);
        call2Btn.setBounds(350, 20, 50, 27);
        call2Btn.setBorder(null);
        reqID2.add(call2Btn);

        JButton chat2Btn = new JButton("💬");
        chat2Btn.setBackground(new Color(120, 140, 255));
        chat2Btn.setLayout(null);
        chat2Btn.setForeground(Color.WHITE);
        chat2Btn.setBounds(410, 20, 50, 27);
        chat2Btn.setBorder(null);
        reqID2.add(chat2Btn);









        frame.setVisible(true);

    }

    public static void main(String[] args) {
        new RejectedSelectedUrgentPickUps();
    }
}

