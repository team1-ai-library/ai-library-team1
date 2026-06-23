package com.nhnacademy.ailibraryteam1.common.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatModel geminiChatModel(@Qualifier("googleGenAiChatModel") ChatModel chatModel) {
        return chatModel;
    }
}
