package Admin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class AdminCredentialsStore {

    private static final String ADMIN_XML_RELATIVE = "src/Admin/admin_credentials.xml";

    public boolean isValidAdmin(String email, String password) {
        if (email == null || password == null) {
            return false;
        }
        email = email.trim();
        password = password.trim();
        if (email.isEmpty() || password.isEmpty()) {
            return false;
        }

        try {
            File file = getAdminXmlFile();
            if (!file.exists()) {
                return false;
            }

            Document doc = loadDocument(file);
            NodeList admins = doc.getElementsByTagName("admin");
            for (int i = 0; i < admins.getLength(); i++) {
                Element admin = (Element) admins.item(i);
                String xmlEmail = getTextContent(admin, "email");
                String xmlPassword = getTextContent(admin, "password");

                if (xmlEmail != null && xmlPassword != null
                        && xmlEmail.trim().equals(email)
                        && xmlPassword.equals(password)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static File getAdminXmlFile() {
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File candidate = new File(dir, ADMIN_XML_RELATIVE);
            if (candidate.exists()) {
                return candidate;
            }
        }
        return new File(cwd, ADMIN_XML_RELATIVE);
    }

    private static Document loadDocument(File file) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(file);
    }

    private static String getTextContent(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) {
            return null;
        }
        Element el = (Element) list.item(0);
        return el.getTextContent() != null ? el.getTextContent().trim() : null;
    }
}