package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    @Transactional
    public BookReview register(BookReview review) {
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<BookReview> getAllBookReviews(long bookId) {
        return reviewRepository.findAllByBook_Id(bookId);
    }

    @Transactional(readOnly = true)
    public List<BookReview> getCursorNextReviews(long bookId, long cursorId) {
        return reviewRepository.findAllByBook_IdAndIdGreaterThan(bookId, cursorId);
    }
}
