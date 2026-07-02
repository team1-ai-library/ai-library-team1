package com.nhnacademy.ailibraryteam1.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Chat;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@Slf4j
public class ChatController {

    private final ChatClient geminiChatClient;
    private final ChatClient ollamaChatClient;
    private final ChatClient localChatClient;
    private final ObjectMapper objectMapper;

    public ChatController(@Qualifier("geminiChatClient") ChatClient geminiChatClient,
                          @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                          @Qualifier("localChatClient") ChatClient localChatClient,
                          ObjectMapper objectMapper) {

        this.geminiChatClient = geminiChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.localChatClient = localChatClient;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/api/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String question,
                             @RequestParam(defaultValue = "ollama") String model,
                             HttpSession session) {

        String conversationId = "web-%s".formatted(session.getId());
        log.info("[ChatController] 질문: {}, 모델: {}", question, model);

        ChatClient chatClient;
        if (model.equalsIgnoreCase("gemini")) {
            chatClient = this.geminiChatClient;
        } else if (model.equalsIgnoreCase("local")) {
            chatClient = this.localChatClient;
        } else {
            chatClient = this.ollamaChatClient;
        }

        Flux<String> contentStream = chatClient.prompt()
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .map(this::toJsonChunk)
                .doOnError(e -> log.info("[ChatController] 스트리밍 에러: {}", e.getMessage()));
        return Flux.concat(contentStream, Flux.just("[DONE]"))
                .doOnComplete(() -> log.info("[ChatController] ChatBot 스트리밍 완료"));
    }

    private String toJsonChunk(String chunk) {
        try {
            return objectMapper.writeValueAsString(Map.of("content", chunk));
        } catch (JsonProcessingException e) {
            return "{\"content\":\"\"}";
        }
    }
}