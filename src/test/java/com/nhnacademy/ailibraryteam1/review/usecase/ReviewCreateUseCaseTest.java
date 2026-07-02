package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.rabbitmq.event.ReviewCreatedEvent;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
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
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReviewCreateUseCaseTest {

    @Mock
    private BookService bookService;

    @Mock
    private ReviewService reviewService;

    @Mock
    private ReviewStatisticService reviewStatisticService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReviewCreateUseCase reviewCreateUseCase;

    @Test
    @DisplayName("리뷰가 생성되면 리뷰 통계 데이터가 업데이트되고 리뷰 생성 이벤트가 발행된다.")
    void execute_WhenCreated_StatisticsUpdateAndEventPublished() {
        // given
        long bookId = 1L;
        Book book = mock(Book.class);
        BookReview review = mock(BookReview.class);
        BookReviewStatistic statistic = mock(BookReviewStatistic.class);
        ReviewCreateRequest request = new ReviewCreateRequest("test", 5);

        given(bookService.getBook(bookId))
                .willReturn(book);
        given(reviewService.register(any(BookReview.class)))
                .willReturn(review);
        given(reviewStatisticService.getStatistic(bookId))
                .willReturn(statistic);

        // when
        reviewCreateUseCase.execute(bookId, request);

        // then
        then(statistic)
                .should()
                .addReview(review);
        then(eventPublisher)
                .should()
                .publishEvent(any(ReviewCreatedEvent.class));
    }

}