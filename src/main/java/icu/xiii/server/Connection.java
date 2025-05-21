package icu.xiii.server;

import icu.xiii.app.Packet;

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
        String json;
        try {
            while (!socket.isClosed()) {
                json = in.readLine();
                Packet packet = Server.gson.fromJson(json, Packet.class);
                packet.setSender(clientName);
                onPacketReceived(packet);
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

    public void close() throws IOException {
        socket.close();
        in.close();
        out.close();
    }

    private void onPacketReceived(Packet packet) throws IOException {
        if (packet.getMessage().startsWith("/")) {
            String[] cmd = packet.getMessage().substring(1).split(" ", 2);
            switch (cmd[0].toLowerCase()) {
                case "exit":
                    Server.disconnect(this);
                    break;
                case "private":
                    String[] msg = cmd[1].split(" ", 2);
                    if (msg.length < 2) {
                        System.out.println("Tried to send empty private message (from " + packet.getSender() + ")");
                    } else {
                        Server.sendTo(clientName, msg[0], msg[1]);
                    }
                    break;
                default:
                    System.out.printf("Unknown command %s (%s)\n",
                        cmd[0],
                        packet.getMessage()
                    );
            }
        } else {
            Server.broadcast(clientName, packet.getMessage());
        }
    }
}
