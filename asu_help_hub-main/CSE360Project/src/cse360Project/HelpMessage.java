package cse360Project.model;

import java.time.LocalDateTime;

public class HelpMessage {
    private int id;
    private int userId;
    private MessageType messageType;
    private String content;
    private LocalDateTime timestamp;

    public enum MessageType {
        GENERIC,
        SPECIFIC
    }

    // Constructors
    public HelpMessage(int userId, MessageType messageType, String content) {
        this.userId = userId;
        this.messageType = messageType;
        this.content = content;
    }

    public HelpMessage(int id, int userId, MessageType messageType, String content, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.messageType = messageType;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "HelpMessage{" +
                "id=" + id +
                ", userId=" + userId +
                ", messageType=" + messageType +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
