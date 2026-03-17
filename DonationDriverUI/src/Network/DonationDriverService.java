package Network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DonationDriverService extends Remote {

    // Auth Methods
    String login(String email, String password) throws RemoteException;
    String register(String email, String password) throws RemoteException;
    String register(String firstName, String lastName, String middleName, String dateOfBirth, String address,
            String phone, String email, String password, String role) throws RemoteException;
    String logout(String email) throws RemoteException;
    String updateUserRole(String email, String newRole) throws RemoteException;

    // Ticket Methods
    String createTicket(String userId, String type, String details) throws RemoteException;
    String createTicket(String userId, String itemCategory, int quantity, String condition, String expirationDate,
            String pickupDateTime, String pickupLocation, String photoPath, String notes,
            String photoBase64) throws RemoteException;
    String createTicket(String userId, String itemCategory, int quantity, String condition, String expirationDate,
            String pickupDateTime, String pickupLocation, String photoPath, String notes,
            String donationDrive, String deliveryDestination, String photoBase64) throws RemoteException;
            
    String readTickets(String userId) throws RemoteException;
    String readTickets(String userId, String status) throws RemoteException;
    
    String updateTicket(String userId, String ticketId, String status) throws RemoteException;
    String updateTicket(String userId, String ticketId, String status, String qualityStatus, String qualityReason) throws RemoteException;
    String updateTicketPickupTime(String userId, String ticketId, String pickupDateTime) throws RemoteException;
    
    String deleteTicket(String userId, String ticketId) throws RemoteException;
    String deleteTicket(String userId, String ticketId, String reason) throws RemoteException;
    String permanentDeleteTicket(String adminUserId, String ticketId) throws RemoteException;

    // Donation Drive Methods
    String createDonationDrive(String userId, String title, String description, String targetAmount, String photoBase64) throws RemoteException;
    String readDonationDrives() throws RemoteException;
    String deleteDonationDrive(String userId, String driveTitle) throws RemoteException;
    String updateDriveAmount(String driveTitle, double amount) throws RemoteException;

    // Search Methods
    String searchTickets(String keyword) throws RemoteException;
    String searchDonationDrives(String keyword) throws RemoteException;
    String ping() throws RemoteException;
    
    // Rider Methods
    String setRiderAvailable(String userId) throws RemoteException;
    String setRiderUnavailable(String userId) throws RemoteException;

    // Admin Methods
    String getServerLogs() throws RemoteException;
    String setServerMaintenanceMode(boolean enabled) throws RemoteException;
}
