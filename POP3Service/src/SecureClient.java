import java.io.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SecureClient {
    private static final String HOST = "outlook.office365.com";
    private static final int PORT = 995;
    private static final String TRUST_STORE_PATH = "POP3Service\\Cert\\truststore.jks";

    private SSLSocket sslSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public void connect() {
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
            try (FileInputStream trustStoreStream = new FileInputStream(TRUST_STORE_PATH)) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            // Initialize TrustManagerFactory with the trust store
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // Initialize SSLContext with the trust managers
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // Create SSLSocketFactory from the SSLContext
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create SSLSocket and connect to the server
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(HOST, PORT);

            // Start the handshake
            sslSocket.startHandshake();

            // Get input and output streams
            reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(sslSocket.getOutputStream()), true);

            // Send a request to the server
            writer.println("USER " + email);
            writer.println("PASS " + emailPassword);

            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println("Server response: " + response);
                if (response.startsWith("+OK")) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(String command) {
        try {

            //check if the command is empty
            if (command.isEmpty()) return;
            

            // Send the command to the server
            writer.println(command);

            String response;
            StringBuilder allResponses = new StringBuilder(); // Create a StringBuilder to store all responses
            while ((response = reader.readLine()) != null) {
                // System.out.println("Server response: " + response);
                allResponses.append(response).append("\n"); // Append each response to the StringBuilder
                if (response.equals(".")) {
                    break;
                }
            }

            // Print all responses to a file
            try (PrintWriter out = new PrintWriter("output.txt")) {
                out.println(allResponses.toString());
            }

           
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            // Send QUIT command to end the session
            writer.println("QUIT");
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println("Server response: " + response);
            }

            // Close the streams and socket
            reader.close();
            writer.close();
            sslSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SecureClient client = new SecureClient();
        client.connect();
        // send a POP3 command to the server for the top 10 emails
        client.sendCommand("TOP 10 0");
        client.close();
    }
}