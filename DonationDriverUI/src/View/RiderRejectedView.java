package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class RiderRejectedView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton notifBtn;
    public JButton myPickupsBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JButton acceptBtn;
    public JButton deliveredBtn;
    public JList<String> ticketsList;
    public JButton refreshBtn;

    public RiderRejectedView() {
        frame = new JFrame("DonationDriver");
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

        myPickupsBtn = new JButton("My Pickups");
        myPickupsBtn.setBounds(55, 140, 110, 40);
        myPickupsBtn.setBorderPainted(false);
        myPickupsBtn.setFocusPainted(false);
        myPickupsBtn.setBackground(Color.lightGray);
        sidebar.add(myPickupsBtn);

        ImageIcon Rider = new ImageIcon("Resources/Images/rider.png");
        scaledImg = Rider.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarRider = new JLabel(new ImageIcon(scaledImg));
        sidebarRider.setBounds(30, 145, 25, 25);
        sidebar.add(sidebarRider);

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

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBounds(200, 80, 1200, 720);
        mainContentPanel.setBackground(new Color(235, 237, 240));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(235, 237, 240));
        topBar.setBorder(new EmptyBorder(10, 300, 10, 300));

        JLabel newsFlash = new JLabel(
                " News Flash                    Super Typhoon Haiyan as the storm plowed across the...                                     ❗");
        newsFlash.setOpaque(true);
        newsFlash.setBackground(new Color(40, 60, 120));
        newsFlash.setForeground(Color.WHITE);
        newsFlash.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)));

        topBar.add(newsFlash, BorderLayout.CENTER);
        mainContentPanel.add(topBar, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(235, 237, 240));
        content.setBorder(new EmptyBorder(20, 300, 20, 300));

        // Tab panel
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tabPanel.setOpaque(false);

        JLabel rejectedTab = new JLabel("Rejected");
        rejectedTab.setBorder(new EmptyBorder(5, 10, 5, 10));
        rejectedTab.setForeground(Color.BLACK);
        rejectedTab.setFont(new Font("Arial", Font.BOLD, 14));

        acceptBtn = new JButton("Accepted");
        acceptBtn.setBorder(new EmptyBorder(5, 20, 5, 20));
        acceptBtn.setForeground(Color.GRAY);
        acceptBtn.setContentAreaFilled(false);
        acceptBtn.setFocusPainted(false);
        acceptBtn.setOpaque(true);
        acceptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        deliveredBtn = new JButton("Delivered");
        deliveredBtn.setBorder(new EmptyBorder(5, 20, 5, 20));
        deliveredBtn.setForeground(Color.GRAY);
        deliveredBtn.setContentAreaFilled(false);
        deliveredBtn.setFocusPainted(false);
        deliveredBtn.setOpaque(true);
        deliveredBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        tabPanel.add(rejectedTab);
        tabPanel.add(acceptBtn);
        tabPanel.add(deliveredBtn);

        content.add(tabPanel, BorderLayout.NORTH);

        JPanel ticketsPanel = new JPanel(new BorderLayout(5, 5));
        ticketsPanel.setOpaque(false);
        ticketsPanel.add(new JLabel("Rejected pickups (from server):"), BorderLayout.NORTH);
        ticketsList = new JList<>(new javax.swing.DefaultListModel<>());
        ticketsList.setFont(new Font("Arial", Font.PLAIN, 14));
        ticketsList.setBorder(new EmptyBorder(10, 10, 10, 10));

        ticketsPanel.add(new JScrollPane(ticketsList), BorderLayout.CENTER);

        JPanel ticketActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        refreshBtn = new JButton("Refresh");
        ticketActions.add(refreshBtn);
        ticketsPanel.add(ticketActions, BorderLayout.SOUTH);

        content.add(ticketsPanel, BorderLayout.CENTER);

        mainContentPanel.add(content, BorderLayout.CENTER);

        frame.add(mainContentPanel);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RiderRejectedView());
    }
}