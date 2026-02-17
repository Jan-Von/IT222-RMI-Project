package View;

import javax.swing.*;
import java.awt.*;

public class HelpView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton helpBtn;
    public JButton settingsBtn;


    public HelpView() {
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

        helpBtn = new JButton("Help");
        helpBtn.setBounds(65, 550, 80, 40);
        helpBtn.setBorderPainted(false);
        helpBtn.setFocusPainted(false);
        helpBtn.setBackground(Color.lightGray);
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


        JLabel newsLabel = new JLabel("How Donation Driver Works");
        newsLabel.setFont(new Font("Arial", Font.BOLD, 30));
        newsLabel.setForeground(Color.BLACK);
        newsLabel.setBounds(230, 100, 500, 30);
        frame.add(newsLabel);

        JLabel mainPanel = new JLabel();
        mainPanel.setForeground(new Color(20, 35, 100));
        mainPanel.setBounds(400, 200, 800, 400);
        mainPanel.setBackground(new Color(245, 245, 245));
        mainPanel.setOpaque(true);
        frame.add(mainPanel);

        JLabel makeDonations = new JLabel("Make donations");
        makeDonations.setFont(new Font("Arial", Font.BOLD, 20));
        makeDonations.setForeground(Color.BLACK);
        makeDonations.setBounds(40, 30, 150, 30);
        mainPanel.add(makeDonations);


        JLabel makeDonationsInstruction1 = new JLabel("Select your cause");
        makeDonationsInstruction1.setFont(new Font("Arial", Font.BOLD, 14));
        makeDonationsInstruction1.setForeground(Color.BLACK);
        makeDonationsInstruction1.setBounds(60, 60, 150, 30);
        mainPanel.add(makeDonationsInstruction1);

        JLabel text1 = new JLabel("<html> Click the Donate tab in the left sidebar or select an urgent <br> campaign directly from the News Flash on your home screen</html>");
        text1.setFont(new Font("Arial", Font.PLAIN, 11));
        text1.setForeground(Color.BLACK);
        text1.setBounds(60, 60, 450, 80);
        mainPanel.add(text1);

        ImageIcon text1icon = new ImageIcon("Resources/Images/number-one.png");
        scaledImg = text1icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel text1Icon = new JLabel(new ImageIcon(scaledImg));
        text1Icon.setBounds(20, 75, 30, 30);
        mainPanel.add(text1Icon);

        JLabel makeDonationsInstruction2 = new JLabel("Choose Donation Type");
        makeDonationsInstruction2.setFont(new Font("Arial", Font.BOLD, 14));
        makeDonationsInstruction2.setForeground(Color.BLACK);
        makeDonationsInstruction2.setBounds(60, 120, 250, 30);
        mainPanel.add(makeDonationsInstruction2);

        JLabel text2 = new JLabel("<html>Select whether you are giving Money,Physical Good <br> or Services");
        text2.setFont(new Font("Arial", Font.PLAIN, 11));
        text2.setForeground(Color.BLACK);
        text2.setBounds(60, 120, 450, 80);
        mainPanel.add(text2);

        ImageIcon text2icon = new ImageIcon("Resources/Images/number-two.png");
        scaledImg = text2icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel text2Icon = new JLabel(new ImageIcon(scaledImg));
        text2Icon.setBounds(20, 135, 30, 30);
        mainPanel.add(text2Icon);

        JLabel makeDonationsInstruction3 = new JLabel("Enter Details");
        makeDonationsInstruction3.setFont(new Font("Arial", Font.BOLD, 14));
        makeDonationsInstruction3.setForeground(Color.BLACK);
        makeDonationsInstruction3.setBounds(60, 180, 250, 30);
        mainPanel.add(makeDonationsInstruction3);

        JLabel text3 = new JLabel("<html>For Money: Enter the amount and method <br>" +
                "For Goods: Select the type and quantity</html>");
        text3.setFont(new Font("Arial", Font.PLAIN, 11));
        text3.setForeground(Color.BLACK);
        text3.setBounds(60, 180, 450, 80);
        mainPanel.add(text3);

        ImageIcon text3icon = new ImageIcon("Resources/Images/number-three.png");
        scaledImg = text3icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel text3Icon = new JLabel(new ImageIcon(scaledImg));
        text3Icon.setBounds(20, 195, 30, 30);
        mainPanel.add(text3Icon);

        JLabel makeDonationsInstruction4 = new JLabel("Confirm & Pay");
        makeDonationsInstruction4.setFont(new Font("Arial", Font.BOLD, 14));
        makeDonationsInstruction4.setForeground(Color.BLACK);
        makeDonationsInstruction4.setBounds(60, 240, 250, 30);
        mainPanel.add(makeDonationsInstruction4);

        JLabel text4 = new JLabel("<html>Review your contribution, choose to either donate <br> money or goods, and hit Confirm</html>");
        text4.setFont(new Font("Arial", Font.PLAIN, 11));
        text4.setForeground(Color.BLACK);
        text4.setBounds(60, 240, 450, 80);
        mainPanel.add(text4);

        ImageIcon text4icon = new ImageIcon("Resources/Images/number-four.png");
        scaledImg = text4icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel text4Icon = new JLabel(new ImageIcon(scaledImg));
        text4Icon.setBounds(20, 255, 30, 30);
        mainPanel.add(text4Icon);

        JLabel monitorDeliveriesAndTransactions = new JLabel("Monitor Deliveries & Transactions");
        monitorDeliveriesAndTransactions.setFont(new Font("Arial", Font.BOLD, 20));
        monitorDeliveriesAndTransactions.setForeground(Color.BLACK);
        monitorDeliveriesAndTransactions.setBounds(440, 30, 450, 30);
        mainPanel.add(monitorDeliveriesAndTransactions);

        JLabel monitorDeliveriesInstructions1 = new JLabel("Donations");
        monitorDeliveriesInstructions1.setFont(new Font("Arial", Font.BOLD, 14));
        monitorDeliveriesInstructions1.setForeground(Color.BLACK);
        monitorDeliveriesInstructions1.setBounds(460, 60, 250, 30);
        mainPanel.add(monitorDeliveriesInstructions1);

        JLabel monitortext1 = new JLabel("<html>Access Your Hub Click the Donations Icon (the package symbol) <br>" +
                "in the left sidebar to open your transaction manager</html>");
        monitortext1.setFont(new Font("Arial", Font.PLAIN, 11));
        monitortext1.setForeground(Color.BLACK);
        monitortext1.setBounds(460, 60, 450, 80);
        mainPanel.add(monitortext1);

        ImageIcon Text1icon = new ImageIcon("Resources/Images/number-one.png");
        scaledImg = Text1icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel Text1Icon = new JLabel(new ImageIcon(scaledImg));
        Text1Icon.setBounds(420, 75, 30, 30);
        mainPanel.add(Text1Icon);

        JLabel monitorDeliveriesInstructions2 = new JLabel("Pick A Category");
        monitorDeliveriesInstructions2.setFont(new Font("Arial", Font.BOLD, 14));
        monitorDeliveriesInstructions2.setForeground(Color.BLACK);
        monitorDeliveriesInstructions2.setBounds(460, 120, 250, 30);
        mainPanel.add(monitorDeliveriesInstructions2);

        JLabel monitortext2 = new JLabel("<html>Select a tab at the top: Active Delivery, Delivered, or Rejected <br>" +
                "To find the specific donation you want to track</html>");
        monitortext2.setFont(new Font("Arial", Font.PLAIN, 11));
        monitortext2.setForeground(Color.BLACK);
        monitortext2.setBounds(460, 120, 450, 80);
        mainPanel.add(monitortext2);

        ImageIcon Text2icon = new ImageIcon("Resources/Images/number-two.png");
        scaledImg = Text2icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel Text2Icon = new JLabel(new ImageIcon(scaledImg));
        Text2Icon.setBounds(420, 135, 30, 30);
        mainPanel.add(Text2Icon);

        JLabel monitorDeliveriesInstructions3 = new JLabel("Check Status");
        monitorDeliveriesInstructions3.setFont(new Font("Arial", Font.BOLD, 14));
        monitorDeliveriesInstructions3.setForeground(Color.BLACK);
        monitorDeliveriesInstructions3.setBounds(460, 180, 250, 30);
        mainPanel.add(monitorDeliveriesInstructions3);

        JLabel monitortext3 = new JLabel("<html>Check the sections for the high-visibility tag (like in Transit or <br>" +
                "Pending) and the latest logistic update such as Waiting for Pick up</html>");
        monitortext3.setFont(new Font("Arial", Font.PLAIN, 11));
        monitortext3.setForeground(Color.BLACK);
        monitortext3.setBounds(460, 180, 450, 80);
        mainPanel.add(monitortext3);

        ImageIcon Text3icon = new ImageIcon("Resources/Images/number-three.png");
        scaledImg = Text3icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel Text3Icon = new JLabel(new ImageIcon(scaledImg));
        Text3Icon.setBounds(420, 195, 30, 30);
        mainPanel.add(Text3Icon);

        JLabel monitorDeliveriesInstructions4 = new JLabel("Deep Dive");
        monitorDeliveriesInstructions4.setFont(new Font("Arial", Font.BOLD, 14));
        monitorDeliveriesInstructions4.setForeground(Color.BLACK);
        monitorDeliveriesInstructions4.setBounds(460, 240, 250, 30);
        mainPanel.add(monitorDeliveriesInstructions4);

        JLabel monitortext4 = new JLabel("<html>Click the Buttons on the Side bar to see information <br> about your Donations</html>");
        monitortext4.setFont(new Font("Arial", Font.PLAIN, 11));
        monitortext4.setForeground(Color.BLACK);
        monitortext4.setBounds(460, 240, 450, 80);
        mainPanel.add(monitortext4);

        ImageIcon Text4icon = new ImageIcon("Resources/Images/number-four.png");
        scaledImg = Text4icon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        JLabel Text4Icon = new JLabel(new ImageIcon(scaledImg));
        Text4Icon.setBounds(420, 255, 30, 30);
        mainPanel.add(Text4Icon);


        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new HelpView();
    }
}