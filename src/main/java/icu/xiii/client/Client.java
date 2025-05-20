package icu.xiii.client;

import java.io.*;
import java.net.Socket;

public class Client {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private static BufferedReader reader;

    public static void main(String[] args) {
        try {
            Connection connection = new Connection(new Socket(SERVER_HOST, SERVER_PORT));
            reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message:");
            while (!connection.getSocket().isClosed()) {
                String packet = reader.readLine();
                connection.send(packet);
                Thread.sleep(1000);
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
