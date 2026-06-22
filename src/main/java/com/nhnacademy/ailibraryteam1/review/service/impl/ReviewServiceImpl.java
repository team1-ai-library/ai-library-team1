package com.nhnacademy.ailibraryteam1.review.service.impl;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewRepository;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public BookReview register(BookReview review) {
        return reviewRepository.save(review);
    }
}
