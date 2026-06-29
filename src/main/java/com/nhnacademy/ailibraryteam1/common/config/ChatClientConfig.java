package com.nhnacademy.ailibraryteam1.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final ToolCallbackProvider mcpTools;

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(@Qualifier("googleGenAiChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(mcpTools)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(@Qualifier("ollamaChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(mcpTools)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}