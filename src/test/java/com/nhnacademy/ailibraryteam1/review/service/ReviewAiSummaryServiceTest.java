package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewAiSummaryServiceTest {

    @Mock
    private ReviewAiSummaryRepository reviewAiSummaryRepository;

    @InjectMocks
    private ReviewAiSummaryService reviewAiSummaryService;

    @Test
    @DisplayName("존재하지 않는 리뷰 요약을 조회하려 하면, BusinessException이 발생한다")
    void getSummary_WhenNonExistent_ThrowsException() {
        // given
        long bookId = 1L;

        given(reviewAiSummaryRepository.findById(bookId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewAiSummaryService.getSummary(bookId))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.SUMMARY_NOT_FOUND, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("리뷰 요약을 조회하면, 해당 요약을 반환한다")
    void getSummary_WhenExists_ReturnsSummary() {
        // given
        long bookId = 1L;
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);
        given(reviewAiSummaryRepository.findById(bookId)).willReturn(Optional.of(summary));

        // when
        BookReviewAiSummary result = reviewAiSummaryService.getSummary(bookId);

        // then
        assertThat(result).isEqualTo(summary);
    }

    @Test
    @DisplayName("요약 생성을 시작하면, 상태가 변경되고 저장된다")
    void startGenerating_UpdatesStatusAndSaves() {
        // given
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);

        // when
        reviewAiSummaryService.startGenerating(summary);

        // then
        then(summary)
                .should()
                .startGenerating();
        then(reviewAiSummaryRepository)
                .should()
                .save(summary);
    }

    @Test
    @DisplayName("요약 생성을 취소하면, 상태가 변경되고 저장된다")
    void cancelGenerating_UpdatesStatusAndSaves() {
        // given
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);

        // when
        reviewAiSummaryService.cancelGenerating(summary);

        // then
        then(summary)
                .should()
                .stopGenerating();
        then(reviewAiSummaryRepository)
                .should()
                .save(summary);
    }

    @Test
    @DisplayName("요약 완료 시, 엔티티를 저장한다")
    void complete_SavesSummary() {
        // given
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);

        // when
        reviewAiSummaryService.complete(summary);

        // then
        then(reviewAiSummaryRepository)
                .should()
                .save(summary);
    }
}
