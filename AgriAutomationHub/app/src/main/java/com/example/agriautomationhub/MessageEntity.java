package com.example.agriautomationhub;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;  // Primary key with auto-generation

    private String message;  // Actual message
    private boolean sentByUser;  // Whether the message is sent by the user
    private String timestamp;  // Timestamp for the message

    // Constructor
    public MessageEntity(String message, boolean sentByUser, String timestamp) {
        this.message = message;
        this.sentByUser = sentByUser;
        this.timestamp = timestamp;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setters (optional, depending on your use case)
    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentByUser(boolean sentByUser) {
        this.sentByUser = sentByUser;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
