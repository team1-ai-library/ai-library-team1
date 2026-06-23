package com.nhnacademy.ailibraryteam1.review.repository;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<BookReview, Long> {
    List<BookReview> findAllByBook_Id(Long bookId);

    List<BookReview> findAllByIdGreaterThan(Long cursorId);
}
