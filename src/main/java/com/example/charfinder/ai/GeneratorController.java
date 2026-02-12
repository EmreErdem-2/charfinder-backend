package com.example.charfinder.ai;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/generate")
public class GeneratorController {
    private final GeneratorService generatorService;
    private final ChatClient geminiChatClient;
    @Autowired
    public GeneratorController(GeneratorService generatorService, ChatClient  geminiChatClient) {
        this.generatorService = generatorService;
        this.geminiChatClient = geminiChatClient;
    }
    @PostMapping("/query")
    public ResponseEntity<ChatResponse> chat(@RequestBody String characterJson) {
        String response = generatorService.generateBackstory(characterJson);
        return ResponseEntity.ok(new ChatResponse(response, characterJson));
    }

}
