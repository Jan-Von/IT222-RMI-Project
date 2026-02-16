package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class RiderDeliveredView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton helpBtn;
    public JButton settingsBtn;

    public RiderDeliveredView() {
        frame = new JFrame("DonationDriver - Dashboard");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        // ================= HEADER (FROM ORIGINAL DASHBOARDVIEW) =================
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

        // ================= SIDEBAR (FROM ORIGINAL DASHBOARDVIEW) =================
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

        donationBtn = new JButton("Rider");
        donationBtn.setBounds(45, 140, 120, 40);
        donationBtn.setBorderPainted(false);
        donationBtn.setFocusPainted(false);
        donationBtn.setContentAreaFilled(false);
        sidebar.add(donationBtn);

        ImageIcon Rider = new ImageIcon("Resources/Images/rider.png");
        scaledImg = Rider.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        JLabel sidebarRider = new JLabel(new ImageIcon(scaledImg));
        sidebarRider.setBounds(30, 145, 25, 25);
        sidebar.add(sidebarRider);

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

        // ================= CENTER CONTENT (FROM RIDERREJECTEDVIEW) =================
        // Create main panel for center content
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBounds(200, 80, 1200, 720);
        mainContentPanel.setBackground(new Color(235, 237, 240));

        // Top bar with News Flash
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(235, 237, 240));
        topBar.setBorder(new EmptyBorder(10, 300, 10, 300));

        JLabel newsFlash = new JLabel(" News Flash                    Super Typhoon Haiyan as the storm plowed across the...                                     ❗");
        newsFlash.setOpaque(true);
        newsFlash.setBackground(new Color(40, 60, 120));
        newsFlash.setForeground(Color.WHITE);
        newsFlash.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));

        topBar.add(newsFlash, BorderLayout.CENTER);

        mainContentPanel.add(topBar, BorderLayout.NORTH);

        // Content panel with tabs and cards
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(235, 237, 240));
        content.setBorder(new EmptyBorder(20, 300, 20, 300));

        // Tab panel
        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tabPanel.setOpaque(false);

        JLabel rejectedTab = new JLabel("Rejected");
        rejectedTab.setBorder(new EmptyBorder(5, 10, 5, 10));
        rejectedTab.setForeground(Color.GRAY);

        JLabel acceptedTab = new JLabel("Accepted");
        acceptedTab.setBorder(new EmptyBorder(5, 20, 5, 20));
        acceptedTab.setForeground(Color.GRAY);

        JLabel deliveredTab = new JLabel("Delivered");
        deliveredTab.setBorder(new EmptyBorder(5, 20, 5, 20));
        deliveredTab.setForeground(Color.BLACK);
        deliveredTab.setFont(new Font("Arial", Font.BOLD, 14));

        tabPanel.add(rejectedTab);
        tabPanel.add(acceptedTab);
        tabPanel.add(deliveredTab);

        content.add(tabPanel, BorderLayout.NORTH);

        // Cards container
        JPanel cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);

        cardsContainer.add(createDonationCard(
                "Dela Cruz, Juan",
                "09984567123",
                "8",
                "DSWD Field Office IV-A, Alabang, Muntinlupa City",
                "Barangay Bayanan Hall, Muntinlupa City",
                "Resources/Images/donationcard1.png"));

        cardsContainer.add(Box.createVerticalStrut(15));

        cardsContainer.add(createDonationCard(
                "Gapuz, John Paul",
                "0919239876",
                "15",
                "Philippine Red Cross Warehouse, Subic, Zambales",
                "Castillejos National High School, Castillejos, Zambales",
                "Resources/Images/donationcard2.png"));

        JScrollPane scrollPane = new JScrollPane(cardsContainer);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(235, 237, 240));

        content.add(scrollPane, BorderLayout.CENTER);

        mainContentPanel.add(content, BorderLayout.CENTER);

        frame.add(mainContentPanel);

        frame.setVisible(true);
    }

    // ================= CREATE DONATION CARD (FROM RIDERDELIVEREDEDVIEW) =================
    private JPanel createDonationCard(String name, String mobile, String boxes,
                                      String pickup, String dropoff, String imagePath) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        infoPanel.add(new JLabel("Name: " + name));
        infoPanel.add(new JLabel("Mobile Number: " + mobile));
        infoPanel.add(new JLabel("Boxes: " + boxes));
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("PICK UP LOCATION:"));
        infoPanel.add(new JLabel(pickup));
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("DROP-OFF:"));
        infoPanel.add(new JLabel(dropoff));

        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton callBtn = new JButton("📞");
        callBtn.setBackground(new Color(46, 204, 113));
        callBtn.setForeground(Color.WHITE);
        callBtn.setFocusPainted(false);
        callBtn.setBorderPainted(false);

        JButton msgBtn = new JButton("💬");
        msgBtn.setBackground(new Color(120, 140, 255));
        msgBtn.setForeground(Color.WHITE);
        msgBtn.setFocusPainted(false);
        msgBtn.setBorderPainted(false);

        buttonPanel.add(callBtn);
        buttonPanel.add(msgBtn);

        rightPanel.add(buttonPanel);
        rightPanel.add(Box.createVerticalStrut(10));

        // Image panel with border
        JPanel imagePanel = new JPanel();
        imagePanel.setOpaque(false);
        imagePanel.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        imagePanel.setPreferredSize(new Dimension(150, 100));
        imagePanel.setMaximumSize(new Dimension(550, 100));
        imagePanel.setLayout(new BorderLayout());

        // Load the specific image for this card
        ImageIcon imgIcon = new ImageIcon(imagePath);
        if (imgIcon.getIconWidth() > 0 && imgIcon.getIconHeight() > 0) {
            Image scaledImg = imgIcon.getImage().getScaledInstance(150, 100, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaledImg));
            imagePanel.add(imgLabel, BorderLayout.CENTER);
        } else {
            JLabel placeholder = new JLabel("Image");
            placeholder.setHorizontalAlignment(SwingConstants.CENTER);
            imagePanel.add(placeholder, BorderLayout.CENTER);
        }

        rightPanel.add(imagePanel);
        rightPanel.add(Box.createVerticalStrut(10));

        JLabel status = new JLabel("Delivered");
        status.setOpaque(true);
        status.setBackground(new Color(200, 200, 200));
        status.setBorder(new EmptyBorder(5, 10, 5, 10));

        rightPanel.add(status);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    // ================= MAIN METHOD =================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RiderDeliveredView();
            }
        });
    }
}