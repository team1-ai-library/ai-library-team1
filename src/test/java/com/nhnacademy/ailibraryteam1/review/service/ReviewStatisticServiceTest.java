package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewStatisticServiceTest {

    @Mock
    private ReviewStatisticRepository reviewStatisticRepository;

    @InjectMocks
    private ReviewStatisticService reviewStatisticService;

    @Test
    @DisplayName("존재하지 않는 리뷰 통계를 조회하려고 하면, 예외가 발생한다.")
    void getStatistic_WhenNonExistent_ThrowsException() {
        // given
        long bookId = 1L;

        given(reviewStatisticRepository.findById(bookId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewStatisticService.getStatistic(bookId))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.STATISTIC_NOT_FOUND, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("리뷰 통계를 조회하면, 해당 통계를 반환한다.")
    void getStatistic_WhenExists_ReturnsStatistic() {
        // given
        long bookId = 1L;
        BookReviewStatistic statistic = mock(BookReviewStatistic.class);

        given(reviewStatisticRepository.findById(bookId))
                .willReturn(Optional.of(statistic));

        // when
        BookReviewStatistic result = reviewStatisticService.getStatistic(bookId);

        // then
        assertThat(result)
                .isEqualTo(statistic);
    }
}
