package nl.avans;

import java.net.*;
import java.io.*;

public class Main {

    public static void main(String[] args) throws IOException {
        int portNumber = 25;
        boolean listening = true;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (listening) {
                new SmtpContext(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }
}

