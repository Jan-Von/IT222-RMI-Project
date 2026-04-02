package Admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardView {

    public static final String CARD_HOME = "HOME";
    public static final String CARD_NOTIFICATIONS = "NOTIFICATIONS";
    public static final String CARD_DONATIONS = "DONATIONS";

    public JFrame frame;
    public JButton logoutBtn;

    public JButton homeBtn;
    public JButton notificationsBtn;
    public JButton logsBtn;
    public JButton donationsBtn;
    public JButton helpBtn;
    public JButton settingsBtn;

    public JPanel contentPanel;
    public CardLayout cardLayout;

    public AdminHomePanel homePanel;
    public AdminNotificationsPanel notificationsPanel;
    public AdminDonationsPanel donationsPanel;

    public AdminDashboardView() {
        initFrame();
        initHeader();
        initSidebarAndContent();

        frame.setVisible(true);
    }

    private void initFrame() {
        frame = new JFrame("DonationDriver - Admin Dashboard");
        frame.setSize(1400, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());
    }

    private void initHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 245, 245));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(20, 35, 100));
        titlePanel.add(title);

        header.add(titlePanel, BorderLayout.WEST);

        logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            if (homePanel != null) homePanel.stopTimer();
            frame.dispose();
        });
        header.add(logoutBtn, BorderLayout.EAST);

        frame.add(header, BorderLayout.NORTH);
    }

    private void initSidebarAndContent() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel sidebar = buildSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        homePanel = new AdminHomePanel();
        homePanel.setOnShowNotifications(() -> showCard(CARD_NOTIFICATIONS));
        homePanel.setOnShowDonations(() -> showCard(CARD_DONATIONS));
        notificationsPanel = new AdminNotificationsPanel();
        donationsPanel = new AdminDonationsPanel();

        contentPanel.add(homePanel, CARD_HOME);
        contentPanel.add(notificationsPanel, CARD_NOTIFICATIONS);
        contentPanel.add(donationsPanel, CARD_DONATIONS);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.CENTER);

        showCard(CARD_HOME);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));

        homeBtn = createSidebarButton("Home");
        notificationsBtn = createSidebarButton("Notifications");
        donationsBtn = createSidebarButton("Donations");
        donationsBtn.setFont(new Font("Arial", Font.BOLD, 14));
        donationsBtn.setForeground(new Color(20, 35, 100));
        helpBtn = createSidebarButton("Help");
        settingsBtn = createSidebarButton("Settings");

        homeBtn.addActionListener(e -> showCard(CARD_HOME));
        notificationsBtn.addActionListener(e -> showCard(CARD_NOTIFICATIONS));
        donationsBtn.addActionListener(e -> showCard(CARD_DONATIONS));

        sidebar.add(homeBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(notificationsBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(donationsBtn);

        sidebar.add(Box.createVerticalGlue());

        sidebar.add(helpBtn);
        sidebar.add(Box.createVerticalStrut(5));
        sidebar.add(settingsBtn);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(new Color(30, 30, 30));
        return button;
    }

    public void showCard(String cardName) {
        if (cardLayout != null && contentPanel != null) {
            if (CARD_HOME.equals(cardName) && homePanel != null) {
                homePanel.refreshData();
            } else if (CARD_NOTIFICATIONS.equals(cardName) && notificationsPanel != null) {
                notificationsPanel.refreshData();
            } else if (CARD_DONATIONS.equals(cardName) && donationsPanel != null) {
                donationsPanel.refreshData();
            }

            cardLayout.show(contentPanel, cardName);
        }
    }

    public static void main(String[] args) {
        new AdminDashboardView();
    }

}
