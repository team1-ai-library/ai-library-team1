package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;

import java.util.Optional;

public interface ReviewAiSummaryService {
    BookReviewAiSummary register(BookReviewAiSummary summary);
    Optional<BookReviewAiSummary> findSummary(long bookId);
    BookReviewAiSummary getSummary(long bookId);
    BookReviewAiSummary startGenerating(BookReviewAiSummary summary);
    void cancelGenerating(BookReviewAiSummary summary);
    void complete(BookReviewAiSummary summary);
}
