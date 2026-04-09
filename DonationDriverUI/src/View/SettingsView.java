package View;

import javax.swing.*;
import java.awt.*;

public class SettingsView {
    public JFrame frame;
    public JButton switchRoleBtn;
    public JButton homeBtn;
    public JButton logoutBtn;

    public JLabel rmiHostLabel;
    public JTextField rmiHostField;
    public JLabel rmiPortLabel;
    public JTextField rmiPortField;
    public JButton rmiTestBtn;
    public JButton rmiSaveBtn;

    public SettingsView() {
        frame = new JFrame("DonationDriver - Settings");
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());
        frame.setSize(420, 480);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBounds(140, 20, 150, 30);
        frame.add(title);

        JLabel rmiSection = new JLabel("RMI server");
        rmiSection.setFont(new Font("Arial", Font.BOLD, 14));
        rmiSection.setForeground(new Color(20, 35, 100));
        rmiSection.setBounds(30, 58, 200, 22);
        frame.add(rmiSection);

        rmiHostLabel = new JLabel("Host:");
        rmiHostLabel.setBounds(30, 88, 100, 24);
        frame.add(rmiHostLabel);

        rmiHostField = new JTextField();
        rmiHostField.setBounds(130, 86, 240, 28);
        frame.add(rmiHostField);

        rmiPortLabel = new JLabel("Port:");
        rmiPortLabel.setBounds(30, 126, 100, 24);
        frame.add(rmiPortLabel);

        rmiPortField = new JTextField();
        rmiPortField.setBounds(130, 124, 100, 28);
        frame.add(rmiPortField);

        rmiTestBtn = new JButton("Test connection");
        rmiTestBtn.setBounds(70, 168, 130, 32);
        rmiTestBtn.setFocusPainted(false);
        frame.add(rmiTestBtn);

        rmiSaveBtn = new JButton("Save");
        rmiSaveBtn.setBounds(220, 168, 130, 32);
        rmiSaveBtn.setBackground(new Color(20, 35, 100));
        rmiSaveBtn.setForeground(Color.WHITE);
        rmiSaveBtn.setFocusPainted(false);
        frame.add(rmiSaveBtn);

        switchRoleBtn = new JButton("Switch to Rider Role"); // Redundant Button
        switchRoleBtn.setBounds(0, 0, 0, 0);
        switchRoleBtn.setBackground(new Color(20, 35, 100));
        switchRoleBtn.setForeground(Color.WHITE);
        switchRoleBtn.setFocusPainted(false);
        frame.add(switchRoleBtn);

        homeBtn = new JButton("Back to Dashboard");
        homeBtn.setBounds(100, 230, 220, 40);
        homeBtn.setBackground(Color.LIGHT_GRAY);
        homeBtn.setFocusPainted(false);
        frame.add(homeBtn);

        logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(100, 285, 220, 40);
        logoutBtn.setBackground(Color.RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        frame.add(logoutBtn);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new SettingsView();
    }
}
