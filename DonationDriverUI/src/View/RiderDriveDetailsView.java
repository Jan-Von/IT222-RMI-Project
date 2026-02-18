package View;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class RiderDriveDetailsView {

    public JFrame frame;
    public JButton backBtn;
    public JPanel cardsPanel;
    public JLabel titleLabel;

    public interface OnAcceptListener {
        void accept(String ticketId);
    }

    private OnAcceptListener onAcceptListener;

    public void setOnAcceptListener(OnAcceptListener listener) {
        this.onAcceptListener = listener;
    }

    public RiderDriveDetailsView(String driveName) {
        frame = new JFrame("DonationDriver - " + driveName);
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setLayout(null);
        header.setBackground(new Color(20, 35, 100)); // Dark Blue
        header.setBounds(0, 0, 1200, 80);
        frame.add(header);

        backBtn = new JButton("Back");
        backBtn.setBounds(20, 20, 80, 40);
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(20, 35, 100));
        backBtn.setFocusPainted(false);
        header.add(backBtn);

        titleLabel = new JLabel(driveName);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(120, 20, 800, 40);
        header.add(titleLabel);

        cardsPanel = new JPanel();
        cardsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
        cardsPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setBounds(0, 80, 1185, 680);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scrollPane);

        frame.setVisible(false);
    }

    public void updateCards(List<RiderDashboard.TicketStub> tickets) {
        cardsPanel.removeAll();

        if (tickets.isEmpty()) {
            JLabel noData = new JLabel("No pending donations for this drive.");
            noData.setFont(new Font("Arial", Font.PLAIN, 18));
            noData.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            cardsPanel.add(noData);
        } else {
            for (RiderDashboard.TicketStub t : tickets) {
                JPanel card = new JPanel();
                card.setLayout(null);
                card.setPreferredSize(new Dimension(300, 320));
                card.setBackground(new Color(245, 245, 245));
                card.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));

                JLabel title = new JLabel(t.category + ": " + t.quantity);
                title.setForeground(new Color(20, 35, 100));
                title.setFont(new Font("Arial", Font.BOLD, 16));
                title.setBounds(15, 10, 270, 25);
                card.add(title);

                String imgPath = "Resources/Images/image2.png"; // Default
                if (t.donationDrive != null) {
                    if (t.donationDrive.contains("Haiyan")) {
                        imgPath = "Resources/Images/image1.png";
                    } else if (t.donationDrive.contains("Cebu")) {
                        imgPath = "Resources/Images/image2.png";
                    } else if (t.donationDrive.contains("Fire")) {
                        imgPath = "Resources/Images/image3.png";
                    }
                }
                ImageIcon icon = new ImageIcon(imgPath);
                Image img = icon.getImage().getScaledInstance(298, 160, Image.SCALE_SMOOTH);
                JLabel photo = new JLabel(new ImageIcon(img));
                photo.setBounds(1, 40, 298, 160);
                card.add(photo);

                JTextArea locText = new JTextArea("Loc: " + t.location);
                locText.setBounds(15, 210, 270, 40);
                locText.setFont(new Font("Arial", Font.PLAIN, 14));
                locText.setBackground(new Color(245, 245, 245));
                locText.setForeground(Color.DARK_GRAY);
                locText.setLineWrap(true);
                locText.setWrapStyleWord(true);
                locText.setEditable(false);
                card.add(locText);

                JButton acceptBtn = new JButton("Accept");
                acceptBtn.setBounds(15, 260, 270, 40);
                acceptBtn.setBackground(new Color(0, 153, 51));
                acceptBtn.setForeground(Color.WHITE);
                acceptBtn.setFont(new Font("Arial", Font.BOLD, 16));
                acceptBtn.setFocusPainted(false);
                acceptBtn.addActionListener(e -> {
                    if (onAcceptListener != null) {
                        onAcceptListener.accept(t.ticketId);
                    }
                });
                card.add(acceptBtn);

                cardsPanel.add(card);
            }
        }

        cardsPanel.revalidate();
        cardsPanel.repaint();
    }

    public static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= (getHgap() + 1);
            return minimum;
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap();
                int vgap = getVgap();
                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int nmembers = target.getComponentCount();
                for (int i = 0; i < nmembers; i++) {
                    Component m = target.getComponent(i);
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            addRow(dim, rowWidth, rowHeight);
                            rowWidth = 0;
                            rowHeight = 0;
                        }
                        if (rowWidth != 0) {
                            rowWidth += hgap;
                        }
                        rowWidth += d.width;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                }
                addRow(dim, rowWidth, rowHeight);
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }
}
