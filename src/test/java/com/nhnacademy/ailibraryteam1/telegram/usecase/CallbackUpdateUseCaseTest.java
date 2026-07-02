package com.nhnacademy.ailibraryteam1.telegram.usecase;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.service.FeedbackService;
import com.nhnacademy.ailibraryteam1.telegram.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.telegram.dto.TelegramCallbackInfo;
import com.nhnacademy.ailibraryteam1.telegram.service.QueryCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CallbackUpdateUseCaseTest {

    @Mock
    private QueryCacheService queryCacheService;

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private CallbackUpdateUseCase callbackUpdateUseCase;

    @Test
    @DisplayName("피드백 세션이 존재하고 정상적인 요청인 경우, 피드백을 저장하고 성공 결과를 반환한다.")
    void handleCallback_WhenSessionValid_SavesFeedbackAndReturnsSuccess() {
        // given
        TelegramCallbackInfo info = new TelegramCallbackInfo(1L, 123L, 456L, "queryId", 1L, FeedbackType.GOOD);
        given(queryCacheService.getRecentQuery(info.chatId(), info.messageId()))
                .willReturn("이 책 추천해줘");

        // when
        CallbackResult result = callbackUpdateUseCase.handleCallback(info);

        // then
        assertThat(result.callbackQueryId())
                .isEqualTo("queryId");
        assertThat(result.success())
                .isTrue();
        assertThat(result.message())
                .isEqualTo("피드백이 저장되었습니다!");
        then(feedbackService).should()
                .registerOrUpdateFeedback(any(Feedback.class));
    }

    @Test
    @DisplayName("피드백 세션이 만료된 경우(캐시에 쿼리 없음), 실패 결과를 반환한다.")
    void handleCallback_WhenSessionExpired_ReturnsFailure() {
        // given
        TelegramCallbackInfo info = new TelegramCallbackInfo(1L, 123L, 456L, "test", 1L, FeedbackType.GOOD);
        given(queryCacheService.getRecentQuery(info.chatId(), info.messageId()))
                .willReturn(null);

        // when
        CallbackResult result = callbackUpdateUseCase.handleCallback(info);

        // then
        assertThat(result.success())
                .isFalse();
        assertThat(result.message())
                .isEqualTo("피드백 세션이 만료되었습니다.");
        then(feedbackService).should(never())
                .registerOrUpdateFeedback(any());
    }

    @Test
    @DisplayName("이미 피드백을 남긴 도서인 경우, 중복 안내 메시지를 반환한다.")
    void handleCallback_WhenFeedbackDuplicated_ReturnsDuplicatedMessage() {
        // given
        TelegramCallbackInfo info = new TelegramCallbackInfo(1L, 123L, 456L, "test", 1L, FeedbackType.GOOD);
        given(queryCacheService.getRecentQuery(info.chatId(), info.messageId()))
                .willReturn("검색 쿼리");
        doThrow(new BusinessException(ErrorCode.FEEDBACK_DUPLICATED))
                .when(feedbackService).registerOrUpdateFeedback(any(Feedback.class));

        // when
        CallbackResult result = callbackUpdateUseCase.handleCallback(info);

        // then
        assertThat(result.success())
                .isFalse();
        assertThat(result.message())
                .isEqualTo("이미 피드백을 남겼습니다.");
    }
}
