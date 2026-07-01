package com.nhnacademy.ailibraryteam1.telegram.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
public class LibraryCommandHandler implements TelegramCommandHandler {
    private final ChatClient chatClient;
    
    public LibraryCommandHandler(@Qualifier("ollamaChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    @Override
    public String getCommand() {
        return "/library";
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update, String argument) {
        Long chatId = update.getMessage().getChatId();

        String conversationId = String.format("telegram-%s", chatId);

        if (argument == null || argument.isBlank()) {
            return List.of(SendMessage.builder()
                    .chatId(chatId)
                    .text("질문을 입력해 주세요. (예: '/library 도서관 이용 시간을 알려줘')")
                    .build());
        }

        log.info("[텔레그램 도서나루 챗봇 호출]: {}", argument);

        String apiResponse = chatClient.prompt()
                .user(argument)
                .advisors(advisorSpec -> advisorSpec.param(
                        ChatMemory.CONVERSATION_ID, conversationId
                ))
                .call()
                .content();

        log.info("[텔레그램 도서나루 챗봇 응답]: {}", apiResponse);
        
        if (apiResponse == null || apiResponse.isBlank()) {
            return List.of(SendMessage.builder()
                    .chatId(chatId)
                    .text("답변 생성 중 오류가 발생했습니다. 다시 시도해주세요.")
                    .build());
        }
        
        return List.of(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(apiResponse)
                .build());
    }
}
