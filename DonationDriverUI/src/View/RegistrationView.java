package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class RegistrationView {

    public JFrame frame;
    public JTextField emailField;
    public JTextField passwordField;
    public JButton finishButton;
    public JButton backButton;
    public JTextField firstNameField;
    public JTextField lastNameField;
    public JTextField middleNameField;
    public JTextField dateOfBirthField;
    public JTextField addressField;
    public JTextField phoneNumberField;
    public JTextField confirmPasswordField;
    public JCheckBox termsCheck;

    public RegistrationView() {
        frame = new JFrame("DonationDriver - Registration");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        // Header
        JPanel Header = new JPanel();
        Header.setLayout(null);
        Header.setBackground(Color.WHITE);
        Header.setBounds(0, 0, 1350, 120); // x, y, width, height

        ImageIcon headerLogo = new ImageIcon("Resources/Images/logoicon.png");
        Image scaledImg = headerLogo.getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH);
        JLabel HeaderLogo = new JLabel(new ImageIcon(scaledImg));
        HeaderLogo.setBounds(480, 20, 150, 80);
        Header.add(HeaderLogo);

        JLabel donationField = new JLabel("DonationDriver");
        donationField.setFont(new Font("Arial", Font.BOLD, 18));
        donationField.setBounds(650, 40, 300, 30); // x, y, width, height
        Header.add(donationField);

        JLabel acceleratedGiving = new JLabel("Accelerated Giving");
        acceleratedGiving.setFont(new Font("Arial", Font.BOLD, 18));
        acceleratedGiving.setForeground(new Color(20, 35, 100));
        acceleratedGiving.setBounds(633, 60, 300, 30);
        Header.add(acceleratedGiving);

        JLabel personalLabel = new JLabel("Personal Information");
        personalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        personalLabel.setBounds(150, 160, 250, 30);
        frame.add(personalLabel);

        JLabel firstNameLabel = new JLabel("First Name");
        firstNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        firstNameLabel.setBounds(180, 180, 380, 45);
        frame.add(firstNameLabel);

        firstNameField = new JTextField();
        firstNameField.setBounds(200, 220, 380, 30);
        firstNameField.setForeground(Color.GRAY);
        firstNameField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(firstNameField);

        JLabel lastNameLabel = new JLabel("Last Name");
        lastNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lastNameLabel.setBounds(180, 240, 380, 45);
        frame.add(lastNameLabel);

        lastNameField = new JTextField();
        lastNameField.setBounds(200, 280, 380, 30);
        lastNameField.setForeground(Color.GRAY);
        lastNameField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(lastNameField);

        JLabel middleNameLabel = new JLabel("Middle Name");
        middleNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        middleNameLabel.setBounds(180, 300, 380, 45);
        frame.add(middleNameLabel);

        middleNameField = new JTextField();
        middleNameField.setBounds(200, 340, 380, 30);
        middleNameField.setForeground(Color.GRAY);
        middleNameField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(middleNameField);

        JLabel dateOfBirthLabel = new JLabel("Date of Birth");
        dateOfBirthLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dateOfBirthLabel.setBounds(180, 360, 380, 45);
        frame.add(dateOfBirthLabel);

        dateOfBirthField = new JTextField();
        dateOfBirthField.setBounds(200, 400, 380, 30);
        dateOfBirthField.setForeground(Color.GRAY);
        dateOfBirthField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(dateOfBirthField);

        JLabel addressLabel = new JLabel("Address");
        addressLabel.setFont(new Font("Arial", Font.BOLD, 14));
        addressLabel.setBounds(180, 420, 380, 45);
        frame.add(addressLabel);

        addressField = new JTextField();
        addressField.setBounds(200, 460, 380, 30);
        addressField.setForeground(Color.GRAY);
        addressField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(addressField);

        JLabel contactLabel = new JLabel("Contact Information");
        contactLabel.setFont(new Font("Arial", Font.BOLD, 20));
        contactLabel.setBounds(750, 160, 250, 30);
        frame.add(contactLabel);

        JLabel phoneNumberLabel = new JLabel("Phone Number");
        phoneNumberLabel.setFont(new Font("Arial", Font.BOLD, 14));
        phoneNumberLabel.setBounds(780, 180, 380, 45);
        frame.add(phoneNumberLabel);

        phoneNumberField = new JTextField();
        phoneNumberField.setBounds(780, 220, 380, 30);
        phoneNumberField.setForeground(Color.GRAY);
        phoneNumberField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(phoneNumberField);

        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 14));
        emailLabel.setBounds(780, 240, 380, 45);
        frame.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(780, 280, 380, 30);
        emailField.setForeground(Color.GRAY);
        emailField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(emailField);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setBounds(780, 300, 380, 45);
        frame.add(passwordLabel);

        passwordField = new JTextField();
        passwordField.setBounds(780, 340, 380, 30);
        passwordField.setForeground(Color.GRAY);
        passwordField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(passwordField);

        JLabel confirmPasswordlabel = new JLabel("Confirm Password");
        confirmPasswordlabel.setFont(new Font("Arial", Font.BOLD, 14));
        confirmPasswordlabel.setBounds(780, 360, 380, 45);
        frame.add(confirmPasswordlabel);

        confirmPasswordField = new JTextField();
        confirmPasswordField.setBounds(780, 400, 380, 30);
        confirmPasswordField.setForeground(Color.GRAY);
        confirmPasswordField.setBorder(new LineBorder(new Color(230, 230, 230), 1));
        frame.add(confirmPasswordField);

        termsCheck = new JCheckBox("I Agree to Terms and Agreements");
        termsCheck.setFont(new Font("Arial", Font.BOLD, 12));
        termsCheck.setBounds(780, 500, 250, 45);
        termsCheck.setBorderPainted(false);
        termsCheck.setContentAreaFilled(false);
        frame.add(termsCheck);

        finishButton = new JButton("Finish");
        finishButton.setFont(new Font("Arial", Font.BOLD, 12));
        finishButton.setBounds(1060, 560, 100, 45);
        finishButton.setForeground(Color.WHITE);
        finishButton.setBackground(new Color(20, 35, 100));
        frame.add(finishButton);

        backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 12));
        backButton.setBounds(940, 560, 100, 45);
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(Color.GRAY);
        frame.add(backButton);

        JLabel roleLabel = new JLabel("I am a:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        roleLabel.setBounds(780, 450, 100, 30);
        frame.add(roleLabel);

        String[] roles = { "Donor", "Rider" };
        roleBox = new JComboBox<>(roles);
        roleBox.setBounds(840, 450, 150, 30);
        frame.add(roleBox);

        frame.add(Header);
        frame.setVisible(true);
    }

    public JComboBox<String> roleBox;
}