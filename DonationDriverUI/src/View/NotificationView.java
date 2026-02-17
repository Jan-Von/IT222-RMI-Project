package View;

import javax.management.Notification;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class NotificationView {
    public JFrame frame;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JButton homeBtn;

    public NotificationView() {
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
        homeBtn.setContentAreaFilled(false);
        homeBtn.setBorderPainted(false);
        homeBtn.setFocusPainted(false);
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
        notifBtn.setBackground(Color.lightGray);
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

        helpBtn = new JButton("Help");
        helpBtn.setBounds(45,550,120,40);
        helpBtn.setBorderPainted(false);
        helpBtn.setFocusPainted(false);
        helpBtn.setContentAreaFilled(false);
        sidebar.add(helpBtn);

        JButton settingsBtn = new JButton("Settings");
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

        JPanel Notif1Card = new JPanel();
        Notif1Card.setLayout(null);
        Notif1Card.setBounds(500, 100, 600, 150);
        Notif1Card.setBackground(new Color(245, 245, 245));
        frame.add(Notif1Card);

        JLabel Location1 = new JLabel("Baguio City - Tacloban");
        Location1.setFont(new Font("Arial", Font.BOLD, 12));
        Location1.setForeground(Color.GRAY);
        Location1.setBounds(20, 10, 200, 20);
        Notif1Card.add(Location1);

        JLabel deliveryStatus1 = new JLabel("Donation Delivered");
        deliveryStatus1.setFont(new Font("Arial", Font.BOLD, 12));
        deliveryStatus1.setForeground(Color.GRAY);
        deliveryStatus1.setBounds(480, 10, 200, 20);
        Notif1Card.add(deliveryStatus1);

        ImageIcon goodsDonate = new ImageIcon("Resources/Images/food-donation.png");
        scaledImg = goodsDonate.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel goodsDonation = new JLabel(new ImageIcon(scaledImg));
        goodsDonation.setBounds(100, 65, 50, 50);
        Notif1Card.add(goodsDonation);

        JLabel boxAmount = new JLabel("Box Amount: 3");
        boxAmount.setForeground(Color.BLACK);
        boxAmount.setFont(new Font("Arial", Font.BOLD, 14));
        boxAmount.setBounds(175, 75, 100, 35);
        Notif1Card.add(boxAmount);

        JButton transitStatus = new JButton("<html>3:09 PM Feb 21, Status: Delivered <br> Your donation has been successfully delivered</html>");
        transitStatus.setForeground(Color.BLACK);
        transitStatus.setBackground(Color.LIGHT_GRAY);
        transitStatus.setBorderPainted(false);
        transitStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        transitStatus.setBounds(300, 75, 280, 50);
        Notif1Card.add(transitStatus);

        JPanel Notif2Card = new JPanel();
        Notif2Card.setLayout(null);
        Notif2Card.setBounds(500, 275, 600, 225);
        Notif2Card.setBackground(new Color(245, 245, 245));
        frame.add(Notif2Card);

        JLabel Location2 = new JLabel("Quezon City");
        Location2.setFont(new Font("Arial", Font.BOLD, 12));
        Location2.setForeground(Color.GRAY);
        Location2.setBounds(20, 10, 200, 20);
        Notif2Card.add(Location2);

        JLabel newFlash = new JLabel("News Flash");
        newFlash.setFont(new Font("Arial", Font.BOLD, 12));
        newFlash.setForeground(Color.GRAY);
        newFlash.setBounds(525, 10, 200, 20);
        Notif2Card.add(newFlash);

        JPanel Notif2Main = new JPanel();
        Notif2Main.setLayout(null);
        Notif2Main.setBounds(150, 12, 300, 25);
        Notif2Main.setBackground(new Color(20, 35, 100));
        Notif2Main.setBorder(new LineBorder(Color.LIGHT_GRAY));
        Notif2Card.add(Notif2Main);

        JLabel Notif2Title = new JLabel("5th alarm fire hits supermarket in Quezon...");
        Notif2Title.setForeground(Color.WHITE);
        Notif2Title.setFont(new Font("Arial", Font.PLAIN, 14));
        Notif2Title.setBounds(10, 7, 300, 10);
        Notif2Main.add(Notif2Title);

        ImageIcon card3photo = new ImageIcon("Resources/Images/image3.png");
        scaledImg = card3photo.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel card3Photo = new JLabel(new ImageIcon(scaledImg));
        card3Photo.setBounds(150, 40, 300, 180);
        Notif2Card.add(card3Photo);

        JPanel Notif3Card = new JPanel();
        Notif3Card.setLayout(null);
        Notif3Card.setBounds(500, 525, 600, 150);
        Notif3Card.setBackground(new Color(245, 245, 245));
        frame.add(Notif3Card);

        JLabel Location3 = new JLabel("Baguio City - Tacloban");
        Location3.setFont(new Font("Arial", Font.BOLD, 12));
        Location3.setForeground(Color.GRAY);
        Location3.setBounds(20, 10, 200, 20);
        Notif3Card.add(Location3);

        JLabel deliveryStatus3 = new JLabel("Donation Delivered");
        deliveryStatus3.setFont(new Font("Arial", Font.BOLD, 12));
        deliveryStatus3.setForeground(Color.GRAY);
        deliveryStatus3.setBounds(480, 10, 200, 20);
        Notif3Card.add(deliveryStatus3);

        ImageIcon moneyDonate = new ImageIcon("Resources/Images/moneyhand.png");
        scaledImg = moneyDonate.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel moneyDonation = new JLabel(new ImageIcon(scaledImg));
        moneyDonation.setBounds(100, 65, 50, 50);
        Notif3Card.add(moneyDonation);

        JLabel moneyAmount = new JLabel("₱10,000.00");
        moneyAmount.setForeground(Color.BLACK);
        moneyAmount.setFont(new Font("Arial", Font.BOLD, 14));
        moneyAmount.setBounds(185, 75, 100, 35);
        Notif3Card.add(moneyAmount);

        JButton moneyStatus = new JButton("<html>4:32 PM Feb. 15 Success <br> Your Cash Donation has been Received.");
        moneyStatus.setForeground(Color.BLACK);
        moneyStatus.setBackground(Color.LIGHT_GRAY);
        moneyStatus.setBorderPainted(false);
        moneyStatus.setFont(new Font("Arial", Font.PLAIN, 14));
        moneyStatus.setBounds(300, 65, 280, 50);
        Notif3Card.add(moneyStatus);

        frame.setVisible(true);
    }
}
