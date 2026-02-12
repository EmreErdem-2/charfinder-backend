package com.example.charfinder.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String originalMessage;
    private long timestamp;

    public ChatResponse(String response, String originalMessage) {
        this.response = response;
        this.originalMessage = originalMessage;
        this.timestamp = System.currentTimeMillis();
    }
    // Getters and setters
}