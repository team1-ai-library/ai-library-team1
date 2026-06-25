package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.event.ReviewCreatedEvent;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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

        // 통계 데이터 조회 또는 생성
        BookReviewStatistic statistic = reviewStatisticService.findStatistic(bookId)
                .orElseGet(() -> {
                    try {
                        BookReviewStatistic newStat = BookReviewStatistic.create(book);

                        return reviewStatisticService.register(newStat);
                    } catch (DataIntegrityViolationException e) {
                        // 동시성 충돌 발생 -> 다시 조회
                        return reviewStatisticService.getStatistic(bookId);
                    }
                });

        // 통계 데이터 갱신
        statistic.addReview(savedReview);

        // REQUIRES_NEW로 생성된 엔티티 -> 1차 캐시에 존재하지 않을 수 있으므로 명시적 저장
        reviewStatisticService.complete(statistic);

        // 리뷰 생성 이벤트 발행
        eventPublisher.publishEvent(new ReviewCreatedEvent(bookId));
    }
}
