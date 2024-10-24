import java.net.*;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatServer {

    private static final int PORT = 12345;
    private static Map<String, SecretKey> clientKeys = new HashMap<>();

    private static KeyPair keyPair;

    public static void main(String[] args) throws Exception {
        // Genereer RSA-sleutelpaar
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        System.out.println("RSA-sleutelpaar gegenereerd.");

        DatagramSocket serverSocket = new DatagramSocket(PORT);
        byte[] receiveData = new byte[2048];

        System.out.println("ChatServer gestart op poort " + PORT);

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            String clientID = clientAddress.toString() + ":" + clientPort;

            byte[] receivedBytes = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), 0, receivedBytes, 0, receivePacket.getLength());

            // Als de client nog geen symmetrische sleutel heeft gestuurd
            if (!clientKeys.containsKey(clientID)) {
                String receivedText = new String(receivedBytes);
                if (receivedText.equals("/getPublicKey")) {
                    System.out.println("Ontvangen verzoek om publieke sleutel van client " + clientID);
                    sendPublicKey(serverSocket, clientAddress, clientPort);
                    System.out.println("Publieke sleutel verzonden naar client " + clientID);
                } else {
                    // Ontsleutel de symmetrische sleutel die door de client is gestuurd
                    System.out.println("Versleutelde symmetrische sleutel ontvangen van client " + clientID);
                    byte[] decryptedKey = decryptWithPrivateKey(receivedBytes, keyPair.getPrivate());
                    SecretKey symmetricKey = new SecretKeySpec(decryptedKey, 0, decryptedKey.length, "AES");
                    clientKeys.put(clientID, symmetricKey);
                    System.out.println("Symmetrische sleutel opgeslagen voor client " + clientID);
                }
                continue;
            }

            // Ontsleutel binnenkomend bericht
            System.out.println("Ontvangen versleuteld bericht van client " + clientID);
            SecretKey symmetricKey = clientKeys.get(clientID);
            String message = decryptMessage(receivedBytes, symmetricKey);
            System.out.println("Bericht succesvol ontsleuteld: \"" + message + "\"");

            if (message.startsWith("/")) {
                processCommand(clientID, message.trim(), serverSocket, clientAddress, clientPort);
            } else {
                broadcastMessage(clientID, message, serverSocket, true);
            }
        }
    }

    private static void sendPublicKey(DatagramSocket serverSocket, InetAddress clientAddress, int clientPort) throws Exception {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        DatagramPacket sendPacket = new DatagramPacket(publicKeyBytes, publicKeyBytes.length, clientAddress, clientPort);
        serverSocket.send(sendPacket);
    }

    private static byte[] decryptWithPrivateKey(byte[] data, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    private static String decryptMessage(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, data, 0, 12); // Eerste 12 bytes voor IV
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        byte[] decryptedData = cipher.doFinal(data, 12, data.length - 12);
        return new String(decryptedData);
    }

    private static void processCommand(String clientID, String command, DatagramSocket serverSocket, InetAddress clientAddress, int clientPort) throws Exception {
        System.out.println("Ontvangen commando van client " + clientID + ": \"" + command + "\"");
        // Hier kun je je statusafhandeling implementeren (zoals in de originele code)
        sendEncryptedMessage("Commando ontvangen: " + command, serverSocket, clientAddress, clientPort, clientKeys.get(clientID));
    }

    private static void broadcastMessage(String clientID, String message, DatagramSocket serverSocket, boolean includeClientID) throws Exception {
        String fullMessage = includeClientID ? clientID + ": " + message : message;
        System.out.println("Bericht wordt verzonden naar alle clients: \"" + fullMessage + "\"");

        for (Map.Entry<String, SecretKey> entry : clientKeys.entrySet()) {
            String otherClientID = entry.getKey();
            SecretKey key = entry.getValue();

            if (!otherClientID.equals(clientID)) {
                String[] parts = otherClientID.split(":");
                InetAddress otherClientAddress = InetAddress.getByName(parts[0].substring(1));
                int otherClientPort = Integer.parseInt(parts[1]);
                sendEncryptedMessage(fullMessage, serverSocket, otherClientAddress, otherClientPort, key);
                System.out.println("Bericht verzonden naar client " + otherClientID);
            }
        }
    }

    private static void sendEncryptedMessage(String message, DatagramSocket serverSocket, InetAddress clientAddress, int clientPort, SecretKey key) throws Exception {
        byte[] encryptedMessage = encryptMessage(message, key);
        DatagramPacket sendPacket = new DatagramPacket(encryptedMessage, encryptedMessage.length, clientAddress, clientPort);
        serverSocket.send(sendPacket);
    }

    private static byte[] encryptMessage(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12); // 12 bytes voor GCM IV
        GCMParameterSpec params = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, params);
        byte[] encryptedData = cipher.doFinal(message.getBytes());
        byte[] encryptedMessage = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, encryptedMessage, 0, iv.length);
        System.arraycopy(encryptedData, 0, encryptedMessage, iv.length, encryptedData.length);
        return encryptedMessage;
    }
}
