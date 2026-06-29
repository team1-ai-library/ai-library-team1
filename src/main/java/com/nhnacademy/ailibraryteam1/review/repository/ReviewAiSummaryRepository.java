package com.nhnacademy.ailibraryteam1.review.repository;

import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewAiSummaryRepository extends JpaRepository<BookReviewAiSummary, Long> {

    Optional<BookReviewAiSummary> findByBookId(Long id);
}