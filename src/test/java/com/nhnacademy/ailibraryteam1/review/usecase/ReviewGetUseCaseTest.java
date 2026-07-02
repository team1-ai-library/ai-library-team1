package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.review.dto.ReviewDetailResponse;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewStatisticService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewGetUseCaseTest {
    
    @Mock
    private ReviewService reviewService;
    
    @Mock
    private ReviewStatisticService reviewStatisticService;

    @InjectMocks
    private ReviewGetUseCase reviewGetUseCase;
    
    @Test
    @DisplayName("리뷰 목록을 조회하면, 리뷰 통계 정보와 리뷰 목록을 함께 반환한다.")
    void execute_WhenStatisticsExist_ReturnsFullDetail() {
        // given
        long bookId = 1L;
        BookReviewStatistic statistic = mock(BookReviewStatistic.class);

        given(statistic.getAverageRating())
                .willReturn(BigDecimal.valueOf(4.5));
        given(statistic.getReviewCount())
                .willReturn(10L);
        given(statistic.getRating5Count())
                .willReturn(5);

        BookReview review = mock(BookReview.class);

        given(review.getId())
                .willReturn(100L);
        given(review.getContent())
                .willReturn("좋아요");
        given(review.getRating())
                .willReturn(5);
        given(review.getCreatedAt())
                .willReturn(OffsetDateTime.now());

        given(reviewStatisticService.getStatistic(bookId))
                .willReturn(statistic);
        given(reviewService.getAllBookReviews(bookId))
                .willReturn(List.of(review));

        // when
        ReviewDetailResponse response = reviewGetUseCase.execute(bookId);

        // then
        assertThat(response.averageRating())
                .isEqualTo(BigDecimal.valueOf(4.5));
        assertThat(response.reviewCount())
                .isEqualTo(10L);
        assertThat(response.reviews())
                .hasSize(1);
        assertThat(response.reviews().getFirst().content())
                .isEqualTo("좋아요");
    }

    @Test
    @DisplayName("리뷰 통계 데이터가 없는 경우, 기본값과 리뷰 목록을 반환한다.")
    void execute_WhenStatisticsNotExist_ReturnsDefaultValuesAndReviews() {
        // given
        long bookId = 1L;
        given(reviewStatisticService.getStatistic(bookId))
                .willReturn(null);
        given(reviewService.getAllBookReviews(bookId))
                .willReturn(List.of());

        // when
        ReviewDetailResponse response = reviewGetUseCase.execute(bookId);

        // then
        assertThat(response.averageRating())
                .isNull();
        assertThat(response.reviewCount())
                .isZero();
        assertThat(response.reviews())
                .isEmpty();
    }
}
