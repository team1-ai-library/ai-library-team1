package com.nhnacademy.ailibraryteam1.telegram.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class HelpCommandHandler implements TelegramCommandHandler {

    @Override
    public String getCommand() {
        return "/help";
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update, String argument) {
        String helpText = """
            명령어 가이드
            
            /start : 봇 시작 및 소개
            /help : 도움말 확인
            /search [검색어] : 도서 추천 검색
            /library [검색어]: 도서나루 챗봇 (도서 검색, 도서 대출 여부...)
            """;

        return List.of(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(helpText)
                .build());
    }
}
