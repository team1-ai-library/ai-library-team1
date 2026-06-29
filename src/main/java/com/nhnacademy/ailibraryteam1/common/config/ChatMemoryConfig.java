package com.nhnacademy.ailibraryteam1.common.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();

        // Telegram
        // String conversationId = "telegram-" + chatId;

        // Web
        // String conversationId = "web-" + sessionId;
        /*
        String response = chatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(
                    MessageChatMemoryAdvisor.CONVERSATION_ID_KEY, conversationId
                ))
                .call()
                .content();
         */
    }
}
