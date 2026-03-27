package Admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AdminLoginView {

    public JFrame frame;
    public JTextField emailField;
    public JPasswordField passField;
    public JButton loginBtn;
    public JButton cancelBtn;

    private static final String EMAIL_PLACEHOLDER = "Admin Email";
    private static final String PASSWORD_PLACEHOLDER = "Password";

    public AdminLoginView() {
        frame = new JFrame("DonationDriver - Admin Login");
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        ImageIcon originalIcon = new ImageIcon("Resources/Images/logo.png");
        Image scaledImg = originalIcon.getImage().getScaledInstance(220, 165, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(scaledImg));

        gbc.gridy = 0;
        mainPanel.add(logoLabel, gbc);

        emailField = new JTextField(EMAIL_PLACEHOLDER);
        emailField.setPreferredSize(new Dimension(280, 30));
        emailField.setForeground(Color.GRAY);
        emailField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (emailField.getText().equals(EMAIL_PLACEHOLDER)) {
                    emailField.setText("");
                    emailField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (emailField.getText().trim().isEmpty()) {
                    emailField.setText(EMAIL_PLACEHOLDER);
                    emailField.setForeground(Color.GRAY);
                }
            }
        });
        gbc.gridy = 1;
        mainPanel.add(emailField, gbc);

        passField = new JPasswordField(PASSWORD_PLACEHOLDER);
        passField.setPreferredSize(new Dimension(280, 30));
        passField.setForeground(Color.GRAY);
        passField.setEchoChar((char) 0);
        passField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(passField.getPassword()).equals(PASSWORD_PLACEHOLDER)) {
                    passField.setText("");
                    passField.setForeground(Color.BLACK);
                    passField.setEchoChar('*');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (passField.getPassword().length == 0) {
                    passField.setText(PASSWORD_PLACEHOLDER);
                    passField.setForeground(Color.GRAY);
                    passField.setEchoChar((char) 0);
                }
            }
        });
        gbc.gridy = 2;
        mainPanel.add(passField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);

        loginBtn = new JButton("Login");
        loginBtn.setPreferredSize(new Dimension(120, 40));
        loginBtn.setBackground(new Color(20, 35, 100));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        buttonPanel.add(loginBtn);

        cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(120, 40));
        cancelBtn.setFocusPainted(false);
        buttonPanel.add(cancelBtn);

        gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
