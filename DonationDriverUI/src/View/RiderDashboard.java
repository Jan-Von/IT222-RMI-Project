package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RiderDashboard {
    public JFrame frame;
    public JButton notifBtn;

    public JButton myPickupsBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JButton homeBtn;
    public JButton locUpdateBtn;
    public JToggleButton onlineToggle;
    public JPanel cardsPanel;
    public JLabel reqCount;
    public JLabel donInHandCount;

    public RiderDashboard() {
        frame = new JFrame("DonationDriver - Rider Dashboard");
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

        onlineToggle = new JToggleButton("Online (Receiving Requests)");
        onlineToggle.setBounds(980, 20, 220, 40);
        onlineToggle.setBackground(new Color(0, 150, 0));
        onlineToggle.setForeground(Color.WHITE);
        onlineToggle.setFont(new Font("Arial", Font.BOLD, 12));
        onlineToggle.setFocusPainted(false);
        onlineToggle.setSelected(true);
        header.add(onlineToggle);

        frame.add(header);

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

        JLabel newsLabel = new JLabel("Donation Drives");
        newsLabel.setFont(new Font("Arial", Font.BOLD, 30));
        newsLabel.setForeground(new Color(20, 35, 100));
        newsLabel.setBounds(230, 100, 300, 30);
        frame.add(newsLabel);

        cardsPanel = new JPanel();
        cardsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 5));
        cardsPanel.setBackground(Color.WHITE);

        JScrollPane cardsScroll = new JScrollPane(cardsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cardsScroll.setBounds(230, 145, 1100, 310);
        cardsScroll.setBorder(null);
        cardsScroll.getViewport().setBackground(Color.WHITE);
        frame.add(cardsScroll);

        JLabel reqText = new JLabel("Requests");
        reqText.setFont(new Font("Arial", Font.BOLD, 17));
        reqText.setForeground(new Color(0, 0, 0));
        reqText.setBounds(225, 470, 300, 30);
        frame.add(reqText);

        JPanel requests = new JPanel();
        requests.setLayout(null);
        requests.setBounds(220, 500, 180, 220);
        requests.setBackground(new Color(245, 245, 245));
        requests.setBorder(new LineBorder(Color.WHITE));
        frame.add(requests);

        reqCount = new JLabel("0");
        reqCount.setForeground(Color.BLACK);
        reqCount.setFont(new Font("Arial", Font.BOLD, 60));
        reqCount.setBounds(55, 90, 250, 50);
        requests.add(reqCount);

        JLabel donInHandText = new JLabel("Donations in Hand");
        donInHandText.setFont(new Font("Arial", Font.BOLD, 17));
        donInHandText.setForeground(new Color(0, 0, 0));
        donInHandText.setBounds(425, 470, 300, 30);
        frame.add(donInHandText);

        JPanel donInHand = new JPanel();
        donInHand.setLayout(null);
        donInHand.setBounds(420, 500, 180, 220);
        donInHand.setBackground(new Color(245, 245, 245));
        donInHand.setBorder(new LineBorder(Color.WHITE));
        frame.add(donInHand);

        donInHandCount = new JLabel("0");
        donInHandCount.setForeground(Color.BLACK);
        donInHandCount.setFont(new Font("Arial", Font.BOLD, 60));
        donInHandCount.setBounds(55, 90, 250, 50);
        donInHand.add(donInHandCount);

        JLabel reportText = new JLabel("Incident Report");
        reportText.setFont(new Font("Arial", Font.BOLD, 17));
        reportText.setForeground(new Color(0, 0, 0));
        reportText.setBounds(625, 470, 300, 30);
        frame.add(reportText);

        JPanel report = new JPanel();
        report.setLayout(null);
        report.setBounds(620, 500, 180, 220);
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
        Loc.setBounds(850, 500, 450, 220);
        Loc.setBackground(new Color(245, 245, 245));
        Loc.setBorder(new LineBorder(Color.WHITE));
        frame.add(Loc);

        JLabel locLabel = new JLabel("<html>Your Current <br> Location:");
        locLabel.setForeground(Color.BLACK);
        locLabel.setFont(new Font("Arial", Font.BOLD, 18));
        locLabel.setBounds(10, 25, 250, 100);
        Loc.add(locLabel);

        locUpdateBtn = new JButton();
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

        ImageIcon locphoto = new ImageIcon("Resources/Images/loc.png");
        scaledImg = locphoto.getImage().getScaledInstance(299, 180, Image.SCALE_SMOOTH);
        JLabel Locphoto = new JLabel(new ImageIcon(scaledImg));
        Locphoto.setBounds(150, 40, 299, 180);
        Loc.add(Locphoto);

        frame.setVisible(true);
    }

    public static class DriveCardRefs {
        public final JLabel countLabel;
        public final JButton actionBtn;

        public DriveCardRefs(JLabel countLabel, JButton actionBtn) {
            this.countLabel = countLabel;
            this.actionBtn = actionBtn;
        }
    }

    public DriveCardRefs addDriveCard(String titleText, String imgPath) {
        JPanel card = new JPanel();
        card.setLayout(null);
        card.setPreferredSize(new Dimension(300, 303));
        card.setBackground(new Color(20, 35, 100));
        card.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel title = new JLabel(titleText);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setBounds(20, 10, 260, 20);
        card.add(title);

        JLabel countLbl = new JLabel("0 donations available");
        countLbl.setForeground(Color.YELLOW);
        countLbl.setFont(new Font("Arial", Font.ITALIC, 12));
        countLbl.setBounds(20, 180, 260, 20);
        card.add(countLbl);

        ImageIcon icon = new ImageIcon(imgPath);
        Image img = icon.getImage().getScaledInstance(299, 140, Image.SCALE_SMOOTH);
        JLabel photo = new JLabel(new ImageIcon(img));
        photo.setBounds(0, 35, 299, 140);
        card.add(photo);

        JTextArea text = new JTextArea("Click to view available donations for this drive.");
        text.setBounds(20, 210, 260, 50);
        text.setFont(new Font("Arial", Font.PLAIN, 12));
        text.setBackground(new Color(20, 35, 100));
        text.setForeground(Color.LIGHT_GRAY);
        text.setLineWrap(true);
        text.setEditable(false);
        card.add(text);

        JButton actionBtn = new JButton();
        actionBtn.setOpaque(false);
        actionBtn.setContentAreaFilled(false);
        actionBtn.setBorderPainted(false);
        actionBtn.setBounds(0, 0, 300, 303);
        card.add(actionBtn);

        cardsPanel.add(card);
        cardsPanel.revalidate();
        cardsPanel.repaint();

        return new DriveCardRefs(countLbl, actionBtn);
    }

    public static class TicketStub {
        public String ticketId;
        public String category;
        public String quantity;
        public String location;
        public String donationDrive;

        public TicketStub(String id, String c, String q, String l, String d) {
            this.ticketId = id;
            this.category = c;
            this.quantity = q;
            this.location = l;
            this.donationDrive = d;
        }
    }

    public static void main(String[] args) {
        new RiderDashboard();
    }
}