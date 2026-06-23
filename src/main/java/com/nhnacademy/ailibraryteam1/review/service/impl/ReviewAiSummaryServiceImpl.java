package com.nhnacademy.ailibraryteam1.review.service.impl;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import com.nhnacademy.ailibraryteam1.review.service.ReviewAiSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewAiSummaryServiceImpl implements ReviewAiSummaryService {
    private final ReviewAiSummaryRepository reviewAiSummaryRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookReviewAiSummary register(BookReviewAiSummary summary) {
        return reviewAiSummaryRepository.save(summary);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookReviewAiSummary> findSummary(long bookId) {
        return reviewAiSummaryRepository.findById(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewAiSummary getSummary(long bookId) {
        return reviewAiSummaryRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));
    }

    @Override
    @Transactional
    public BookReviewAiSummary startGenerating(BookReviewAiSummary summary) {
        summary.startGenerating();
        return reviewAiSummaryRepository.save(summary);
    }

    @Override
    @Transactional
    public void cancelGenerating(BookReviewAiSummary summary) {
        summary.stopGenerating();
        reviewAiSummaryRepository.save(summary);
    }

    @Override
    @Transactional
    public void complete(BookReviewAiSummary summary) {
        reviewAiSummaryRepository.save(summary);
    }
}
