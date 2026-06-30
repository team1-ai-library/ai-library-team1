package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ReviewStatisticService {
    private final ReviewStatisticRepository reviewStatisticRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public BookReviewStatistic getStatistic(long bookId) {
        return reviewStatisticRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATISTIC_NOT_FOUND));
    }
}
