package icu.xiii.client;

import java.io.*;
import java.net.Socket;

public class Connection extends Thread {

    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        try {
            try {
                while (true) {
                    if (socket.isClosed()) {
                        break;
                    }
                    String packet = in.readLine();
                    if (packet == null) {
                        break;
                    }
                    System.out.println(packet);
                }
            } finally {
                socket.close();
                in.close();
                out.close();
                System.out.println("Disconnected.");
            }
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }
    }

    public void send(String message) {
        if (!socket.isClosed()) {
            try {
                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Client::send error: " + e.getMessage());
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
