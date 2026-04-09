package View;

import javax.swing.*;
import java.awt.*;

public class RiderNotificationView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton notifBtn;
    public JButton myPickupsBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JPanel notificationsPanel;

    public RiderNotificationView() {
        frame = new JFrame("DonationDriver - Rider Notifications");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());
        frame.repaint();

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

        myPickupsBtn = new JButton("My Pickups");
        myPickupsBtn.setBounds(55, 140, 110, 40);
        myPickupsBtn.setBorderPainted(false);
        myPickupsBtn.setFocusPainted(false);
        myPickupsBtn.setContentAreaFilled(false);
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

        JPanel content = new JPanel();
        content.setLayout(null);
        content.setBounds(200, 80, 1200, 720);
        content.setBackground(Color.WHITE);

        JLabel label = new JLabel("Notifications");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setBounds(40, 20, 300, 30);
        content.add(label);

        notificationsPanel = new JPanel();
        notificationsPanel.setLayout(new BoxLayout(notificationsPanel, BoxLayout.Y_AXIS));
        notificationsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(notificationsPanel);
        scrollPane.setBounds(40, 70, 1100, 600);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scrollPane);

        frame.add(content);

        frame.setVisible(true);
    }
}
