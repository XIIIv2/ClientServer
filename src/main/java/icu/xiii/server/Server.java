package icu.xiii.server;

import com.google.gson.*;
import icu.xiii.app.LocalDateTimeDeserializer;
import icu.xiii.app.LocalDateTimeSerializer;
import icu.xiii.app.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8080;
    private static final LinkedList<Connection> connections = new LinkedList<>();
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress(HOST, PORT));
            System.out.println("Server started. Listening " + server.getLocalSocketAddress().toString());
            while (true) {
                Socket socket = server.accept();
                try {
                    Connection connection = new Connection(socket, "Client-" + counter.getAndIncrement());
                    System.out.printf("Connected: %s at %s from %s\n",
                        connection.getClientName(),
                        dtf.format(connection.getConnectedAt()),
                        connection.getRemoteAddress()
                    );
                    broadcast("Server", connection.getClientName() + " connected.");
                    connections.add(connection);
                    sendTo("Server", connection.getClientName(), "Greetings! Type \"/exit\" for disconnect or \"/private Client-N text\" where N is client ID for private message");
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    socket.close();
                }
            }
        }
    }

    public static void disconnect(Connection connection) throws IOException {
        connections.remove(connection);
        connection.close();
        System.out.println("Disconnected: " + connection.getClientName());
        broadcast("Server", connection.getClientName() + " disconnected.");
    }

    public static void broadcast(String sender, String message) {
        System.out.printf("[%s] %s: %s\n", dtf.format(LocalDateTime.now()), sender, message);
        String json = gson.toJson(new Packet(sender, LocalDateTime.now(), message));
        connections.forEach(connection -> connection.send(json));
    }

    public static void sendTo(String from, String to, String message) {
        String json = gson.toJson(new Packet(from, LocalDateTime.now(), "private [" + to + "]: " + message));
        Optional<Connection> connection = connections.stream()
                .filter(c -> c.getClientName().equalsIgnoreCase(to))
                .findFirst();
        if (connection.isPresent()) {
            connection.get().send(json);
            System.out.printf("[%s] %s private [%s]: %s\n",
                    dtf.format(LocalDateTime.now()),
                    from,
                    connection.get().getClientName(),
                    message
            );
        } else {
            System.out.printf("[%s] can't send private message from %s to %s (user not found).",
                    dtf.format(LocalDateTime.now()),
                    from,
                    to
            );
        }
    }
}
