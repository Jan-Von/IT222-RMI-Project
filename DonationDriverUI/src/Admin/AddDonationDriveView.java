package Admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class AddDonationDriveView {

    public JFrame frame;
    public JTextField titleField;
    public JTextArea descriptionArea;
    public JTextField targetAmountField;
    public JButton createBtn;
    public JButton cancelBtn;
    public JButton uploadPhotoBtn;
    public JLabel photoStatusLabel;
    private File selectedPhotoFile;

    public AddDonationDriveView(JFrame parent) {
        frame = new JFrame("Create Donation Drive");
        frame.setSize(400, 550);
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
        content.add(Box.createVerticalStrut(15));

        content.add(createLabel("Cover Photo (JPG)"));
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        uploadPhotoBtn = new JButton("Choose Photo");
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        photoPanel.add(uploadPhotoBtn);
        
        photoStatusLabel = new JLabel("No photo selected");
        photoStatusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        photoStatusLabel.setForeground(Color.GRAY);
        photoStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        photoPanel.add(photoStatusLabel);
        
        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JPEG images", "jpg", "jpeg"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedPhotoFile = chooser.getSelectedFile();
                photoStatusLabel.setText("Photo: " + selectedPhotoFile.getName());
            }
        });
        
        content.add(photoPanel);
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

    public File getSelectedPhotoFile() {
        return selectedPhotoFile;
    }

    public void clearSelectedPhoto() {
        selectedPhotoFile = null;
        if (photoStatusLabel != null) {
            photoStatusLabel.setText("No photo selected");
        }
    }
}
