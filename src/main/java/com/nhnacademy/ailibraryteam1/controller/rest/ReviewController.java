package com.nhnacademy.ailibraryteam1.controller.rest;

import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewDetailResponse;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewCreateUseCase;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewGetUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{book-id}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewCreateUseCase reviewCreateUseCase;
    private final ReviewGetUseCase reviewGetUseCase;

    /**
     * 특정 도서의 리뷰 목록과 통계 조회
     */
    @GetMapping
    public ResponseEntity<ReviewDetailResponse> getReviews(@PathVariable("book-id") long bookId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.reviewGetUseCase.execute(bookId));
    }

    /**
     * 특정 도서에 리뷰를 작성
     */
    @PostMapping
    public ResponseEntity<Void> createReview(
            @PathVariable("book-id") long bookId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        reviewCreateUseCase.execute(bookId, request);

        return ResponseEntity.status(201).build();
    }
}