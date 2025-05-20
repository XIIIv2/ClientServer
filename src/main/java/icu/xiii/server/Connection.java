package icu.xiii.server;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class Connection extends Thread {

    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final String clientName;
    private final LocalDateTime connectedAt;

    public Connection(Socket socket, String clientName) throws IOException {
        this.socket = socket;
        this.clientName = clientName;
        this.connectedAt = LocalDateTime.now();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }

    @Override
    public void run() {
        String packet;
        try {
            while (true) {
                packet = in.readLine();
                if (packet.equalsIgnoreCase("exit")) {
                    Server.disconnect(this);
                    socket.close();
                    in.close();
                    out.close();
                    break;
                }
                Server.broadcast(clientName, packet);
            }
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    public void send(String message) {
        if (!socket.isClosed()) {
            try {
                out.write(message + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Connection::send error: " + e.getMessage());
            }
        }
    }

    public String getClientName() {
        return this.clientName;
    }

    public LocalDateTime getConnectedAt() {
        return this.connectedAt;
    }

    public String getRemoteAddress() {
        return this.socket.getRemoteSocketAddress().toString();
    }
}
