package Controller;

import View.*;
import Network.Client;
import Util.PhotoUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
    private BoxDonationView viewBox;

    public MonetaryDonationController(MonetaryDonationView view) {
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.donateNow.addActionListener(e -> handleDonateNow());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());

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
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
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
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validate donation drive selection
        String selectedDrive = (String) view.donationDriveDropdown.getSelectedItem();
        if (selectedDrive == null || selectedDrive.equals("Select")) {
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Please select a donation drive.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
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

        File photoFile = view.getSelectedPhotoFile();
        String photoBase64 = null;
        if (photoFile != null) {
            photoBase64 = PhotoUtil.jpgFileToBase64(photoFile);
            if (photoBase64 == null) {
                javax.swing.JOptionPane.showMessageDialog(
                        view.frame,
                        "Could not read the selected photo. Please choose a valid JPG file.",
                        "Monetary Donation",
                        javax.swing.JOptionPane.WARNING_MESSAGE
                );
                view.clearSelectedPhoto();
                return;
            }
        }

        try {
            Client client = Client.getDefault();

            // Use the extended CREATE_TICKET for consistency with goods donations
            String responseXml = client.createTicket(
                    userId,
                    "Monetary donation", // itemCategory
                    1,                   // quantity (conceptual)
                    "N/A",               // condition
                    "",                  // expirationDate
                    "",                  // pickupDateTime
                    "",                  // pickupLocation
                    "",                  // photoPath
                    notes,               // details / notes
                    photoBase64 != null ? photoBase64 : "",
                    selectedDrive,       // donationDrive
                    ""                   // deliveryDestination (not applicable for monetary)
            );

            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && response.isOk()) {
        // Log locally to Monetary Donations.xml
        logMonetaryDonation(selectedDrive, amount, transactionId, photoBase64);

                javax.swing.JOptionPane.showMessageDialog(
                        view.frame,
                        "Monetary donation ticket created!\n" + response.message,
                        "Monetary Donation",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                );
                openSuccessDonation();
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Failed to create monetary donation ticket.";
                javax.swing.JOptionPane.showMessageDialog(
                        view.frame,
                        msg,
                        "Monetary Donation",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(
                    view.frame,
                    "Unable to contact server. Please try again.",
                    "Monetary Donation",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
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

    /**
     * Resolve the Monetary Donations.xml file so that it points to the project's DonationDriverUI folder.
     */
    private static File getMonetaryXmlFile() {
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File candidate = new File(dir, MONETARY_XML_RELATIVE);
            if (candidate.exists()) return candidate;
            File donationDriverDir = new File(dir, "DonationDriverUI");
            if (donationDriverDir.isDirectory()) return new File(donationDriverDir, "Monetary Donations.xml");
        }
        return new File(cwd, MONETARY_XML_RELATIVE);
    }

    /**
     * Append a monetary donation entry into Monetary Donations.xml.
     * Logs drive, amount, unique transaction id, and running total donated.
     * Optionally includes photoBase64 when a photo was attached.
     */
    private void logMonetaryDonation(String driveName, double amount, String transactionId, String photoBase64) {
        try {
            File file = getMonetaryXmlFile();

            Document doc;
            if (file.exists()) {
                doc = loadDocument(file);
            } else {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.newDocument();
                doc.appendChild(doc.createElement("monetaryDonations"));
            }

            Element root = doc.getDocumentElement();
            if (root == null || !"monetaryDonations".equals(root.getTagName())) {
                root = doc.createElement("monetaryDonations");
                doc.appendChild(root);
            }

            // Compute existing total amount
            double existingTotal = 0.0;
            org.w3c.dom.NodeList donations = root.getElementsByTagName("donation");
            for (int i = 0; i < donations.getLength(); i++) {
                org.w3c.dom.Element d = (org.w3c.dom.Element) donations.item(i);
                org.w3c.dom.NodeList amountNodes = d.getElementsByTagName("amount");
                if (amountNodes.getLength() > 0) {
                    String text = amountNodes.item(0).getTextContent();
                    if (text != null && !text.trim().isEmpty()) {
                        try {
                            existingTotal += Double.parseDouble(text.trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            double newTotal = existingTotal + amount;

            Element donationEl = doc.createElement("donation");

            // Optional: who donated (if logged in)
            String userEmail = LoginController.currentUserEmail != null
                    ? LoginController.currentUserEmail
                    : "guest@donationdriver";

            appendChildText(doc, donationEl, "userEmail", userEmail);
            appendChildText(doc, donationEl, "drive", driveName);
            appendChildText(doc, donationEl, "transactionId", transactionId);
            appendChildText(doc, donationEl, "amount", String.valueOf(amount));

            // Optional: simple timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            appendChildText(doc, donationEl, "timestamp", timestamp);
            appendChildText(doc, donationEl, "runningTotal", String.valueOf(newTotal));
            if (photoBase64 != null && !photoBase64.isEmpty()) {
                appendChildText(doc, donationEl, "photoBase64", photoBase64);
            }

            root.appendChild(donationEl);
            writeDocument(doc, file);
        } catch (Exception e) {
            // For now, just print the stack trace; donation flow should still continue.
            e.printStackTrace();
        }
    }

    private static Document loadDocument(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(file);
    }

    private static void writeDocument(Document doc, File file) throws Exception {
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        try (FileOutputStream out = new FileOutputStream(file)) {
            t.transform(new DOMSource(doc), new StreamResult(out));
        }
    }

    private static void appendChildText(Document doc, Element parent, String tagName, String value) {
        Element el = doc.createElement(tagName);
        el.setTextContent(value != null ? value : "");
        parent.appendChild(el);
    }

    private void openHelp(){
        HelpView helpView = new HelpView();
        new HelpController(helpView);
        helpView.frame.setVisible(true);
        view.frame.dispose();
    }
}
