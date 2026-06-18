package com.nhnacademy.ailibraryteam1.book.controller;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookSearchController {
    private final BookService bookService;

    @GetMapping("/books/search")
    public ResponseEntity<Page<BookSearchResponse>> search(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String keyword,
            @RequestParam String searchType,
            Pageable pageable
    ) {
        // TODO: searchType으로 분기 (VECTOR, HYBRID, RAG 추가 시)

        Page<BookSearchResponse> result = bookService.searchByKeyword(isbn, keyword, pageable);

        return ResponseEntity.ok(result);
    }
}