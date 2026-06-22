package com.nhnacademy.ailibraryteam1.review.service.impl;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
import com.nhnacademy.ailibraryteam1.review.service.ReviewStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewStatisticServiceImpl implements ReviewStatisticService {
    private final ReviewStatisticRepository reviewStatisticRepository;

    // 동시 삽입으로 인한 DataIntegrityViolationException 발생 가능 -> 트랜잭션 오염 방지를 위한 전파 속성 설정
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookReviewStatistic register(BookReviewStatistic statistic) {

        return reviewStatisticRepository.save(statistic);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookReviewStatistic> findStatistic(long bookId) {
        return reviewStatisticRepository.findById(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public BookReviewStatistic getStatistic(long bookId) {
        return reviewStatisticRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATISTIC_NOT_FOUND));
    }

    @Override
    @Transactional
    public void complete(BookReviewStatistic statistic) {
        reviewStatisticRepository.save(statistic);
    }
}
