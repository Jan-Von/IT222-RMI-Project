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
    public JPanel notificationListPanel;

    public NotificationView() {
        frame = new JFrame("DonationDriver - Notifications");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(new Color(245, 245, 245));
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

        //Notification List
        JLabel pageTitle = new JLabel("Recent Activity");
        pageTitle.setFont(new Font("Arial", Font.BOLD, 24));
        pageTitle.setForeground(new Color(20, 35, 100));
        pageTitle.setBounds(500, 100, 300, 40);
        frame.add(pageTitle);

        notificationListPanel = new JPanel();
        notificationListPanel.setLayout(new BoxLayout(notificationListPanel, BoxLayout.Y_AXIS));
        notificationListPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(notificationListPanel);
        scrollPane.setBounds(500, 150, 650, 550);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    public void clearNotifications() {
        notificationListPanel.removeAll();
        notificationListPanel.revalidate();
        notificationListPanel.repaint();
    }

    public void addNotificationCard(String location, String status, String message, String time, String category) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(600, 140));
        card.setMaximumSize(new Dimension(600, 140));
        card.setBackground(new Color(245, 245, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 0, 5, 0),
                new LineBorder(Color.LIGHT_GRAY, 1)
        ));

        JLabel locLbl = new JLabel(location);
        locLbl.setFont(new Font("Arial", Font.BOLD, 12));
        locLbl.setForeground(Color.GRAY);
        locLbl.setBounds(20, 10, 300, 20);
        card.add(locLbl);

        JLabel statusLbl = new JLabel(status);
        statusLbl.setFont(new Font("Arial", Font.BOLD, 12));
        if ("REJECTED".equalsIgnoreCase(status)) statusLbl.setForeground(Color.RED);
        else if ("PENDING".equalsIgnoreCase(status)) statusLbl.setForeground(Color.ORANGE);
        else statusLbl.setForeground(new Color(0, 150, 0));
        
        statusLbl.setBounds(450, 10, 140, 20);
        statusLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        card.add(statusLbl);

        String iconPath = "Resources/Images/food-donation.png";
        if ("Monetary".equalsIgnoreCase(category)) iconPath = "Resources/Images/moneyhand.png";
        
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel iconLbl = new JLabel(new ImageIcon(img));
        iconLbl.setBounds(20, 50, 50, 50);
        card.add(iconLbl);

        JLabel msgLbl = new JLabel("<html><b>" + time + "</b><br>" + message + "</html>");
        msgLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        msgLbl.setBounds(90, 45, 480, 60);
        card.add(msgLbl);

        notificationListPanel.add(card);
        notificationListPanel.add(Box.createVerticalStrut(10));
        notificationListPanel.revalidate();
        notificationListPanel.repaint();
    }
}
