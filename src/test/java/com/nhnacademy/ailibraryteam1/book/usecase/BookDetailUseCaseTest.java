package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BookDetailUseCaseTest {

    @Mock
    private BookService bookService;

    @Mock
    private ReviewAiSummaryRepository reviewAiSummaryRepository;

    @Mock
    private ReviewStatisticRepository reviewStatisticRepository;

    @InjectMocks
    private BookDetailUseCase bookDetailUseCase;

    @Test
    @DisplayName("리뷰 통계와 AI 요약이 모두 있으면 전체 정보를 반환한다")
    void getBookDetailWhenAllDataExistsReturnFullResponseTest() {

        Book book = mock(Book.class);
        BookReviewStatistic bookReviewStatistic = mock(BookReviewStatistic.class);
        BookReviewAiSummary bookReviewAiSummary = mock(BookReviewAiSummary.class);

        given(this.bookService.getBook(1L)).willReturn(book);
        given(this.reviewStatisticRepository.findByBookId(1L)).willReturn(Optional.of(bookReviewStatistic));
        given(this.reviewAiSummaryRepository.findByBookId(1L)).willReturn(Optional.of(bookReviewAiSummary));
        given(bookReviewStatistic.getAverageRating()).willReturn(new BigDecimal("4.5"));
        given(bookReviewStatistic.getReviewCount()).willReturn(10L);
        given(bookReviewAiSummary.getReviewSummary()).willReturn("좋은 책입니다.");

        BookSearchResponse result = this.bookDetailUseCase.getBookDetail(1L);

        assertThat(result.averageRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(result.reviewCount()).isEqualTo(10L);
        assertThat(result.reviewSummary()).isEqualTo("좋은 책입니다.");
    }

    @Test
    @DisplayName("리뷰 통계가 없으면 평점과 리뷰 수가 널이다")
    void getBookDetailWhenNoStatisticReturnsNullRatingTest() {

        Book book = mock(Book.class);

        given(this.bookService.getBook(1L)).willReturn(book);
        given(this.reviewStatisticRepository.findByBookId(1L)).willReturn(Optional.empty());
        given(this.reviewAiSummaryRepository.findByBookId(1L)).willReturn(Optional.empty());

        BookSearchResponse result = this.bookDetailUseCase.getBookDetail(1L);

        assertThat(result.averageRating()).isNull();
        assertThat(result.reviewCount()).isNull();
        assertThat(result.reviewSummary()).isNull();
    }

    @Test
    @DisplayName("AI 요약이 없으면 reviewSummary가 널이다")
    void getBookDetailWhenNoAiSummaryReturnNullSummaryTest() {

        Book book = mock(Book.class);
        BookReviewStatistic statistic = mock(BookReviewStatistic.class);

        given(this.bookService.getBook(1L)).willReturn(book);
        given(this.reviewStatisticRepository.findByBookId(1L)).willReturn(Optional.of(statistic));
        given(this.reviewAiSummaryRepository.findByBookId(1L)).willReturn(Optional.empty());
        given(statistic.getAverageRating()).willReturn(new BigDecimal("4.0"));
        given(statistic.getReviewCount()).willReturn(5L);

        BookSearchResponse result = this.bookDetailUseCase.getBookDetail(1L);

        assertThat(result.averageRating()).isEqualByComparingTo(new BigDecimal("4.0"));
        assertThat(result.reviewCount()).isEqualTo(5L);
        assertThat(result.reviewSummary()).isNull();
    }

    @Test
    @DisplayName("similarity와 rrfScore는 항상 널이다")
    void getBookDetailSimilarityAndRrfScoreAlwaysNullTest() {

        Book book = mock(Book.class);

        given(this.bookService.getBook(1L)).willReturn(book);
        given(this.reviewStatisticRepository.findByBookId(1L)).willReturn(Optional.empty());
        given(this.reviewStatisticRepository.findByBookId(1L)).willReturn(Optional.empty());

        BookSearchResponse result = this.bookDetailUseCase.getBookDetail(1L);

        assertThat(result.similarity()).isNull();
        assertThat(result.rrfScore()).isNull();
    }
}