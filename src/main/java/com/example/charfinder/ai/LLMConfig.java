package com.example.charfinder.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LLMConfig {

    @Bean
    @Primary
    @Qualifier("geminiChatClient")
    public ChatClient geminiChatModel(
            OpenAiChatModel baseChatModel,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.url}") String geminiUrl,
            @Value("${gemini.api.completions.path}") String completionsPath,
            @Value("${gemini.model.name}") String modelName) {

        var geminiApi = OpenAiApi.builder()
                .baseUrl(geminiUrl)
                .completionsPath(completionsPath)
                .apiKey(apiKey)
                .build();

        var customModel = baseChatModel.mutate()
                .openAiApi(geminiApi)
                .defaultOptions(OpenAiChatOptions.builder().model(modelName).build())
                .build();

        return ChatClient.create(customModel);
    }
}