import java.io.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SecureClient {
    public static void main(String[] args) {
        String host = "outlook.office365.com";
        int port = 995;
        String trustStorePath = "POP3Service\\Cert\\truststore.jks";
        String trustStorePassword = System.getenv("TRUSTSTORE_PASSWORD");
        
        String email = System.getenv("EMAIL");
        String emailPassword = System.getenv("EMAIL_PASSWORD");

        if (trustStorePassword == null || emailPassword == null) {
            System.err.println("Environment variables TRUSTSTORE_PASSWORD, EMAIL, and EMAIL_PASSWORD must be set.");
            return;
        }

        try {
            // Load the trust store
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream trustStoreStream = new FileInputStream(trustStorePath)) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            // Initialize TrustManagerFactory with the trust store
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Initialize SSLContext with the trust managers
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Create SSLSocketFactory from the SSLContext
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create SSLSocket and connect to the server
            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(host, port)) {
                // Start the handshake
                sslSocket.startHandshake();

                // Get input and output streams
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                     PrintWriter writer = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()), true)) {

                    // Send a request to the server
                    writer.println("USER "+ email);
                    writer.println("PASS " + emailPassword);

                    // Read the response from the server
                    String response;
                    while ((response = reader.readLine()) != null) {
                        System.out.println("Server response: " + response);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}