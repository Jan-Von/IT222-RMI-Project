import Network.DonationDriverService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class Server extends JFrame implements DonationDriverService {

    private static final int PORT = 5267;
    private JTextArea logArea;
    private JButton startBtn;
    private JButton stopBtn;
    private Registry registry;
    private final Set<String> activeSessions = new HashSet<>();

    public Server() throws RemoteException {
        super("DonationDriver RMI Server");
        initUI();
    }

    private void initUI() {
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        startBtn = new JButton("Start Server");
        stopBtn = new JButton("Stop Server");
        stopBtn.setEnabled(false);

        startBtn.addActionListener(e -> startServer());
        stopBtn.addActionListener(e -> stopServer());

        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    private void startServer() {
        try {
            registry = LocateRegistry.createRegistry(PORT);
            UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("rmi://localhost:" + PORT + "/DonationDriverService", this);
            log("RMI Server started and bound on port " + PORT);
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        } catch (Exception e) {
            log("Failed to start server: " + e.getMessage());
        }
    }

    private void stopServer() {
        try {
            Naming.unbind("rmi://localhost:" + PORT + "/DonationDriverService");
            UnicastRemoteObject.unexportObject(this, true);
            UnicastRemoteObject.unexportObject(registry, true);
            log("RMI Server stopped.");
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        } catch (Exception e) {
            log("Error stopping server: " + e.getMessage());
        }
    }

    // Dummy returns & initial methods no body
    @Override public String login(String email, String password) throws RemoteException { return ""; }
    @Override public String register(String email, String password) throws RemoteException { return ""; }
    @Override public String register(String firstName, String lastName, String middleName, String dateOfBirth, String address, String phone, String email, String password, String role) throws RemoteException { return ""; }
    @Override public String logout(String email) throws RemoteException { return ""; }
    @Override public String updateUserRole(String email, String newRole) throws RemoteException { return ""; }
    @Override public String createTicket(String userId, String type, String details) throws RemoteException { return ""; }
    @Override public String createTicket(String userId, String itemCategory, int quantity, String condition, String expirationDate, String pickupDateTime, String pickupLocation, String photoPath, String notes, String photoBase64) throws RemoteException { return ""; }
    @Override public String createTicket(String userId, String itemCategory, int quantity, String condition, String expirationDate, String pickupDateTime, String pickupLocation, String photoPath, String notes, String donationDrive, String deliveryDestination, String photoBase64) throws RemoteException { return ""; }
    @Override public String readTickets(String userId) throws RemoteException { return ""; }
    @Override public String readTickets(String userId, String status) throws RemoteException { return ""; }
    @Override public String updateTicket(String userId, String ticketId, String status) throws RemoteException { return ""; }
    @Override public String updateTicket(String userId, String ticketId, String status, String qualityStatus, String qualityReason) throws RemoteException { return ""; }
    @Override public String updateTicketPickupTime(String userId, String ticketId, String pickupDateTime) throws RemoteException { return ""; }
    @Override public String deleteTicket(String userId, String ticketId) throws RemoteException { return ""; }
    @Override public String deleteTicket(String userId, String ticketId, String reason) throws RemoteException { return ""; }
    @Override public String permanentDeleteTicket(String adminUserId, String ticketId) throws RemoteException { return ""; }
    @Override public String createDonationDrive(String userId, String title, String description, String targetAmount, String photoBase64) throws RemoteException { return ""; }
    @Override public String readDonationDrives() throws RemoteException { return ""; }
    @Override public String deleteDonationDrive(String userId, String driveTitle) throws RemoteException { return ""; }
    @Override public String updateDriveAmount(String driveTitle, double amount) throws RemoteException { return ""; }
    @Override public String searchTickets(String keyword) throws RemoteException { return ""; }
    @Override public String searchDonationDrives(String keyword) throws RemoteException { return ""; }
    @Override public String ping() throws RemoteException { return ""; }
    @Override public String setRiderAvailable(String userId) throws RemoteException { return ""; }
    @Override public String setRiderUnavailable(String userId) throws RemoteException { return ""; }
    @Override public String getServerLogs() throws RemoteException { return ""; }
    @Override public String setServerMaintenanceMode(boolean enabled) throws RemoteException { return ""; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Server srv = new Server();
                srv.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
