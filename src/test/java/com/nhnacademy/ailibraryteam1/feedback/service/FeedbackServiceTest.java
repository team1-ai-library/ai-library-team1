package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.dto.BookFeedbackCount;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackQueryRepository;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackQueryRepository feedbackQueryRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    @DisplayName("기존 피드백이 없으면 새로운 피드백을 저장한다")
    void registerOrUpdateFeedback_WhenNew_SavesFeedback() {
        // given
        Feedback feedback = Feedback.create(1L, 1L, "query", FeedbackType.GOOD);

        given(feedbackRepository.findByChatIdAndBookIdAndQuery(feedback.getChatId(), feedback.getBookId(), feedback.getQuery()))
                .willReturn(Optional.empty());

        // when
        feedbackService.registerOrUpdateFeedback(feedback);

        // then
        then(feedbackRepository)
                .should()
                .save(feedback);
    }

    @Test
    @DisplayName("동일한 타입의 피드백이 이미 존재하면 예외를 던진다")
    void registerOrUpdateFeedback_WhenDuplicated_ThrowsException() {
        // given
        Feedback feedback = Feedback.create(1L, 1L, "query", FeedbackType.GOOD);
        Feedback existingFeedback = Feedback.create(1L, 1L, "query", FeedbackType.GOOD);

        given(feedbackRepository.findByChatIdAndBookIdAndQuery(feedback.getChatId(), feedback.getBookId(), feedback.getQuery()))
                .willReturn(Optional.of(existingFeedback));

        // when & then
        assertThatThrownBy(() -> feedbackService.registerOrUpdateFeedback(feedback))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.FEEDBACK_DUPLICATED, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("다른 타입의 피드백이 이미 존재하면 타입을 업데이트한다")
    void registerOrUpdateFeedback_WhenDifferentType_UpdatesFeedback() {
        // given
        Feedback feedback = Feedback.create(1L, 1L, "query", FeedbackType.GOOD);
        Feedback existingFeedback = Feedback.create(1L, 1L, "query", FeedbackType.BAD);

        given(feedbackRepository.findByChatIdAndBookIdAndQuery(1L, 1L, "query"))
                .willReturn(Optional.of(existingFeedback));

        // when
        assertDoesNotThrow(() -> feedbackService.registerOrUpdateFeedback(feedback));

        // then
        then(feedbackRepository)
                .should(never())
                .save(feedback);
    }

    @Test
    @DisplayName("피드백 존재 여부를 확인한다")
    void hasExistingFeedback_ReturnsBoolean() {
        // given
        given(feedbackRepository.existsByChatIdAndBookIdAndQuery(1L, 1L, "query"))
                .willReturn(true);

        // when
        boolean result = feedbackService.hasExistingFeedback(1L, 1L, "query");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("도서 목록에 대한 글로벌 피드백 점수를 계산한다")
    void getGlobalFeedbackScores_CalculatesRatios() {
        // given
        List<Long> bookIds = List.of(1L, 2L, 3L);
        
        // 1번 도서: 피드백 4개 (GOOD 3, BAD 1) -> (3-1)/4 = 0.5
        // 2번 도서: 피드백 2개 (임계치 3 미만) -> 0.0
        // 3번 도서: 피드백 없음 -> 0.0
        BookFeedbackCount count1 = new BookFeedbackCount(1L, 3L, 4L);
        BookFeedbackCount count2 = new BookFeedbackCount(2L, 2L, 2L);

        given(feedbackQueryRepository.findBookFeedbackCount(bookIds))
                .willReturn(List.of(count1, count2));

        // when
        Map<Long, Double> scores = feedbackService.getGlobalFeedbackScores(bookIds);

        // then
        assertThat(scores)
                .hasSize(3)
                .containsEntry(1L, 0.5)
                .containsEntry(2L, 0.0)
                .containsEntry(3L, 0.0);
    }

    @Test
    @DisplayName("도서 목록이 비어있으면 빈 맵을 반환한다")
    void getGlobalFeedbackScores_WhenEmptyInput_ReturnsEmptyMap() {
        // when
        Map<Long, Double> scores = feedbackService.getGlobalFeedbackScores(List.of());

        // then
        assertThat(scores)
                .isEmpty();
    }
}
