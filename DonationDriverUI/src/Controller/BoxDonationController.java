package Controller;

import Network.DonationDriverService;
import View.*;
import Network.Client;
import Util.PhotoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoxDonationController {

    private static final String GOODS_XML_RELATIVE = "DonationDriverUI/Goods Donations.xml";

    private BoxDonationView view;

    public BoxDonationController(BoxDonationView view) {
        this(view, null);
    }

    public BoxDonationController(BoxDonationView view, String driveName) {
        this.view = view;

        populateDriveCombo();

        if (driveName != null && !driveName.isEmpty()) {
            view.donationDriveCombo.setSelectedItem(driveName);
        }

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.donateNow.addActionListener(e -> handleDonateNow());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());

    }

    private void populateDriveCombo() {
        try {
            DonationDriverService svc = Client.getInstance().getService();
            String responseXml = svc.readDonationDrives();
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
                        if (title != null && !title.isEmpty()) {
                            view.donationDriveCombo.addItem(title);
                        }
                        idx = end + "</drive>".length();
                    }
                }
            }
        } catch (IOException ignored) {
            /* server not available */
        }
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

    private void openDashBoard() {
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openMonetaryDonation() {
        MonetaryDonationView monetaryDonationView = new MonetaryDonationView();
        new MonetaryDonationController(monetaryDonationView);
        monetaryDonationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSuccessDonation() {
        SuccessDonationView successDonationView = new SuccessDonationView();
        new SuccessDonationController(successDonationView);
        successDonationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void handleDonateNow() {

        String goods = view.typeOfGoodsField.getText().trim();
        String boxesText = view.numberOfBoxesField.getText().trim();
        String location = view.locationField.getText().trim();

        if (goods.isEmpty() || boxesText.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please fill in Type of Goods, Number of Boxes, and Your Location.",
                    "Create Donation Ticket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String drive = (String) view.donationDriveCombo.getSelectedItem();
        if (drive == null || drive.isEmpty() || "Select drive".equals(drive)) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please select a donation drive.",
                    "Create Donation Ticket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String destination = view.deliveryDestinationField.getText().trim();
        if (destination.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please enter where to deliver (e.g. public school, barangay).",
                    "Create Donation Ticket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(boxesText);
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view.frame,
                    "Number of Boxes must be a positive whole number.",
                    "Create Donation Ticket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String userId = (LoginController.currentUserEmail != null && !LoginController.currentUserEmail.isEmpty())
                ? LoginController.currentUserEmail
                : "guest@donationdriver";

        // Require photo upload
        File photoFile = view.getSelectedPhotoFile();
        if (photoFile == null) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please upload a photo before submitting your donation.",
                    "Create Donation Ticket",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert photo to base64 with exception handling
        String photoBase64 = null;
        try {
            photoBase64 = PhotoUtil.jpgFileToBase64(photoFile);
            if (photoBase64 == null) {
                throw new IOException("Failed to read photo file");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.frame,
                    "Could not read the selected photo. Please choose a valid JPG file.\nError: " + ex.getMessage(),
                    "Create Donation Ticket",
                    JOptionPane.ERROR_MESSAGE);
            view.clearSelectedPhoto();
            return;
        }

        String notes = "Goods donation – " + (drive != null ? drive : "")
                + (destination != null && !destination.isEmpty() ? " | Deliver to: " + destination : "");
        try {
            Client client = Client.getDefault();

            String responseXml = client.createTicket(
                    userId,
                    goods, // itemCategory
                    quantity, // quantity
                    "good condition", // condition
                    "", // expirationDate
                    "", // pickupDateTime
                    location, // pickupLocation
                    "", // photoPath
                    notes,
                    drive, // donationDrive – sent to server for tracking
                    destination, // deliveryDestination – e.g. public school, barangay
                    photoBase64 != null ? photoBase64 : "");

            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
                // logGoodsDonation(userId, goods, quantity, location, photoBase64);


                JOptionPane.showMessageDialog(view.frame,
                        "Donation ticket created!\n" + response.message,
                        "Create Donation Ticket",
                        JOptionPane.INFORMATION_MESSAGE);
                openSuccessDonation();
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Failed to create donation ticket.";
                JOptionPane.showMessageDialog(view.frame,
                        msg,
                        "Create Donation Ticket",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Unable to contact server. Please try again.",
                    "Create Donation Ticket",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openNotification() {
        NotificationView notificationView = new NotificationView();
        new NotificationController(notificationView);
        notificationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonations() {
        DonationsActiveView donationsView = new DonationsActiveView();
        new DonationsActiveController(donationsView);
        donationsView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openDonate() {
        DonateView donateView = new DonateView();
        new DonateController(donateView);
        donateView.frame.setVisible(true);
        view.frame.dispose();
    }

    // removed getGoodsXMLFile
    /**
     * Append a goods donation entry into Goods Donations.xml.
     * Logs goods, quantity, location, and running total boxes.
     * Optionally includes photoBase64 when a photo was attached.
     */

    // removed logGoodsDonation

    // removed writedocument class

    private static void appendChildText(Document doc, Element parent, String tagName, String value) {
        Element el = doc.createElement(tagName);
        el.setTextContent(value != null ? value : "");
        parent.appendChild(el);
    }

    private void openHelp() {
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }
}
