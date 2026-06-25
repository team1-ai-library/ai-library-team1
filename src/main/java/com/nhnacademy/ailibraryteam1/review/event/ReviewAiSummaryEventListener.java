package com.nhnacademy.ailibraryteam1.review.event;

import com.nhnacademy.ailibraryteam1.review.usecase.ReviewSummarizeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewAiSummaryEventListener {
    private final ReviewSummarizeUseCase reviewSummarizeUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReviewCreated(ReviewCreatedEvent event) {
        reviewSummarizeUseCase.execute(event.bookId());
    }
}