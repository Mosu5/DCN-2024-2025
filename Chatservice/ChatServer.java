import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {

    private static final int PORT = 12345;
    private static Map<String, String> clientStatuses = new HashMap<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        byte[] receiveData = new byte[1024];

        System.out.println("ChatServer gestart op poort " + PORT);

        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
            InetAddress clientAddress = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();
            String clientID = clientAddress.toString() + ":" + clientPort;

            if (message.startsWith("/")) {
                processCommand(clientID, message.trim(), serverSocket, clientAddress, clientPort);
            } else {
                broadcastMessage(clientID, message, serverSocket, true); 
            }
        }
    }

    private static void processCommand(String clientID, String command, DatagramSocket serverSocket, InetAddress clientAddress, int clientPort) throws Exception {
        String statusMessage = null;

        if (command.equals("/offline")) {
            statusMessage = clientID + ": status/offline";
            broadcastMessage(clientID, statusMessage, serverSocket, false);
            clientStatuses.put(clientID, "offline");
        } else if (command.equals("/beschikbaar")) {
            clientStatuses.put(clientID, "beschikbaar");
            statusMessage = clientID + ": status/beschikbaar";
            broadcastMessage(clientID, statusMessage, serverSocket, false);
        } else if (command.equals("/busy")) {
            clientStatuses.put(clientID, "busy");
            statusMessage = clientID + ": status/busy";
            broadcastMessage(clientID, statusMessage, serverSocket, false);
        }

        sendMessage("Status gewijzigd naar: " + command.substring(1), serverSocket, clientAddress, clientPort);
    }

    private static void broadcastMessage(String clientID, String message, DatagramSocket serverSocket, boolean includeClientID) throws Exception {
        if (!clientStatuses.containsKey(clientID) || clientStatuses.get(clientID).equals("offline")) {
            System.out.println("Bericht niet verzonden: " + clientID + " is offline.");
            return;
        }

        String fullMessage = includeClientID ? clientID + ": " + message : message;
        byte[] sendData = fullMessage.getBytes();

        for (String otherClientID : clientStatuses.keySet()) {
            if (!otherClientID.equals(clientID) && !clientStatuses.get(otherClientID).equals("offline")) {
                String[] parts = otherClientID.split(":");
                InetAddress otherClientAddress = InetAddress.getByName(parts[0].substring(1));
                int otherClientPort = Integer.parseInt(parts[1]);
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, otherClientAddress, otherClientPort);
                serverSocket.send(sendPacket);
            }
        }
    }

    private static void sendMessage(String message, DatagramSocket serverSocket, InetAddress clientAddress, int clientPort) throws Exception {
        byte[] sendData = message.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
        serverSocket.send(sendPacket);
    }
}
