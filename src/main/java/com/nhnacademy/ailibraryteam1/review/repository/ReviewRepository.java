package com.nhnacademy.ailibraryteam1.review.repository;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<BookReview, Long> {
}
