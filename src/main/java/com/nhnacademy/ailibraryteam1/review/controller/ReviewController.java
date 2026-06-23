package com.nhnacademy.ailibraryteam1.review.controller;

import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewCreateUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books/{book-id}/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewCreateUseCase reviewCreateUseCase;

    @PostMapping
    public ResponseEntity<Void> createReview(
            @PathVariable("book-id") long bookId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        reviewCreateUseCase.execute(bookId, request);

        return ResponseEntity.status(201).build();
    }
}
