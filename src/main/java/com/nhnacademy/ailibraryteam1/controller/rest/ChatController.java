package com.nhnacademy.ailibraryteam1.controller.rest;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ChatController {

    private final ChatClient geminiChatClient;
    private final ChatClient ollamaChatClient;

    public ChatController(@Qualifier("geminiChatClient") ChatClient geminiChatClient,
                          @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {

        this.geminiChatClient = geminiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    @GetMapping("/api/chat")
    public ResponseEntity<String> chat(@RequestParam String question,
                                       @RequestParam(defaultValue = "ollama") String model,
                                       HttpSession session) {

        String conversationId = "web-%s".formatted(session.getId());
        log.info("[ChatController] 질문: {}, 모델: {}", question, model);

        ChatClient chatClient;
        if (model.equalsIgnoreCase("gemini")) {
            chatClient = this.geminiChatClient;
        } else {
            chatClient = this.ollamaChatClient;
        }

        String response = chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        log.info("[ChatController] 응답: {}", response);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}