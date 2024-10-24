import java.net.*;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import javax.crypto.*;
import javax.crypto.spec.*;

public class ChatClient {

    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName("localhost");

        // Request server's public key
        byte[] requestKey = "/getPublicKey".getBytes();
        DatagramPacket requestPacket = new DatagramPacket(requestKey, requestKey.length, serverAddress, SERVER_PORT);
        clientSocket.send(requestPacket);

        // Receive server's public key
        byte[] keyBuffer = new byte[2048];
        DatagramPacket keyPacket = new DatagramPacket(keyBuffer, keyBuffer.length);
        clientSocket.receive(keyPacket);
        byte[] publicKeyBytes = new byte[keyPacket.getLength()];
        System.arraycopy(keyPacket.getData(), 0, publicKeyBytes, 0, keyPacket.getLength());
        PublicKey serverPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        // Generate symmetric key and send it encrypted to the server
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey symmetricKey = keyGen.generateKey();
        byte[] encryptedKey = encryptWithPublicKey(symmetricKey.getEncoded(), serverPublicKey);
        DatagramPacket keySendPacket = new DatagramPacket(encryptedKey, encryptedKey.length, serverAddress, SERVER_PORT);
        clientSocket.send(keySendPacket);

        // Start thread to receive messages
        Thread receiveThread = new Thread(() -> {
            try {
                byte[] receiveData = new byte[2048];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    byte[] receivedBytes = new byte[receivePacket.getLength()];
                    System.arraycopy(receivePacket.getData(), 0, receivedBytes, 0, receivePacket.getLength());
                    String message = decryptMessage(receivedBytes, symmetricKey);
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Voer je berichten in:");
        while (true) {
            String message = scanner.nextLine();
            byte[] encryptedMessage = encryptMessage(message, symmetricKey);
            DatagramPacket sendPacket = new DatagramPacket(encryptedMessage, encryptedMessage.length, serverAddress, SERVER_PORT);
            clientSocket.send(sendPacket);
            if (message.equals("/offline")) {
                clientSocket.close();
                break;
            }
        }
    }

    private static byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    private static byte[] encryptMessage(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
        GCMParameterSpec params = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, params);
        byte[] encryptedData = cipher.doFinal(message.getBytes());
        byte[] encryptedMessage = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, encryptedMessage, 0, iv.length);
        System.arraycopy(encryptedData, 0, encryptedMessage, iv.length, encryptedData.length);
        return encryptedMessage;
    }

    private static String decryptMessage(byte[] data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, data, 0, 12);
        cipher.init(Cipher.DECRYPT_MODE, key, params);
        byte[] decryptedData = cipher.doFinal(data, 12, data.length - 12);
        return new String(decryptedData);
    }
}
