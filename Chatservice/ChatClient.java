import java.net.*;
import java.util.Scanner;

public class ChatClient {

    private static final int SERVER_PORT = 12345; 

    public static void main(String[] args) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName("localhost");

        Thread receiveThread = new Thread(() -> {
            try {
                byte[] receiveData = new byte[1024];
                while (true) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

        Scanner scanner = new Scanner(System.in);

        System.out.println("Voer /beschikbaar, /busy of /offline in om je status te veranderen.");
        while (true) {
            String message = scanner.nextLine();
            sendMessage(message, clientSocket, serverAddress, SERVER_PORT);
            if (message.equals("/offline")) {
                clientSocket.close();
                break;
            }
        }
    }

    private static void sendMessage(String message, DatagramSocket clientSocket, InetAddress serverAddress, int serverPort) throws Exception {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
        clientSocket.send(sendPacket);
    }
}