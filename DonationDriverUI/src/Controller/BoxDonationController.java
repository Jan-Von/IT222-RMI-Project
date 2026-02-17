package Controller;

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
        this.view = view;

        view.homeBtn.addActionListener(e -> openDashBoard());
        view.donateNow.addActionListener(e -> handleDonateNow());
        view.notifBtn.addActionListener(e -> openNotification());
        view.donationBtn.addActionListener(e -> openDonations());
        view.DonateBtn.addActionListener(e -> openDonate());
        view.helpBtn.addActionListener(e -> openHelp());

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

        File photoFile = view.getSelectedPhotoFile();
        String photoBase64 = null;
        if (photoFile != null) {
            photoBase64 = PhotoUtil.jpgFileToBase64(photoFile);
            if (photoBase64 == null) {
                JOptionPane.showMessageDialog(view.frame,
                        "Could not read the selected photo. Please choose a valid JPG file.",
                        "Create Donation Ticket",
                        JOptionPane.WARNING_MESSAGE);
                view.clearSelectedPhoto();
                return;
            }
        }

        String notes = "Goods donation – " + (drive != null ? drive : "") + (destination != null && !destination.isEmpty() ? " | Deliver to: " + destination : "");
        try {
            Client client = Client.getDefault();

            String responseXml = client.createTicket(
                    userId,
                    goods,                  // itemCategory
                    quantity,               // quantity
                    "good condition",       // condition
                    "",                     // expirationDate
                    "",                     // pickupDateTime
                    location,               // pickupLocation
                    "",                     // photoPath
                    notes,
                    drive,                  // donationDrive – sent to server for tracking
                    destination,            // deliveryDestination – e.g. public school, barangay
                    photoBase64 != null ? photoBase64 : ""
            );

            Client.Response response = Client.parseResponse(responseXml);
            if (response != null && "NO_RIDERS".equals(response.status)) {
                JOptionPane.showMessageDialog(view.frame,
                        "No available riders at the moment. Please wait or try again later.",
                        "No Riders Available",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (response != null && response.isOk()) {
                // Log into Goods Donations.xml (including running total of boxes)
                logGoodsDonation(userId, goods, quantity, location, photoBase64);

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

    /**
     * Resolve the Goods Donations.xml file so that it points to the project's DonationDriverUI folder.
     */
    private static File getGoodsXmlFile() {
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File candidate = new File(dir, GOODS_XML_RELATIVE);
            if (candidate.exists()) return candidate;
            File donationDriverDir = new File(dir, "DonationDriverUI");
            if (donationDriverDir.isDirectory()) return new File(donationDriverDir, "Goods Donations.xml");
        }
        return new File(cwd, GOODS_XML_RELATIVE);
    }

    /**
     * Append a goods donation entry into Goods Donations.xml.
     * Logs goods, quantity, location, and running total boxes.
     * Optionally includes photoBase64 when a photo was attached.
     */
    private void logGoodsDonation(String userId, String goods, int quantity, String location, String photoBase64) {
        try {
            File file = getGoodsXmlFile();

            Document doc;
            if (file.exists()) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(file);
            } else {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.newDocument();
                doc.appendChild(doc.createElement("goodsDonations"));
            }

            Element root = doc.getDocumentElement();
            if (root == null || !"goodsDonations".equals(root.getTagName())) {
                root = doc.createElement("goodsDonations");
                doc.appendChild(root);
            }

            // Compute existing total quantity
            double existingTotal = 0.0;
            org.w3c.dom.NodeList donations = root.getElementsByTagName("donation");
            for (int i = 0; i < donations.getLength(); i++) {
                org.w3c.dom.Element d = (org.w3c.dom.Element) donations.item(i);
                org.w3c.dom.NodeList qtyNodes = d.getElementsByTagName("quantity");
                if (qtyNodes.getLength() > 0) {
                    String text = qtyNodes.item(0).getTextContent();
                    if (text != null && !text.trim().isEmpty()) {
                        try {
                            existingTotal += Double.parseDouble(text.trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            double newTotal = existingTotal + quantity;

            Element donationEl = doc.createElement("donation");
            appendChildText(doc, donationEl, "userEmail", userId);
            appendChildText(doc, donationEl, "goods", goods);
            appendChildText(doc, donationEl, "quantity", String.valueOf(quantity));
            appendChildText(doc, donationEl, "location", location);

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            appendChildText(doc, donationEl, "timestamp", timestamp);
            appendChildText(doc, donationEl, "runningTotalBoxes", String.valueOf(newTotal));
            if (photoBase64 != null && !photoBase64.isEmpty()) {
                appendChildText(doc, donationEl, "photoBase64", photoBase64);
            }

            root.appendChild(donationEl);
            writeDocument(doc, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
