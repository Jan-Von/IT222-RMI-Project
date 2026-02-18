package View;

import javax.swing.*;
import java.awt.*;

public class SettingsView {
    public JFrame frame;
    public JButton switchRoleBtn;
    public JButton homeBtn;
    public JButton logoutBtn;

    public SettingsView() {
        frame = new JFrame("DonationDriver - Settings");
        frame.setSize(400, 350);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBounds(140, 20, 150, 30);
        frame.add(title);

        switchRoleBtn = new JButton("Switch to Rider Role");
        switchRoleBtn.setBounds(100, 100, 200, 40);
        switchRoleBtn.setBackground(new Color(20, 35, 100));
        switchRoleBtn.setForeground(Color.WHITE);
        switchRoleBtn.setFocusPainted(false);
        frame.add(switchRoleBtn);

        homeBtn = new JButton("Back to Dashboard");
        homeBtn.setBounds(100, 160, 200, 40);
        homeBtn.setBackground(Color.LIGHT_GRAY);
        homeBtn.setFocusPainted(false);
        frame.add(homeBtn);

        logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(100, 220, 200, 40);
        logoutBtn.setBackground(Color.RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        frame.add(logoutBtn);
    }
}
