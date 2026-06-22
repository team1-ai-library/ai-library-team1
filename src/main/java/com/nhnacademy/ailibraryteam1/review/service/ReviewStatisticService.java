package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;

import java.util.Optional;

public interface ReviewStatisticService {
    BookReviewStatistic register(BookReviewStatistic statistic);
    Optional<BookReviewStatistic> findStatistic(long bookId);
    BookReviewStatistic getStatistic(long bookId);
}
