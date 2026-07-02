package com.nhnacademy.ailibraryteam1.controller.rest;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookDetailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookDetailController {

    private final BookDetailUseCase bookDetailUseCase;

    @GetMapping("/api/books/{id}")
    public ResponseEntity<BookSearchResponse> bookDetail(@PathVariable Long id) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookDetailUseCase.getBookDetail(id));
    }
}