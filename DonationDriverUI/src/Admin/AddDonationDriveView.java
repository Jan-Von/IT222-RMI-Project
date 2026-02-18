package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AddDonationDriveView {

    public JFrame frame;
    public JTextField titleField;
    public JTextArea descriptionArea;
    public JTextField targetAmountField;
    public JButton createBtn;
    public JButton cancelBtn;

    public AddDonationDriveView(JFrame parent) {
        frame = new JFrame("Create Donation Drive");
        frame.setSize(400, 450);
        frame.setLocationRelativeTo(parent);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        content.add(createLabel("Drive Title"));
        titleField = new JTextField();
        content.add(titleField);
        content.add(Box.createVerticalStrut(15));

        content.add(createLabel("Description"));
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        content.add(new JScrollPane(descriptionArea));
        content.add(Box.createVerticalStrut(15));

        content.add(createLabel("Target Amount (PHP)"));
        targetAmountField = new JTextField();
        content.add(targetAmountField);
        content.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        cancelBtn = new JButton("Cancel");
        createBtn = new JButton("Create Drive");
        createBtn.setBackground(new Color(20, 35, 100));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);

        buttonPanel.add(cancelBtn);
        buttonPanel.add(createBtn);

        content.add(buttonPanel);

        frame.add(content, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(new Color(20, 35, 100));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
