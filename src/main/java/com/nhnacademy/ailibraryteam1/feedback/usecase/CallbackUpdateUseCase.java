package com.nhnacademy.ailibraryteam1.feedback.usecase;

import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramCallbackInfo;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.service.FeedbackService;
import com.nhnacademy.ailibraryteam1.feedback.service.QueryCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CallbackUpdateUseCase {
    private final QueryCacheService queryCacheService;
    private final FeedbackService feedbackService;

    public CallbackResult handleCallback(TelegramCallbackInfo info) {
        log.info("[텔레그램 쿼리 캐시 조회]: chatId: {}, messageId: {}", info.chatId(), info.messageId());

        String query = queryCacheService.getRecentQuery(info.chatId(), info.messageId());

        // 피드백 세션이 만료된 경우
        if (query == null || query.isBlank()) {
            return new CallbackResult(info.callbackQueryId(), false, "피드백 세션이 만료되었습니다.", false);
        }

        Feedback feedback = Feedback.create(info.chatId(), info.bookId(), query, info.type());

        try {
            feedbackService.registerOrUpdateFeedback(feedback);
            return new CallbackResult(info.callbackQueryId(), true, "피드백이 저장되었습니다!", false);
        } catch (BusinessException e) {
            // 피드백 중복 등록 예외 시 처리
            if (e.getErrorCode() == ErrorCode.FEEDBACK_DUPLICATED) {
                return new CallbackResult(info.callbackQueryId(), false, "이미 피드백을 남겼습니다.", false);
            }

            throw e; // 피드백 중복 예외가 아니면 다시 던짐
        }
    }
}
