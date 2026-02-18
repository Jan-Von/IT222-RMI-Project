package Controller;

import View.LoginView;
import View.RegistrationView;
import javax.swing.*;
import java.io.IOException;
import Network.Client;
import javax.swing.*;

public class RegistrationController {

    private final RegistrationView view;

    public RegistrationController(RegistrationView view) {
        this.view = view;
        this.view.finishButton.addActionListener(e -> register());
        this.view.backButton.addActionListener(e -> goBack());
    }

    private void goBack() {
        LoginView loginView = new LoginView();
        new LoginController(loginView);
        view.frame.dispose();
    }

    private void register() {
        String firstName = view.firstNameField.getText().trim();
        String lastName = view.lastNameField.getText().trim();
        String middleName = view.middleNameField.getText().trim();
        String dateOfBirth = view.dateOfBirthField.getText().trim();
        String address = view.addressField.getText().trim();
        String phone = view.phoneNumberField.getText().trim();
        String email = view.emailField.getText().trim();
        String password = view.passwordField.getText().trim();
        String confirm = view.confirmPasswordField.getText().trim();
        boolean termsAccepted = view.termsCheck.isSelected();
        String role = (String) view.roleBox.getSelectedItem();
        if (role == null)
            role = "Donor";

        // Basic validation (fields you already have in the GUI)
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(view.frame,
                    "Please fill in all required fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(view.frame,
                    "Password and confirmation do not match.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!termsAccepted) {
            JOptionPane.showMessageDialog(view.frame,
                    "You must agree to the terms and agreements.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Client client = Client.getDefault();
            String responseXml = client.register(
                    firstName,
                    lastName,
                    middleName,
                    dateOfBirth,
                    address,
                    phone,
                    email,
                    password,
                    role.toUpperCase());
            Client.Response response = Client.parseResponse(responseXml);

            if (response != null && response.isOk()) {
                JOptionPane.showMessageDialog(view.frame,
                        "Registration successful. You can now log in.");
                LoginView loginView = new LoginView();
                new LoginController(loginView);
                view.frame.dispose();
            } else {
                String msg = (response != null && response.message != null && !response.message.isEmpty())
                        ? response.message
                        : "Registration failed. Please try again.";
                JOptionPane.showMessageDialog(view.frame,
                        msg,
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view.frame,
                    "Unable to contact server. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
