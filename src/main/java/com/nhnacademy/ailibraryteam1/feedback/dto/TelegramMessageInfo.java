package com.nhnacademy.ailibraryteam1.feedback.dto;

import org.telegram.telegrambots.meta.api.objects.Update;

public record TelegramMessageInfo(
        long chatId,
        long messageId,
        String text
) {
    public static TelegramMessageInfo from(Update update) {
        long chatId = update.getMessage().getChatId();
        long messageId = update.getMessage().getMessageId();
        String text = update.getMessage().getText();

        return new TelegramMessageInfo(chatId, messageId, text);
    }
}
