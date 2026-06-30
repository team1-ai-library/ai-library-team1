package com.nhnacademy.ailibraryteam1.feedback.usecase;

import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.feedback.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramCallbackInfo;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.service.FeedbackService;
import com.nhnacademy.ailibraryteam1.feedback.service.QueryCacheService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class CallbackUpdateUseCase {
    private final QueryCacheService queryCacheService;
    private final FeedbackService feedbackService;

    public CallbackResult handleCallback(TelegramCallbackInfo info) {
        String query = queryCacheService.getRecentQuery(info.chatId());

        // 피드백 세션이 만료된 경우
        if (query == null || query.isBlank()) {
            return new CallbackResult(info.callbackQueryId(), false, "피드백 세션이 만료되었습니다. 다시 검색해주세요.", false);
        }

        // 이미 피드백을 남긴 경우
        if (feedbackService.hasExistingFeedback(info.chatId(), info.bookId(), query)) {
            return new CallbackResult(info.callbackQueryId(), false, "이미 피드백을 남겼습니다.", false);
        }

        Feedback feedback = Feedback.create(info.chatId(), info.bookId(), query, info.type());

        feedbackService.registerFeedback(feedback);

        return new CallbackResult(info.callbackQueryId(), true, "피드백이 저장되었습니다!", false);
    }
}
