package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton monetaryBtn;
    public JButton goodsBtn;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton riderBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JPanel card1;
    public JPanel card2;
    public JPanel card3;

    public DashboardView() {
        frame = new JFrame("DonationDriver - Dashboard");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(new Color( 245, 245, 245));
        header.setBounds(0, 0, 1400, 80);

        ImageIcon logo = new ImageIcon("Resources/Images/logoicon.png");
        Image logoImg = logo.getImage().getScaledInstance(50, 40, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
        logoLabel.setBounds(20, 18, 50, 40);
        header.add(logoLabel);

        JLabel title = new JLabel("DonationDriver");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setBounds(80, 18, 200, 20);
        header.add(title);

        JLabel subtitle = new JLabel("Accelerated Giving");
        subtitle.setFont(new Font("Arial", Font.BOLD, 12));
        subtitle.setForeground(new Color(20, 35, 100));
        subtitle.setBounds(80, 38, 200, 20);
        header.add(subtitle);

        frame.add(header);

        /* ================= SIDEBAR ================= */
        JPanel sidebar = new JPanel();
        sidebar.setLayout(null);
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBounds(0, 80, 200, 720);

        homeBtn = new JButton("Home");
        homeBtn.setBounds(65, 40, 80, 40);
        homeBtn.setBackground(Color.lightGray);
        homeBtn.setBorderPainted(false);
        homeBtn.setFocusPainted(false);
        sidebar.add(homeBtn);

        ImageIcon Home = new ImageIcon("Resources/Images/home.png");
        Image scaledImg = Home.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarHome = new JLabel(new ImageIcon(scaledImg));
        sidebarHome.setBounds(30, 45, 25, 25);
        sidebar.add(sidebarHome);

        notifBtn = new JButton("Notifications");
        notifBtn.setBounds(45, 90, 120, 40);
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
        donationBtn.setBounds(45, 140, 120, 40);
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

        riderBtn = new JButton("Rider Mode");
        riderBtn.setBounds(45, 240, 120, 40);
        riderBtn.setBorderPainted(false);
        riderBtn.setFocusPainted(false);
        riderBtn.setContentAreaFilled(false);
        sidebar.add(riderBtn);

        try {
            ImageIcon riderIcon = new ImageIcon("Resources/Images/rider.png");
            if (riderIcon.getImage() != null) {
                scaledImg = riderIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
                JLabel sidebarRider = new JLabel(new ImageIcon(scaledImg));
                sidebarRider.setBounds(30, 245, 25, 25);
                sidebar.add(sidebarRider);
            }
        } catch (Exception ignored) { }

        helpBtn = new JButton("Help");
        helpBtn.setBounds(45,550,120,40);
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

        /* ================= MAIN CONTENT ================= */
        JLabel newsLabel = new JLabel("Donation Drives");
        newsLabel.setFont(new Font("Arial", Font.BOLD, 30));
        newsLabel.setForeground(new Color(20, 35, 100));
        newsLabel.setBounds(230, 100, 300, 30);
        frame.add(newsLabel);


        //Card 1
        card1 = new JPanel();
        card1.setLayout(null);
        card1.setBounds(220, 150, 300, 303);
        card1.setBackground(new Color(20, 35, 100));
        card1.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel card1Title = new JLabel("Super Typhoon Haiyan");
        card1Title.setForeground(Color.WHITE);
        card1Title.setFont(new Font("Arial", Font.BOLD, 14));
        card1Title.setBounds(65, 10, 250, 20);
        card1.add(card1Title);

        // Monetary and Box Donations Buttons
        monetaryBtn = new JButton("Monetary Donations");
        monetaryBtn.setBounds(75, 90, 150, 40);
        monetaryBtn.setOpaque(true);
        monetaryBtn.setBackground(Color.WHITE); // fully opaque
        monetaryBtn.setBorderPainted(false);
        monetaryBtn.setVisible(false);

        goodsBtn = new JButton("Goods Donations");
        goodsBtn.setBounds(75, 140, 150, 40);
        goodsBtn.setOpaque(true);
        goodsBtn.setBackground(Color.WHITE); // fully opaque
        goodsBtn.setBorderPainted(false);
        goodsBtn.setVisible(false);


        ImageIcon card1photo = new ImageIcon("Resources/Images/image1.png");
        scaledImg = card1photo.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel card1Photo = new JLabel(new ImageIcon(scaledImg));
        card1Photo.setBounds(0, 40, 299, 180);
        card1.add(card1Photo);

        JTextArea card1Text = new JTextArea("Super Typhoon Haiyan, as the storm plowed across the cluster of" +
                "islands in the heart of country...");
        card1Text.setBounds(30, 230, 250, 50);
        card1Text.setFont(new Font("Arial", Font.PLAIN, 13));
        card1Text.setBackground(new Color(20, 35, 100));
        card1Text.setForeground(Color.WHITE);
        card1Text.setLineWrap(true);
        card1Text.setWrapStyleWord(true);
        card1Text.setEditable(false);
        card1.add(card1Text);

        frame.add(card1);

        // Card 2
        card2 = new JPanel();
        card2.setLayout(null);
        card2.setBounds(540, 150, 300, 303);
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

        JTextArea card2Text = new JTextArea("The magnitude 6.9 quake struck late Tuesday off the northern tip of Cebu island near Bago City, according to...");
        card2Text.setBounds(30, 230, 250, 50);
        card2Text.setFont(new Font("Arial", Font.PLAIN, 13));
        card2Text.setBackground(new Color(20, 35, 100));
        card2Text.setForeground(Color.WHITE);
        card2Text.setLineWrap(true);
        card2Text.setWrapStyleWord(true);
        card2Text.setEditable(false);
        card2.add(card2Text);

        frame.add(card2);

        // Card 3
        card3 = new JPanel();
        card3.setLayout(null);
        card3.setBounds(860, 150, 300, 303);
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

        JTextArea card3Text = new JTextArea("A fire that reached fifth alarm gutted a Landers Supermarket branch in Barangay Pason Putik, Quezon City...");
        card3Text.setBounds(30, 230, 250, 50);
        card3Text.setFont(new Font("Arial", Font.PLAIN, 13));
        card3Text.setBackground(new Color(20, 35, 100));
        card3Text.setForeground(Color.WHITE);
        card3Text.setLineWrap(true);
        card3Text.setWrapStyleWord(true);
        card3Text.setEditable(false);
        card3.add(card3Text);

        frame.add(card3);

        JPanel cardBorder = new JPanel(null);
        cardBorder.setBackground(new Color(245, 245, 245));
        cardBorder.setBounds(210,135,960,340);
        frame.add(cardBorder);

        // Donations Side Bar
        JPanel donationPanel = new JPanel();
        donationPanel.setLayout(null);
        donationPanel.setBounds(1180, 50, 200, 800);
        donationPanel.setBackground(new Color(245, 245, 245));

        JLabel donationTitle = new JLabel("Donation Services");
        donationTitle.setFont(new Font("Arial", Font.BOLD, 14));
        donationTitle.setBounds(5, 25, 160, 20);
        donationPanel.add(donationTitle);

        ImageIcon livePhoto = new ImageIcon("Resources/Images/live photo.png");
        scaledImg = livePhoto.getImage().getScaledInstance(195, 150, Image.SCALE_SMOOTH);
        JLabel LivePhoto = new JLabel(new ImageIcon(scaledImg));
        LivePhoto.setBounds(5, 70, 195, 150);
        donationPanel.add(LivePhoto);

        ImageIcon liveicon = new ImageIcon("Resources/Images/liveIcon.png");
        scaledImg = liveicon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel liveIcon = new JLabel(new ImageIcon(scaledImg));
        liveIcon.setBounds(5, 50, 25, 25);
        donationPanel.add(liveIcon);

        String fullText =
                "Super Typhoon Haiyan (locally known as Yolanda) swept across the Philippines, " +
                        "generating a storm surge of more than 5 meters in places and winds in excess of 190 mph. " +
                        "Fifteen million people felt the effects of the storm directly. Across the nation, " +
                        "approximately 4.1 million people were displaced from their homes.";

        JTextArea donationText = new JTextArea(fullText);
        donationText.setBounds(5, 250, 195, 140);
        donationText.setFont(new Font("Arial", Font.PLAIN, 12));
        donationText.setForeground(Color.BLACK);
        donationText.setBackground(new Color(245, 245, 245));
        donationText.setLineWrap(true);
        donationText.setWrapStyleWord(true);
        donationText.setEditable(false);
        donationPanel.add(donationText);

        JPanel notifCard = new JPanel();
        notifCard.setLayout(null);
        notifCard.setBounds(450, 490, 450, 250);
        notifCard.setBackground(new Color(245, 245, 245));
        notifCard.setBorder(new LineBorder(Color.WHITE));
        frame.add(notifCard);


        JLabel notification = new JLabel("Notifications");
        notification.setForeground(Color.BLACK);
        notification.setFont(new Font("Arial", Font.BOLD, 18));
        notification.setBounds(10, 20, 250, 20);
        notifCard.add(notification);

        JLabel deliveryLocationNotification = new JLabel("Baguio City - Tacloban");
        deliveryLocationNotification.setForeground(Color.BLACK);
        deliveryLocationNotification.setFont(new Font("Arial", Font.PLAIN, 14));
        deliveryLocationNotification.setBounds(10, 48, 165, 20);
        notifCard.add(deliveryLocationNotification);

        JButton viewAllNotif = new JButton("View All");
        viewAllNotif.setBounds(350, 25, 100, 20);
        viewAllNotif.setForeground(Color.BLACK);
        viewAllNotif.setContentAreaFilled(false);
        viewAllNotif.setBorderPainted(false);
        notifCard.add(viewAllNotif);

        JLabel Transit = new JLabel("Donation Delivered");
        Transit.setForeground(Color.BLACK);
        Transit.setFont(new Font("Arial", Font.PLAIN, 14));
        Transit.setBounds(305, 40, 200, 35);
        notifCard.add(Transit);

        ImageIcon deliveryicon = new ImageIcon("Resources/Images/deliveryIcon.png");
        scaledImg = deliveryicon.getImage().getScaledInstance(65, 65, Image.SCALE_SMOOTH);
        JLabel deliveryIcon = new JLabel(new ImageIcon(scaledImg));
        deliveryIcon.setBounds(50, 125, 65, 65);
        notifCard.add(deliveryIcon);

        JLabel boxAmount = new JLabel("Box Amount: 3");
        boxAmount.setForeground(Color.BLACK);
        boxAmount.setFont(new Font("Arial", Font.BOLD, 14));
        boxAmount.setBounds(125, 125, 165, 35);
        notifCard.add(boxAmount);

        JButton transitStatus = new JButton("<html>3:09 PM Feb 21, Status: Delivered <br> Your donation has been successfully delivered</html>");
        transitStatus.setForeground(Color.BLACK);
        transitStatus.setBackground(Color.LIGHT_GRAY);
        transitStatus.setBorderPainted(false);
        transitStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        transitStatus.setBounds(120, 160, 280, 50);
        notifCard.add(transitStatus);

        frame.add(donationPanel);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new DashboardView();
    }

}
