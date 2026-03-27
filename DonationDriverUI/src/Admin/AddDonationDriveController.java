package Admin;

import Controller.LoginController;
import Network.Client;
import Util.PhotoUtil;
import javax.swing.*;
import java.io.File;

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
        try {
            doCreateDrive();
        } catch (Exception ex) {
            showError("An unexpected error occurred while creating the donation drive.", ex);
        }
    }

    private void doCreateDrive() {
        if (view == null || view.titleField == null || view.targetAmountField == null) {
            JOptionPane.showMessageDialog(view != null ? view.frame : null,
                    "Form is not fully loaded. Please close and try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String title = view.titleField.getText().trim();
        String description = view.descriptionArea != null ? view.descriptionArea.getText().trim() : "";
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

        double targetAmount;
        try {
            targetAmount = Double.parseDouble(targetStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view.frame, "Target amount must be a valid number.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (targetAmount < 0) {
            JOptionPane.showMessageDialog(view.frame, "Target amount cannot be negative.", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        File photoFile = view.getSelectedPhotoFile();
        String photoBase64 = null;
        if (photoFile != null) {
            try {
                photoBase64 = PhotoUtil.jpgFileToBase64(photoFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(view.frame,
                        "Could not read the selected photo. Please choose a valid JPG file.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                view.clearSelectedPhoto();
                return;
            }
            if (photoBase64 == null) {
                JOptionPane.showMessageDialog(view.frame,
                        "Could not read the selected photo. Please choose a valid JPG file.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                view.clearSelectedPhoto();
                return;
            }
        } else {
            int result = JOptionPane.showConfirmDialog(view.frame,
                    "No cover photo selected. Do you want to create the drive without a photo?",
                    "Photo Required",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            Client client = Client.getDefault();
            String userId = LoginController.currentUserEmail;
            if (userId == null)
                userId = "admin";

            String responseXml = client.createDonationDrive(userId, title, description, targetStr, photoBase64);
            Client.Response response = null;
            try {
                response = Client.parseResponse(responseXml);
            } catch (Exception e) {
                showError("Invalid response from server. The server may be misconfigured or overloaded.", e);
                return;
            }

            if (response != null && response.isOk()) {
                JOptionPane.showMessageDialog(view.frame, "Donation Drive Created Successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                view.frame.dispose();
                if (onSuccess != null) {
                    try {
                        onSuccess.run();
                    } catch (Exception e) {
                        showError("Drive was created but the list could not be refreshed.", e);
                    }
                }
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Failed to create drive. Please try again.";
                JOptionPane.showMessageDialog(view.frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            String message = "Unable to connect to the server. Please check that the server is running and try again.";
            if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
                message += "\n\nDetails: " + ex.getMessage();
            }
            JOptionPane.showMessageDialog(view.frame, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void showError(String userMessage, Exception ex) {
        if (ex != null)
            ex.printStackTrace();
        JOptionPane.showMessageDialog(view != null ? view.frame : null,
                userMessage + (ex != null && ex.getMessage() != null ? "\n\nDetails: " + ex.getMessage() : ""),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
