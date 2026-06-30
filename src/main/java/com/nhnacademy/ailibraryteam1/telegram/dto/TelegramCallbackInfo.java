package com.nhnacademy.ailibraryteam1.telegram.dto;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

/**
 * 텔레그램 피드백 콜백 데이터
 * @param chatId 텔레그램 채팅 ID (사용자 ID)
 * @param messageId 사용자 검색 메시지 ID (검색어 캐시 매핑용)
 * @param callbackMessageId 텔레그램 콜백 메시지 ID (피드백 팝업 처리용)
 * @param callbackQueryId 텔레그램 콜백 쿼리 ID (피드백 팝업 처리용)
 * @param bookId 피드백 대상 도서 ID
 * @param type 피드백 타입 (GOOD, BAD)
 */
public record TelegramCallbackInfo(
        long chatId,
        long messageId,
        long callbackMessageId,
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
        long callbackMessageId = callbackQuery.getMessage().getMessageId();

        String[] parts = callbackData.split(":");
        if (parts.length != 4 || !Objects.equals("fb", parts[0])) {
            throw new BusinessException(ErrorCode.CALLBACK_DATA_INVALID);
        }

        long messageId = Long.parseLong(parts[1]);
        long bookId = Long.parseLong(parts[2]);
        String callbackQueryId = callbackQuery.getId();
        FeedbackType type = FeedbackType.valueOf(parts[3]);

        return new TelegramCallbackInfo(chatId, messageId, callbackMessageId, callbackQueryId, bookId, type);
    }
}
