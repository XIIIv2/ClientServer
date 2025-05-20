package icu.xiii.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    private static final int PORT = 8080;
    private static final LinkedList<Connection> connections = new LinkedList<>();
    private static final AtomicInteger counter = new AtomicInteger(1);
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss");

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(PORT)) {
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
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                    socket.close();
                }
            }
        }
    }

    public static void disconnect(Connection connection) {
        connections.remove(connection);
        System.out.println("Disconnected: " + connection.getClientName());
        broadcast("Server", connection.getClientName() + " disconnected.");
    }

    public static void broadcast(String sender, String message) {
        String msg = String.format("[%s] %s: %s", dtf.format(LocalDateTime.now()), sender, message);
        System.out.println(msg);
        connections.forEach(connection -> connection.send(msg));
    }
}
