package com.nhnacademy.ailibraryteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ailibraryteam1.controller.rest.ChatController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "geminiChatClient")
    private ChatClient geminiChatClient;

    @MockitoBean(name = "ollamaChatClient")
    private ChatClient ollamaChatClient;

    @MockitoBean(name = "localChatClient")
    private ChatClient localChatClient;

    @Autowired
    private ObjectMapper objectMapper;

    private void mockChatClientStream(ChatClient chatClient, String... chunks) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.StreamResponseSpec streamSpec = mock(ChatClient.StreamResponseSpec.class);

        given(chatClient.prompt()).willReturn(requestSpec);
        given(requestSpec.user(any(String.class))).willReturn(requestSpec);
        given(requestSpec.advisors(any(Consumer.class))).willReturn(requestSpec);  // advisorSpec 제거, requestSpec 재사용
        given(requestSpec.stream()).willReturn(streamSpec);
        given(streamSpec.content()).willReturn(Flux.just(chunks));
    }

    @Test
    @DisplayName("ollama 모델로 질문하면 SSE 스트림을 반환한다")
    void chat_WithOllama_ReturnsStream() throws Exception {

        mockChatClientStream(ollamaChatClient, "안녕", "하세요");

        mockMvc.perform(get("/api/chat")
                        .param("question", "안녕하세요")
                        .param("model", "ollama"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("gemini 모델로 질문하면 SSE 스트림을 반환한다")
    void chat_WithGemini_ReturnsStream() throws Exception {

        mockChatClientStream(geminiChatClient, "안녕", "하세요");

        mockMvc.perform(get("/api/chat")
                        .param("question", "안녕하세요")
                        .param("model", "gemini"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("local 모델로 질문하면 SSE 스트림을 반환한다")
    void chat_WithLocal_ReturnsStream() throws Exception {

        mockChatClientStream(localChatClient, "안녕", "하세요");

        mockMvc.perform(get("/api/chat")
                        .param("question", "안녕하세요")
                        .param("model", "local"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }

    @Test
    @DisplayName("model 파라미터가 없으면 기본값 ollama를 사용한다")
    void chat_WithoutModel_UsesOllamaDefault() throws Exception {

        mockChatClientStream(ollamaChatClient, "응답");

        mockMvc.perform(get("/api/chat")
                        .param("question", "안녕하세요"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM));
    }
}