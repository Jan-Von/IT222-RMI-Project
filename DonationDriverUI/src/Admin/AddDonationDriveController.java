package Admin;

import Controller.LoginController;
import Network.Client;
import javax.swing.*;
import java.io.IOException;

public class AddDonationDriveController {

    private AddDonationDriveView view;
    private Runnable onSuccess;

    public AddDonationDriveController(AddDonationDriveView view, Runnable onSuccess) {
        this.view = view;
        this.onSuccess = onSuccess;

        view.createBtn.addActionListener(e -> createDrive());
        view.cancelBtn.addActionListener(e -> view.frame.dispose());
    }

    private void createDrive() {
        String title = view.titleField.getText().trim();
        String description = view.descriptionArea.getText().trim();
        String targetStr = view.targetAmountField.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Title is required.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (targetStr.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame, "Target amount is required.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Double.parseDouble(targetStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view.frame, "Target amount must be a valid number.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Client client = Client.getDefault();
            String userId = LoginController.currentUserEmail;
            if (userId == null)
                userId = "admin";

            String responseXml = client.createDonationDrive(userId, title, description, targetStr);
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Donation Drive Created Successfully!");
                view.frame.dispose();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                String msg = (response != null && response.message != null) ? response.message
                        : "Failed to create drive.";
                JOptionPane.showMessageDialog(view.frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame, "Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
