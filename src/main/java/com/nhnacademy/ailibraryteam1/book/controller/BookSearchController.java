package com.nhnacademy.ailibraryteam1.book.controller;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.search.SearchType;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookKeywordSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookVectorSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookSearchController {

    private final BookKeywordSearchUseCase bookKeywordSearchUseCase;
    private final BookVectorSearchUseCase bookVectorSearchUseCase;
    private final BookHybridSearchUseCase bookHybridSearchUseCase;

    @GetMapping("/books/search")
    public ResponseEntity<Page<BookSearchResponse>> search(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String keyword,
            @RequestParam String searchType,
            Pageable pageable
    ) {

        SearchType type = SearchType.from(searchType);

        Page<BookSearchResponse> result = switch (type) {
            case KEYWORD -> this.bookKeywordSearchUseCase.searchByKeyword(isbn, keyword, pageable);
            case VECTOR -> this.bookVectorSearchUseCase.searchByVector(keyword, pageable);
            case HYBRID -> this.bookHybridSearchUseCase.searchByHybrid(keyword, pageable);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SEARCH_TYPE);
        };

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }
}