package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.event.ReviewEmbeddingEvent;
import com.nhnacademy.ailibraryteam1.review.service.ReviewAiSummaryService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewSummarizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ReviewSummarizeUseCase {
    private final BookService bookService;
    private final ReviewService reviewService;
    private final ReviewAiSummaryService reviewAiSummaryService;
    private final ReviewSummarizer reviewSummarizer;
    private final ApplicationEventPublisher eventPublisher;

    public void execute(long bookId) {
        Book book = bookService.getBook(bookId);

        // 이제 도서마다 기본 AI 요약 컬럼 존재
        BookReviewAiSummary summary = reviewAiSummaryService.getSummary(bookId);

        // 이미 요약을 생성 중이라면 리턴
        if (summary.isGenerating()) {
            log.info("이미 리뷰 요약이 생성 중입니다.");
            return;
        }

        // 요약 생성 시작
        try {
            summary = reviewAiSummaryService.startGenerating(summary);
            log.info("리뷰 요약 생성을 시작합니다.");
        } catch (OptimisticLockingFailureException e) {
            return;
        }

        List<BookReview> reviews;
        String aiSummaryResult;

        try {
            // 이미 존재하는 리뷰가 있다면
            if (summary.getLastReviewId() != null) {
                // 최신 리뷰 조회
                reviews = reviewService.getCursorNextReviews(bookId, summary.getLastReviewId());

                // 리뷰가 충분하지 않다면 취소
                if (reviews.isEmpty() || reviews.size() < 5) {
                    log.info("리뷰가 충분하지 않습니다. 리뷰 요약 생성을 취소합니다.");
                    reviewAiSummaryService.cancelGenerating(summary);
                    return;
                }

                // 이전 리뷰 요약 + 새로운 리뷰 목록을 함께 전달
                String prevSummary = summary.getReviewSummary();

                aiSummaryResult = reviewSummarizer.updateReviewSummary(prevSummary, reviews);

                summary.updateSummary(reviews.getLast().getId(), aiSummaryResult);
            } else {
                // 새로 생성된 리뷰라면

                // 모든 리뷰 목록 조회
                reviews = reviewService.getAllBookReviews(bookId);

                // 리뷰가 충분하지 않으면 취소
                if (reviews.isEmpty() || reviews.size() < 5) {
                    log.info("리뷰가 충분하지 않습니다. 리뷰 요약 생성을 취소합니다.");
                    reviewAiSummaryService.cancelGenerating(summary);
                    return;
                }

                aiSummaryResult = reviewSummarizer.generateNewReviewSummary(book.getTitle(), reviews);

                summary.updateSummary(reviews.getLast().getId(), aiSummaryResult);
            }

            // 생성 완료
            reviewAiSummaryService.complete(summary);

            log.info("리뷰 요약이 생성되었습니다.");

            eventPublisher.publishEvent(ReviewEmbeddingEvent.create(book, summary.getReviewSummary()));
        } catch (Exception e) {
            log.info("리뷰 요약 생성 중 예외가 발생했습니다. 리뷰 요약 생성을 취소합니다.", e);
            reviewAiSummaryService.cancelGenerating(summary);
            throw e;
        }
    }
}
