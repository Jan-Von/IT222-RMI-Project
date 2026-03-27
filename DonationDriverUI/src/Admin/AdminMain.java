package Admin;

public class AdminMain {
    public static void main(String[] args) {
        AdminLoginView adminLoginView = new AdminLoginView();
        new AdminLoginController(adminLoginView);
    }
}
