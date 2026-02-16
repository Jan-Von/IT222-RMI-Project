package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RiderDashboard {
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

    public RiderDashboard() {
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

        //left side bar

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

// ung gitna

        JLabel newsLabel = new JLabel("Urgent Pick-ups");
        newsLabel.setFont(new Font("Arial", Font.BOLD, 30));
        newsLabel.setForeground(new Color(20, 35, 100));
        newsLabel.setBounds(230, 100, 300, 30);
        frame.add(newsLabel);

        card1 = new JPanel();
        card1.setLayout(null);
        card1.setBounds(285, 150, 300, 303);
        card1.setBackground(new Color(20, 35, 100));
        card1.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel card1Title = new JLabel("Super Typhoon Haiyan");
        card1Title.setForeground(Color.WHITE);
        card1Title.setFont(new Font("Arial", Font.BOLD, 14));
        card1Title.setBounds(65, 10, 250, 20);
        card1.add(card1Title);

        JTextArea card1Text = new JTextArea("Donation Requests:         21");
        card1Text.setBounds(30, 230, 250, 50);
        card1Text.setFont(new Font("Arial", Font.PLAIN, 18));
        card1Text.setBackground(new Color(20, 35, 100));
        card1Text.setForeground(Color.WHITE);
        card1Text.setLineWrap(true);
        card1Text.setWrapStyleWord(true);
        card1Text.setEditable(false);
        card1.add(card1Text);

        frame.add(card1);

        card2 = new JPanel();
        card2.setLayout(null);
        card2.setBounds(605, 150, 300, 303);
        card2.setBackground(new Color(20, 35, 100));
        card2.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel card2Title = new JLabel("6.9 Magnitude in Cebu");
        card2Title.setForeground(Color.WHITE);
        card2Title.setFont(new Font("Arial", Font.BOLD, 14));
        card2Title.setBounds(65, 10, 250, 20);
        card2.add(card2Title);

        ImageIcon card2photo = new ImageIcon("Resources/Images/image2.png");
        scaledImg = card2photo.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel card2Photo = new JLabel(new ImageIcon(scaledImg));
        card2Photo.setBounds(0, 40, 299, 180);
        card2.add(card2Photo);

        JTextArea card2Text = new JTextArea("Donation Requests:         4");
        card2Text.setBounds(30, 230, 250, 50);
        card2Text.setFont(new Font("Arial", Font.PLAIN, 18));
        card2Text.setBackground(new Color(20, 35, 100));
        card2Text.setForeground(Color.WHITE);
        card2Text.setLineWrap(true);
        card2Text.setWrapStyleWord(true);
        card2Text.setEditable(false);
        card2.add(card2Text);

        frame.add(card2);

        card3 = new JPanel();
        card3.setLayout(null);
        card3.setBounds(925, 150, 300, 303);
        card3.setBackground(new Color(20, 35, 100));
        card3.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel card3Title = new JLabel("Fire Hits Supermarket in Quezon");
        card3Title.setForeground(Color.WHITE);
        card3Title.setFont(new Font("Arial", Font.BOLD, 14));
        card3Title.setBounds(40, 10, 250, 20);
        card3.add(card3Title);

        ImageIcon card3photo = new ImageIcon("Resources/Images/image3.png");
        scaledImg = card3photo.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel card3Photo = new JLabel(new ImageIcon(scaledImg));
        card3Photo.setBounds(0, 40, 299, 180);
        card3.add(card3Photo);

        JTextArea card3Text = new JTextArea("Donation Requests:        6");
        card3Text.setBounds(30, 230, 250, 50);
        card3Text.setFont(new Font("Arial", Font.PLAIN, 18));
        card3Text.setBackground(new Color(20, 35, 100));
        card3Text.setForeground(Color.WHITE);
        card3Text.setLineWrap(true);
        card3Text.setWrapStyleWord(true);
        card3Text.setEditable(false);
        card3.add(card3Text);

        frame.add(card3);

        //Other features below
        JLabel reqText = new JLabel("Requests");
        reqText.setFont(new Font("Arial", Font.BOLD, 17));
        reqText.setForeground(new Color(0, 0, 0));
        reqText.setBounds(225, 460, 300, 30);
        frame.add(reqText);

        JPanel requests = new JPanel();
        requests.setLayout(null);
        requests.setBounds(220, 490, 180, 250);
        requests.setBackground(new Color(245, 245, 245));
        requests.setBorder(new LineBorder(Color.WHITE));
        frame.add(requests);

        JLabel reqCount = new JLabel("42");
        reqCount.setForeground(Color.BLACK);
        reqCount.setFont(new Font("Arial", Font.BOLD, 60));
        reqCount.setBounds(55, 90, 250, 50);
        requests.add(reqCount);

        JLabel donInHandText = new JLabel("Donations in Hand");
        donInHandText.setFont(new Font("Arial", Font.BOLD, 17));
        donInHandText.setForeground(new Color(0, 0, 0));
        donInHandText.setBounds(425, 460, 300, 30);
        frame.add(donInHandText);

        JPanel donInHand = new JPanel();
        donInHand.setLayout(null);
        donInHand.setBounds(420, 490, 180, 250);
        donInHand.setBackground(new Color(245, 245, 245));
        donInHand.setBorder(new LineBorder(Color.WHITE));
        frame.add(donInHand);

        JLabel donInHandCount = new JLabel("34");
        donInHandCount.setForeground(Color.BLACK);
        donInHandCount.setFont(new Font("Arial", Font.BOLD, 60));
        donInHandCount.setBounds(55, 90, 250, 50);
        donInHand.add(donInHandCount);

        JLabel reportText = new JLabel("Incident Report");
        reportText.setFont(new Font("Arial", Font.BOLD, 17));
        reportText.setForeground(new Color(0, 0, 0));
        reportText.setBounds(625, 460, 300, 30);
        frame.add(reportText);

        JPanel report = new JPanel();
        report.setLayout(null);
        report.setBounds(620, 490, 180, 250);
        report.setBackground(new Color(245, 245, 245));
        report.setBorder(new LineBorder(Color.WHITE));
        frame.add(report);

        JLabel reportDropdown = new JLabel("None   ^");
        reportDropdown.setForeground(Color.BLACK);
        reportDropdown.setFont(new Font("Arial", Font.BOLD, 30));
        reportDropdown.setBounds(25, 90, 250, 50);
        report.add(reportDropdown);

        JPanel Loc = new JPanel();
        Loc.setLayout(null);
        Loc.setBounds(850, 490, 450, 250);
        Loc.setBackground(new Color(245, 245, 245));
        Loc.setBorder(new LineBorder(Color.WHITE));
        frame.add(Loc);

        JLabel locLabel = new JLabel("<html>Your Current <br> Location:");
        locLabel.setForeground(Color.BLACK);
        locLabel.setFont(new Font("Arial", Font.BOLD, 18));
        locLabel.setBounds(10, 25, 250, 100);
        Loc.add(locLabel);

        ImageIcon locphoto = new ImageIcon("Resources/Images/loc.png");
        scaledImg = locphoto.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel locphoto = new JLabel(new ImageIcon(scaledImg));
        locphoto.setBounds(40, 40, 299, 180);
        Loc.add(locphoto);

        JButton locUpdateBtn = new JButton();
        locUpdateBtn.setLayout(null);
        locUpdateBtn.setBounds(20, 105, 90, 70);
        locUpdateBtn.setBackground(new Color(20, 35, 100));
        locUpdateBtn.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
        Loc.add(locUpdateBtn);

        JLabel locUpdateText = new JLabel("UPDATE");
        locUpdateText.setForeground(Color.WHITE);
        locUpdateText.setFont(new Font("Arial", Font.BOLD, 15));
        locUpdateText.setBounds(14, 25, 80, 20);
        locUpdateBtn.add(locUpdateText);









        frame.setVisible(true);

    }

    public static void main(String[] args) {
        new RiderDashboard();
    }
}