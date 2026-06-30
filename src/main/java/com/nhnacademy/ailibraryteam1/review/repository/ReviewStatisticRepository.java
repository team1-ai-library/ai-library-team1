package com.nhnacademy.ailibraryteam1.review.repository;

import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewStatisticRepository extends JpaRepository<BookReviewStatistic, Long> {

    Optional<BookReviewStatistic> findByBookId(Long bookId);
}