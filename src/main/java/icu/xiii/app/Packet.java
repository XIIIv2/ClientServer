package icu.xiii.app;

import java.time.LocalDateTime;


public class Packet {

    private String sender;
    private LocalDateTime timestamp;
    private String message;

    public Packet() {

    }

    public Packet(String message) {
        this();
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public Packet(String sender, LocalDateTime timestamp, String message) {
        this();
        this.sender = sender;
        this.timestamp =  timestamp;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "from='" + sender + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
