package Network;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DonationDriverService extends Remote {

    String login(String email, String password) throws RemoteException;
    String register(String email, String password) throws RemoteException;
    String register(String firstName, String lastName, String middleName, String dateOfBirth, String address,
            String phone, String email, String password, String role) throws RemoteException;

}
