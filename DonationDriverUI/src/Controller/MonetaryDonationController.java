package Controller;

import Network.DonationDriverService;
import View.*;
import Network.Client;
import Util.PhotoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

public class MonetaryDonationController {

    private static final String MONETARY_XML_RELATIVE = "DonationDriverUI/Monetary Donations.xml";

    private MonetaryDonationView view;

    public MonetaryDonationController(MonetaryDonationView view) {
        this(view, null);
    }

    public MonetaryDonationController(MonetaryDonationView view, String driveName) {
        this.view = view;

        populateDriveCombo();

        if (driveName != null && !driveName.isEmpty()) {
            view.donationDriveDropdown.setSelectedItem(driveName);
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
                            view.donationDriveDropdown.addItem(title);
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

    /**
     * Handle monetary donation: validate amount, create a ticket via the server,
     * then log to Monetary Donations.xml (including total donated) on success.
     */
    private void handleDonateNow() {
        // Validate amount
        String amountText = view.amountField.getText().trim();
        if (amountText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Please enter a donation amount.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Please enter a valid positive number for the amount.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate donation drive selection
        String selectedDrive = (String) view.donationDriveDropdown.getSelectedItem();
        if (selectedDrive == null || selectedDrive.equals("Select")) {
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Please select a donation drive.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate a simple numeric transaction ID (random 9‑digit number)
        String transactionId = String.format("%09d",
                (int) (Math.random() * 1_000_000_000));
        view.transactionIdField.setText(transactionId);

        String userId = (LoginController.currentUserEmail != null && !LoginController.currentUserEmail.isEmpty())
                ? LoginController.currentUserEmail
                : "guest@donationdriver";

        // Build notes for the ticket
        String notes = "Monetary donation to Super Typhoon Haiyan; "
                + "Amount=" + amount + "; "
                + "TransactionId=" + transactionId;

        // Require photo upload
        File photoFile = view.getSelectedPhotoFile();
        if (photoFile == null) {
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Please upload a photo before submitting your donation.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
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
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Could not read the selected photo. Please choose a valid JPG file.\nError: " + ex.getMessage(),
                    "Monetary Donation",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            view.clearSelectedPhoto();
            return;
        }

        try {
            Client client = Client.getDefault();

            // Use the extended CREATE_TICKET for consistency with goods donations
            String responseXml = client.createTicket(
                    userId,
                    "Monetary donation", // itemCategory
                    1, // quantity (conceptual)
                    "N/A", // condition
                    "", // expirationDate
                    "", // pickupDateTime
                    "", // pickupLocation
                    "", // photoPath
                    notes, // details / notes
                    selectedDrive, // donationDrive
                    "", // deliveryDestination
                    photoBase64 != null ? photoBase64 : "" // photoBase64
            );

            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
                try {
                    client.updateDriveAmount(selectedDrive, amount);
                } catch (IOException ignored) {
                    /* non-critical */ }

                // logMonetaryDonation(selectedDrive, amount, transactionId, photoBase64);

                javax.swing.JOptionPane.showMessageDialog(
                        view.frame,
                        "Monetary donation ticket created!\n" + response.message,
                        "Monetary Donation",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE);
                openSuccessDonation();
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Failed to create monetary donation ticket.";
                javax.swing.JOptionPane.showMessageDialog(
                        view.frame,
                        msg,
                        "Monetary Donation",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Unable to contact server. Please try again.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashBoard() {
        DashboardView dashboardView = new DashboardView();
        new DashboardController(dashboardView);
        dashboardView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openBoxDonation() {
        BoxDonationView boxDonationView = new BoxDonationView();
        new BoxDonationController(boxDonationView);
        boxDonationView.frame.setVisible(true);
        view.frame.dispose();
    }

    private void openSuccessDonation() {
        SuccessDonationView successDonationView = new SuccessDonationView();
        new SuccessDonationController(successDonationView);
        successDonationView.frame.setVisible(true);
        view.frame.dispose();
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


    // removed GetMonetaryXMLFIle
    /**
     * Append a monetary donation entry into Monetary Donations.xml.
     * Logs drive, amount, unique transaction id, and running total donated.
     * Optionally includes photoBase64 when a photo was attached.
     */

    // removed logMonetaryDonation Class

    private static Document loadDocument(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(file);
    }


    // removed writeDocument Class

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
