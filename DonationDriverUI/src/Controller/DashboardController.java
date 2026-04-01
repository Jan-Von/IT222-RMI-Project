package Controller;

import View.*;
import Network.Client;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class DashboardController {

    private DashboardView view;
    private String selectedDrive = "";
    private Timer refreshTimer;



    public DashboardController(DashboardView view) {
        this.view = view;
        view.monetaryBtn.addActionListener(e -> openMonetaryDonation());
        view.goodsBtn.addActionListener(e -> openBoxDonation());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.riderBtn.addActionListener(e -> openRiderMode());
        view.helpBtn.addActionListener(e -> openHelp());
        view.settingsBtn.addActionListener(e -> openSettings());

        if ("DONOR".equalsIgnoreCase(LoginController.currentUserRole)) {
            view.riderBtn.setVisible(false);
            if (view.riderIconLabel != null) {
                view.riderIconLabel.setVisible(false);
            }
        }

        loadDriveCards();

        refreshTimer = new Timer(5000, e -> loadDriveCards());
        refreshTimer.start();
    }

    private void stopTimer() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    private void loadDriveCards() {
        view.driveCardsPanel.removeAll();

        // Only server-side drives will be shown

        try {
            Client client = Client.getDefault();
            String responseXml = client.readDonationDrives();
            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
                String drivesXml = Client.unescapeXml(response.message);
                if (drivesXml != null) {
                    int idx = 0;
                    while (true) {
                        int start = drivesXml.indexOf("<drive>", idx);
                        if (start < 0)
                            break;
                        int end = drivesXml.indexOf("</drive>", start);
                        if (end < 0)
                            break;
                        String driveXml = drivesXml.substring(start, end + "</drive>".length());
                        String title = extractTagValue(driveXml, "title");
                        String description = extractTagValue(driveXml, "description");
                        String photoBase64 = extractTagValue(driveXml, "photoBase64");
                        if (title != null && !title.isEmpty()) {
                            view.driveCardsPanel.add(buildDriveCard(title,
                                    description != null ? description : "",
                                    photoBase64));
                        }
                        idx = end + "</drive>".length();
                    }
                }
            }
        } catch (IOException ex) {
            // Server not available
        }

        view.driveCardsPanel.revalidate();
        view.driveCardsPanel.repaint();
    }

    private JPanel buildDriveCard(String driveName, String description, String photoBase64) {
        Color bg = new Color(20, 35, 100);
        Color hoverBg = new Color(30, 50, 140);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(280, 300));
        card.setBackground(bg);
        card.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

        JLabel photoLabel = null;
        if (photoBase64 != null && !photoBase64.trim().isEmpty()) {
            try {
                byte[] imgBytes = Base64.getDecoder().decode(photoBase64);
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgBytes));
                if (img != null) {
                    Image scaled = img.getScaledInstance(280, 120, Image.SCALE_SMOOTH);
                    photoLabel = new JLabel(new ImageIcon(scaled));
                    photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    card.add(photoLabel);
                }
            } catch (Exception ignored) {
            }
        }

        JLabel titleLabel = new JLabel("<html><center>" + driveName + "</center></html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 13));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        card.add(titleLabel);

        JTextArea descArea = new JTextArea(description);
        descArea.setFont(new Font("Arial", Font.PLAIN, 11));
        descArea.setBackground(bg);
        descArea.setForeground(Color.WHITE);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(true);
        descArea.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        descArea.setMaximumSize(new Dimension(280, 80));
        card.add(descArea);

        card.add(Box.createVerticalGlue());

        JButton monBtn = new JButton("Monetary Donation");
        monBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        monBtn.setMaximumSize(new Dimension(200, 35));
        monBtn.setBackground(Color.WHITE);
        monBtn.setForeground(new Color(20, 35, 100));
        monBtn.setFocusPainted(false);
        monBtn.setBorderPainted(false);
        monBtn.setVisible(false);
        monBtn.addActionListener(e -> {
            selectedDrive = driveName;
            openMonetaryDonation();
        });

        JButton goodBtn = new JButton("Goods Donation");
        goodBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        goodBtn.setMaximumSize(new Dimension(200, 35));
        goodBtn.setBackground(Color.WHITE);
        goodBtn.setForeground(new Color(20, 35, 100));
        goodBtn.setFocusPainted(false);
        goodBtn.setBorderPainted(false);
        goodBtn.setVisible(false);
        goodBtn.addActionListener(e -> {
            selectedDrive = driveName;
            openBoxDonation();
        });

        card.add(monBtn);
        card.add(Box.createVerticalStrut(6));
        card.add(goodBtn);
        card.add(Box.createVerticalStrut(10));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(hoverBg);
                descArea.setBackground(hoverBg);
                monBtn.setVisible(true);
                goodBtn.setVisible(true);
                card.revalidate();
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = e.getPoint();
                if (p.x >= 0 && p.x < card.getWidth() && p.y >= 0 && p.y < card.getHeight()) {
                    return;
                }

                card.setBackground(bg);
                descArea.setBackground(bg);
                monBtn.setVisible(false);
                goodBtn.setVisible(false);
                card.revalidate();
                card.repaint();
            }
        });

        return card;
    }

    private String extractTagValue(String xml, String tag) {
        String open = "<" + tag + ">";
        String close = "</" + tag + ">";
        int s = xml.indexOf(open);
        if (s < 0)
            return null;
        int e = xml.indexOf(close, s);
        if (e < 0)
            return null;
        return xml.substring(s + open.length(), e).trim();
    }

    private void openMonetaryDonation() {
        stopTimer();
        MonetaryDonationView moneyView = new MonetaryDonationView();
        new MonetaryDonationController(moneyView, selectedDrive);
        moneyView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openBoxDonation() {
        stopTimer();
        BoxDonationView boxView = new BoxDonationView();
        new BoxDonationController(boxView, selectedDrive);
        boxView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openNotification() {
        stopTimer();
        NotificationView notifView = new NotificationView();
        new NotificationController(notifView);
        notifView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations() {
        stopTimer();
        DonationsActiveView donationsView = new DonationsActiveView();
        new DonationsActiveController(donationsView);
        donationsView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openRiderMode() {
        String userId = LoginController.currentUserEmail;
        if (userId == null || userId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please log in first to use Rider mode.",
                    "Login Required",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        stopTimer();
        RiderDashboard riderView = new RiderDashboard();
        new RiderController(riderView);
        riderView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        stopTimer();
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openHelp() {
        stopTimer();
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSettings() {
        stopTimer();
        SettingsView settingsView = new SettingsView();
        new SettingsController(settingsView);
        settingsView.frame.setVisible(true);
        view.frame.dispose();
    }
}
