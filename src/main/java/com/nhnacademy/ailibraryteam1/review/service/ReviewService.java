package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;

import java.util.List;

public interface ReviewService {

    BookReview register(BookReview review);
    List<BookReview> getAllBookReviews(long bookId);
    List<BookReview> getCursorNextReviews(long cursorId);
}
