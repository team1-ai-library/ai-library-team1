package com.nhnacademy.ailibraryteam1.feedback.dto;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

public record TelegramCallbackInfo(
        long chatId,
        String callbackQueryId,
        long bookId,
        FeedbackType type
) {
    public static TelegramCallbackInfo from(Update update) {
        if (update == null || !update.hasCallbackQuery()) {
            throw new BusinessException(ErrorCode.CALLBACK_DATA_INVALID);
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        String[] parts = callbackData.split(":");
        if (parts.length != 3 || !Objects.equals("fb", parts[0])) {
            throw new BusinessException(ErrorCode.CALLBACK_DATA_INVALID);
        }

        long bookId = Long.parseLong(parts[1]);
        String callbackQueryId = callbackQuery.getId();
        FeedbackType type = FeedbackType.valueOf(parts[2]);

        return new TelegramCallbackInfo(chatId, callbackQueryId, bookId, type);
    }
}
