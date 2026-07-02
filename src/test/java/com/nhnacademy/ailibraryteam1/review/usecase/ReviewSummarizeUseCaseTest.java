package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.rabbitmq.event.ReviewEmbeddingEvent;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.service.ReviewAiSummaryService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewSummarizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReviewSummarizeUseCaseTest {

    @Mock
    private BookService bookService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewAiSummaryService reviewAiSummaryService;

    @Mock
    private ReviewSummarizer reviewSummarizer;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewSummarizeUseCase reviewSummarizeUseCase;

    @Test
    @DisplayName("이미 요약이 생성 중인 경우, 추가 작업을 진행하지 않고 종료한다.")
    void execute_WhenAlreadyGenerating_ReturnsEarly() {
        // given
        long bookId = 1L;
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);

        given(summary.isGenerating())
                .willReturn(true);
        given(reviewAiSummaryService.getSummary(bookId))
                .willReturn(summary);

        // when
        reviewSummarizeUseCase.execute(bookId);

        // then
        then(reviewAiSummaryService).should(never())
                .startGenerating(any());
        then(reviewSummarizer).should(never())
                .generateNewReviewSummary(anyString(), anyList());
    }

    @Test
    @DisplayName("새로운 도서의 리뷰가 5개 이상일 때, AI 요약을 새로 생성하고 이벤트를 발행한다.")
    void execute_WhenNewBookWithEnoughReviews_GeneratesSummary() {
        // given
        long bookId = 1L;
        Book book = mock(Book.class);
        String bookTitle = "Test Book";
        String newSummary = "New Summary";

        given(book.getTitle())
                .willReturn(bookTitle);

        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);
        List<BookReview> reviews = createMockReviews(5);

        given(bookService.getBook(bookId))
                .willReturn(book);
        given(reviewAiSummaryService.getSummary(bookId))
                .willReturn(summary);
        given(reviewAiSummaryService.startGenerating(summary))
                .willReturn(summary);
        given(summary.isGenerating())
                .willReturn(false);
        given(summary.getLastReviewId())
                .willReturn(null);
        given(reviewService.getAllBookReviews(bookId))
                .willReturn(reviews);
        given(reviewSummarizer.generateNewReviewSummary(bookTitle, reviews))
                .willReturn(newSummary);

        // when
        reviewSummarizeUseCase.execute(bookId);

        // then
        then(reviewAiSummaryService).should()
                .startGenerating(summary);
        then(summary)
                .should()
                .updateSummary(reviews.getLast().getId(), newSummary);
        then(reviewAiSummaryService)
                .should()
                .complete(summary);
        then(eventPublisher)
                .should()
                .publishEvent(any(ReviewEmbeddingEvent.class));
    }

    @Test
    @DisplayName("기존 요약이 있고 새로운 리뷰가 5개 이상일 때, 요약을 업데이트한다.")
    void execute_WhenUpdateWithEnoughReviews_UpdatesSummary() {
        // given
        long bookId = 1L;
        Book book = mock(Book.class);
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);
        String oldSummary = "Old Summary";
        String newSummary = "New Summary";
        List<BookReview> newReviews = createMockReviews(5);

        given(bookService.getBook(bookId))
                .willReturn(book);
        given(reviewAiSummaryService.getSummary(bookId))
                .willReturn(summary);
        given(reviewAiSummaryService.startGenerating(summary))
                .willReturn(summary);
        given(summary.getLastReviewId())
                .willReturn(100L);
        given(summary.getReviewSummary())
                .willReturn(oldSummary);
        given(reviewService.getCursorNextReviews(bookId, 100L))
                .willReturn(newReviews);
        given(reviewSummarizer.updateReviewSummary(oldSummary, newReviews))
                .willReturn(newSummary);

        // when
        reviewSummarizeUseCase.execute(bookId);

        // then
        then(summary).should().updateSummary(newReviews.getLast().getId(), newSummary);
        then(reviewAiSummaryService).should().complete(summary);
    }

    @Test
    @DisplayName("리뷰 개수가 5개 미만인 경우, 요약 생성을 취소한다.")
    void execute_WhenNotEnoughReviews_CancelsGenerating() {
        // given
        long bookId = 1L;
        BookReviewAiSummary summary = mock(BookReviewAiSummary.class);
        List<BookReview> reviews = createMockReviews(3);

        given(bookService.getBook(bookId))
                .willReturn(mock(Book.class));
        given(reviewAiSummaryService.getSummary(bookId))
                .willReturn(summary);
        given(reviewAiSummaryService.startGenerating(summary))
                .willReturn(summary);
        given(summary.getLastReviewId())
                .willReturn(null);
        given(reviewService.getAllBookReviews(bookId))
                .willReturn(reviews);

        // when
        reviewSummarizeUseCase.execute(bookId);

        // then
        then(reviewAiSummaryService).should()
                .cancelGenerating(summary);
        then(reviewSummarizer).should(never())
                .generateNewReviewSummary(anyString(), anyList());
    }

    private List<BookReview> createMockReviews(int count) {
        return LongStream.range(0, count)
                .mapToObj(i -> {
                    BookReview review = mock(BookReview.class);
                    lenient().when(review.getId()).thenReturn(i + 1);
                    return review;
                })
                .toList();
    }
}
