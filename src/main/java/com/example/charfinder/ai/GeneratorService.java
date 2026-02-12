package com.example.charfinder.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class GeneratorService {
    private final ChatClient geminiChatClient;

    public GeneratorService(ChatClient geminiChatClient) {
        this.geminiChatClient = geminiChatClient;
    }

    public String generateBackstory(String message) {
        var chatClient = this.geminiChatClient;
        return chatClient.prompt("Create a 2, 3 paragraphs of intriguing backstory for the given character information for Pathfinder 2e in Golarion lore")
                .user(message)
                .call()
                .content();
    }

}
