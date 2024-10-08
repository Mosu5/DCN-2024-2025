import java.io.*;
import java.net.*;
import com.google.gson.*;



public class LibraryServer {
    public static void main(String[] args) throws IOException {

        // if (args.length != 1) {
        //     System.err.println("Usage: java KnockKnockServer <port number>");
        //     System.exit(1);
        // }

        int portNumber = 5440;

        try (
                ServerSocket serverSocket = new ServerSocket(portNumber);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {

            String inputLine, outputLine;

            // Initiate conversation with client
            LibraryProtocol lp = new LibraryProtocol();
            outputLine = lp.processInput(null);
            out.println(outputLine);

            while ((inputLine = in.readLine()) != null) {
                outputLine = lp.processInput(inputLine);
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}