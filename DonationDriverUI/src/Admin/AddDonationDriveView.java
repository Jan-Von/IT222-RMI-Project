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
    public JLabel photoPreviewLabel;
    private File selectedPhotoFile;

    public AddDonationDriveView(JFrame parent) {
        frame = new JFrame("Create Donation Drive");
        frame.setSize(400, 550);
        frame.setLocationRelativeTo(parent);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon frameIcon = new ImageIcon("Resources/Images/logoicon.png");
        frame.setIconImage(frameIcon.getImage());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        JLabel titleLabel = createLabel("Drive Title");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        titleField = new JTextField();
        content.add(titleField);
        content.add(Box.createVerticalStrut(15));

        JLabel descLabel = createLabel("Description");
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(descLabel);
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        content.add(new JScrollPane(descriptionArea));
        content.add(Box.createVerticalStrut(15));

        JLabel amountLabel = createLabel("Target Amount (PHP)");
        amountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(amountLabel);
        targetAmountField = new JTextField();
        content.add(targetAmountField);
        content.add(Box.createVerticalStrut(15));

        JLabel coverPhotoLabel = createLabel("Cover Photo");
        coverPhotoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(coverPhotoLabel);
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        photoPanel.setBackground(Color.WHITE);
        photoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        uploadPhotoBtn = new JButton("Choose Photo");
        uploadPhotoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadPhotoBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        photoPanel.add(uploadPhotoBtn);
        
        photoPreviewLabel = new JLabel();
        photoPreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        photoPreviewLabel.setPreferredSize(new Dimension(100, 60));
        photoPreviewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        photoPreviewLabel.setVisible(false);
        content.add(photoPreviewLabel);
        content.add(Box.createVerticalStrut(10));

        uploadPhotoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("JPEG images", "jpg", "jpeg"));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                selectedPhotoFile = chooser.getSelectedFile();
                photoStatusLabel.setText("Photo: " + selectedPhotoFile.getName());
                
                // Update preview thumbnail
                try {
                    ImageIcon full = new ImageIcon(selectedPhotoFile.getAbsolutePath());
                    Image scaled = full.getImage().getScaledInstance(100, 60, Image.SCALE_SMOOTH);
                    photoPreviewLabel.setIcon(new ImageIcon(scaled));
                    photoPreviewLabel.setVisible(true);
                    frame.revalidate();
                    frame.repaint();
                } catch (Exception ex) {
                    photoPreviewLabel.setVisible(false);
                }
            }
        });
        
        content.add(photoPanel);
        content.add(Box.createVerticalStrut(25));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
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
        if (photoPreviewLabel != null) {
            photoPreviewLabel.setIcon(null);
            photoPreviewLabel.setVisible(false);
        }
    }
}
