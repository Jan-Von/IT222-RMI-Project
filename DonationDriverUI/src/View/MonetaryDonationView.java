package View;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class MonetaryDonationView {
    public JFrame frame;
    public JButton homeBtn;
    public JButton notifBtn;
    public JButton donationBtn;
    public JButton DonateBtn;
    public JButton helpBtn;
    public JButton settingsBtn;
    public JButton donateNow;
    public JButton uploadPhotoBtn;
    public JLabel photoStatusLabel;
    public JTextField amountField;
    public JTextField transactionIdField;
    public JComboBox<String> donationDriveDropdown;

    private File selectedPhotoFile;

    public MonetaryDonationView() {
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

        helpBtn = new JButton("Help");
        helpBtn.setBounds(45, 550, 120, 40);
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

        JLabel newsLabel = new JLabel("Monetary Donations");
        newsLabel.setFont(new Font("Arial", Font.BOLD, 30));
        newsLabel.setForeground(new Color(20, 35, 100));
        newsLabel.setBounds(230, 100, 300, 30);
        frame.add(newsLabel);

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

        String fullText = "Super Typhoon Haiyan (locally known as Yolanda) swept across the Philippines, " +
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
        frame.add(donationPanel);

        JPanel monetaryMainCard = new JPanel();
        monetaryMainCard.setLayout(null);
        monetaryMainCard.setBounds(400, 150, 600, 500);
        monetaryMainCard.setBackground(new Color(245, 245, 245));
        frame.add(monetaryMainCard);

        JLabel donationServices = new JLabel("Donation Services");
        donationServices.setFont(new Font("Arial", Font.BOLD, 14));
        donationServices.setForeground(Color.BLACK);
        donationServices.setBounds(10, 10, 300, 30);
        monetaryMainCard.add(donationServices);

        JLabel Amount = new JLabel("Amount");
        Amount.setFont(new Font("Arial", Font.BOLD, 14));
        Amount.setForeground(Color.BLACK);
        Amount.setBounds(150, 50, 300, 30);
        monetaryMainCard.add(Amount);

        amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        amountField.setForeground(Color.BLACK);
        amountField.setBounds(150, 80, 300, 30);
        monetaryMainCard.add(amountField);

        transactionIdField = new JTextField();

        JLabel paymentMethod = new JLabel("Payment Method");
        paymentMethod.setFont(new Font("Arial", Font.BOLD, 14));
        paymentMethod.setForeground(Color.BLACK);
        paymentMethod.setBounds(150, 110, 300, 30);
        monetaryMainCard.add(paymentMethod);

        String[] paymentOptions = { "Select", "BDO", "BPI", "Paymaya", "Gcash", "Gotyme" };
        JComboBox PaymentOptions = new JComboBox(paymentOptions);
        PaymentOptions.setFont(new Font("Arial", Font.BOLD, 14));
        PaymentOptions.setBackground(new Color(245, 245, 245));
        PaymentOptions.setBounds(150, 140, 300, 30);
        monetaryMainCard.add(PaymentOptions);

        JLabel donationDriveLabel = new JLabel("Donation Drive");
        donationDriveLabel.setFont(new Font("Arial", Font.BOLD, 14));
        donationDriveLabel.setForeground(Color.BLACK);
        donationDriveLabel.setBounds(150, 170, 300, 30);
        monetaryMainCard.add(donationDriveLabel);

        donationDriveDropdown = new JComboBox<>();
        donationDriveDropdown.addItem("Select");
        donationDriveDropdown.setFont(new Font("Arial", Font.BOLD, 14));
        donationDriveDropdown.setBackground(new Color(245, 245, 245));
        donationDriveDropdown.setBounds(150, 250, 300, 30);
        monetaryMainCard.add(donationDriveDropdown);

        uploadPhotoBtn = new JButton("Upload photo (JPG)");
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        uploadPhotoBtn.setBounds(150, 200, 180, 28);
        monetaryMainCard.add(uploadPhotoBtn);

        photoStatusLabel = new JLabel("No photo selected");
        photoStatusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        photoStatusLabel.setForeground(Color.GRAY);
        photoStatusLabel.setBounds(340, 200, 200, 22);
        monetaryMainCard.add(photoStatusLabel);

        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JPEG images", "jpg", "jpeg"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedPhotoFile = chooser.getSelectedFile();
                photoStatusLabel.setText("Photo selected: " + selectedPhotoFile.getName());
            }
        });

        donateNow = new JButton("Donate Now");
        donateNow.setFont(new Font("Arial", Font.BOLD, 14));
        donateNow.setForeground(Color.WHITE);
        donateNow.setBackground(new Color(20, 35, 100));
        donateNow.setBounds(150, 300, 300, 30);
        monetaryMainCard.add(donateNow);

        JLabel instructionText = new JLabel("<html>Note: Please send the monetary donation first and Enter" +
                "the reference number from the bank transfer</html> ");
        instructionText.setFont(new Font("Arial", Font.BOLD, 16));
        instructionText.setForeground(Color.BLACK);
        instructionText.setBounds(150, 310, 300, 150);
        monetaryMainCard.add(instructionText);

        frame.setVisible(true);
    }

    public File getSelectedPhotoFile() {
        return selectedPhotoFile;
    }

    public void clearSelectedPhoto() {
        selectedPhotoFile = null;
        if (photoStatusLabel != null) {
            photoStatusLabel.setText("No photo selected");
        }
    }

    public static void main(String[] args) {
        new MonetaryDonationView();
    }
}
