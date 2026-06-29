package com.nhnacademy.ailibraryteam1.feedback.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class StartCommandHandler implements TelegramCommandHandler {
    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update, String argument) {
        String welcome = """
            안녕하세요! AI 도서관 추천 봇입니다.
            
            원하는 도서 주제나 질문을 입력하시면 AI가 관련 도서를 찾아서 추천해 드립니다.
            
            사용 방법:
            1. '/search [검색어]' 명령어를 사용하세요.
            2. 도서 아래 👍/👎 버튼으로 피드백을 남기면 개인화 추천이 시작됩니다!
            """;

        return List.of(SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(welcome)
                .build());
    }
}
