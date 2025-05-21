package icu.xiii.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.xiii.app.LocalDateTimeDeserializer;
import icu.xiii.app.LocalDateTimeSerializer;
import icu.xiii.app.Packet;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Connection extends Thread {

    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");


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
                    if (!socket.isConnected()) {
                        System.out.println("Connecting...");
                        Thread.sleep(1000);
                        continue;
                    }
                    if (socket.isClosed()) {
                        break;
                    }
                    String json = in.readLine();
                    if (json == null) {
                        break;
                    }
                    Packet packet = Client.gson.fromJson(json, Packet.class);
                    onPacketReceived(packet);
                }
            } finally {
                close();
                System.out.println("Disconnected.");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }
    }

    public void send(String message) {
        if (!socket.isClosed()) {
            try {
                Packet packet = new Packet(message);
                String json = Client.gson.toJson(packet);
                out.write(json + "\n");
                out.flush();
            } catch (IOException e) {
                System.out.println("Client::send error: " + e.getMessage());
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void close() throws IOException {
        socket.close();
        in.close();
        out.close();
    }

    private void onPacketReceived(Packet packet) {
        System.out.printf("[%s] %s: %s\n",
                dtf.format(packet.getTimestamp()),
                packet.getSender(),
                packet.getMessage()
        );
    }
}
