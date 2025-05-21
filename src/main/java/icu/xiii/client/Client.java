package icu.xiii.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import icu.xiii.app.LocalDateTimeDeserializer;
import icu.xiii.app.LocalDateTimeSerializer;
import icu.xiii.app.Packet;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

public class Client {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    public static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .create();

    public static void main(String[] args) {
        try {
            Connection connection = new Connection(new Socket(SERVER_HOST, SERVER_PORT));
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message:");
            while (!connection.getSocket().isClosed()) {
                String message = reader.readLine();
                if (!message.isBlank()) {
                    connection.send(message);
                    synchronized (Client.class) {
                        Client.class.wait(250);
                    }
                }
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}
