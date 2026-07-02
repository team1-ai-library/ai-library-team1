package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewAiSummaryService {
    private final ReviewAiSummaryRepository reviewAiSummaryRepository;

    @Transactional(readOnly = true)
    public BookReviewAiSummary getSummary(long bookId) {
        return reviewAiSummaryRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));
    }

    @Transactional
    public BookReviewAiSummary startGenerating(BookReviewAiSummary summary) {
        summary.startGenerating();
        return reviewAiSummaryRepository.save(summary);
    }

    @Transactional
    public void cancelGenerating(BookReviewAiSummary summary) {
        summary.stopGenerating();
        reviewAiSummaryRepository.save(summary);
    }

    @Transactional
    public void complete(BookReviewAiSummary summary) {
        reviewAiSummaryRepository.save(summary);
    }
}
