package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰를 등록하면, 저장된 리뷰를 반환한다")
    void register_SavesAndReturnsReview() {
        // given
        BookReview review = mock(BookReview.class);

        given(reviewRepository.save(review))
                .willReturn(review);

        // when
        BookReview result = reviewService.register(review);

        // then
        assertThat(result)
                .isEqualTo(review);
    }

    @Test
    @DisplayName("도서 ID로 모든 리뷰를 조회한다")
    void getAllBookReviews_ReturnsReviewList() {
        // given
        long bookId = 1L;
        List<BookReview> reviews = List.of(mock(BookReview.class));

        given(reviewRepository.findAllByBook_Id(bookId))
                .willReturn(reviews);

        // when
        List<BookReview> result = reviewService.getAllBookReviews(bookId);

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .isEqualTo(reviews);
    }

    @Test
    @DisplayName("커서 기반으로 다음 리뷰들을 조회한다")
    void getCursorNextReviews_ReturnsNextReviews() {
        // given
        long bookId = 1L;
        long cursorId = 10L;
        List<BookReview> reviews = List.of(mock(BookReview.class));

        given(reviewRepository.findAllByBook_IdAndIdGreaterThan(bookId, cursorId))
                .willReturn(reviews);

        // when
        List<BookReview> result = reviewService.getCursorNextReviews(bookId, cursorId);

        // then
        assertThat(result)
                .isNotEmpty()
                .hasSize(1)
                .isEqualTo(reviews);
    }
}
