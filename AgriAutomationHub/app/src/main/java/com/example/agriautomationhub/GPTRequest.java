package com.example.agriautomationhub;

import java.util.List;

public class GPTRequest {
    private String model;
    private List<Message> messages;

    public GPTRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}

