package Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Persists RMI registry host/port for the desktop client under {@code DonationDriverUI/data/rmi_client.properties}.
 */
public final class RmiClientConfig {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 5267;

    private static final String FILE_NAME = "rmi_client.properties";

    public static final class Endpoint {
        public final String host;
        public final int port;

        public Endpoint(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private RmiClientConfig() {}

    private static File resolveDataDir() {
        File cwd = new File(System.getProperty("user.dir"));
        for (File dir = cwd; dir != null; dir = dir.getParentFile()) {
            File donationDriverDir = new File(dir, "DonationDriverUI");
            if (donationDriverDir.isDirectory()) {
                File dataDir = new File(donationDriverDir, "data");
                dataDir.mkdirs();
                return dataDir;
            }
        }
        File fallback = new File(cwd, "DonationDriverUI/data");
        fallback.mkdirs();
        return fallback;
    }

    private static File propsFile() {
        return new File(resolveDataDir(), FILE_NAME);
    }

    public static Endpoint load() {
        File f = propsFile();
        if (!f.isFile()) {
            return new Endpoint(DEFAULT_HOST, DEFAULT_PORT);
        }
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (Exception e) {
            return new Endpoint(DEFAULT_HOST, DEFAULT_PORT);
        }
        String host = p.getProperty("host", DEFAULT_HOST);
        if (host == null || host.trim().isEmpty()) {
            host = DEFAULT_HOST;
        }
        int port = DEFAULT_PORT;
        try {
            String ps = p.getProperty("port");
            if (ps != null && !ps.trim().isEmpty()) {
                port = Integer.parseInt(ps.trim());
            }
        } catch (NumberFormatException ignored) {
            port = DEFAULT_PORT;
        }
        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }
        return new Endpoint(host.trim(), port);
    }

    public static void save(String host, int port) throws IOException {
        String h = (host == null || host.trim().isEmpty()) ? DEFAULT_HOST : host.trim();
        if (port < 1 || port > 65535) {
            port = DEFAULT_PORT;
        }
        Properties p = new Properties();
        p.setProperty("host", h);
        p.setProperty("port", String.valueOf(port));
        File f = propsFile();
        File parent = f.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(f)) {
            p.store(out, "RMI client endpoint");
        }
    }
}
