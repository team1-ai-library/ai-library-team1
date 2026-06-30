package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.rabbitmq.event.ReviewCreatedEvent;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
public class ReviewCreateUseCase {
    private final BookService bookService;
    private final ReviewService reviewService;
    private final ReviewStatisticService reviewStatisticService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(long bookId, ReviewCreateRequest request) {
        // 도서 정보 조회
        Book book = bookService.getBook(bookId);

        // 리뷰 등록
        BookReview savedReview = reviewService.register(request.toEntity(book));

        // 통계 조회 (이제 모든 도서에 기본 통계 컬럼이 존재함)
        BookReviewStatistic statistic = reviewStatisticService.getStatistic(bookId);

        // 통계 데이터 갱신
        statistic.addReview(savedReview);

        // 리뷰 생성 이벤트 발행
        eventPublisher.publishEvent(new ReviewCreatedEvent(bookId));
    }
}
