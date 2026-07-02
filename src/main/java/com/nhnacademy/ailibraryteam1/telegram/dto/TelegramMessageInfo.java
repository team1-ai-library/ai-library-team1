package com.nhnacademy.ailibraryteam1.telegram.dto;

import org.telegram.telegrambots.meta.api.objects.Update;

public record TelegramMessageInfo(
        long chatId,
        long messageId,
        String text
) {
    public static TelegramMessageInfo from(Update update, String query) {
        long chatId = update.getMessage().getChatId();
        long messageId = update.getMessage().getMessageId();

        return new TelegramMessageInfo(chatId, messageId, query);
    }
}
