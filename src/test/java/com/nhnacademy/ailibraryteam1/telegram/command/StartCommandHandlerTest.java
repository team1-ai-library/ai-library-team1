package com.nhnacademy.ailibraryteam1.telegram.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StartCommandHandlerTest {

    private final StartCommandHandler handler = new StartCommandHandler();

    @Test
    @DisplayName("getCommand()는 /start를 반환한다")
    void getCommand_returnsStart() {
        assertEquals("/start", handler.getCommand());
    }

    @Test
    @DisplayName("handle() 호출 시 환영 메시지가 담긴 SendMessage 1건을 반환한다")
    void handle_returnsSingleWelcomeMessage() {
        Update update = createTextMessageUpdate(1234L, "/start");

        List<PartialBotApiMethod<?>> responses = handler.handle(update, "");

        assertEquals(1, responses.size());
        assertInstanceOf(SendMessage.class, responses.getFirst());
    }

    @Test
    @DisplayName("응답 메시지의 chatId는 update의 chatId와 동일하다")
    void handle_setsChatIdFromUpdate() {
        Update update = createTextMessageUpdate(5678L, "/start");

        SendMessage message = (SendMessage) handler.handle(update, "").getFirst();

        assertEquals("5678", message.getChatId());
    }

    @Test
    @DisplayName("응답 메시지 본문에는 사용법 안내가 포함된다")
    void handle_messageContainsUsageGuide() {
        Update update = createTextMessageUpdate(1L, "/start");

        SendMessage message = (SendMessage) handler.handle(update, "").getFirst();

        assertTrue(message.getText().contains("AI 도서관 추천 봇"));
        assertTrue(message.getText().contains("/search"));
        assertTrue(message.getText().contains("👍"));
        assertTrue(message.getText().contains("👎"));
    }

    @Test
    @DisplayName("argument 값과 무관하게 동일한 환영 메시지를 반환한다")
    void handle_ignoresArgument() {
        Update update = createTextMessageUpdate(1L, "/start 아무개변수");

        SendMessage withArg = (SendMessage) handler.handle(update, "아무개변수").getFirst();
        SendMessage withoutArg = (SendMessage) handler.handle(update, "").getFirst();

        assertEquals(withoutArg.getText(), withArg.getText());
    }

    private Update createTextMessageUpdate(Long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(chatId);
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);
        return update;
    }
}