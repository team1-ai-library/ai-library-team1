package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.book.service.RrfService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
public class BookHybridSearchUseCase {

    private final BookRepository bookRepository;
    private final BookService bookService;
    private final RrfService rrfService;
    private final Executor hybridSearchExecutor;

    // 하이브리드 검색
    public Page<BookSearchResponse> searchByHybrid(String keyword, Pageable pageable) {

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        Pageable largePage = PageRequest.of(0, 100); // 100개만

        CompletableFuture<List<BookSearchResponse>> keywordSearchFuture = CompletableFuture.supplyAsync(() -> {
            Page<BookSearchResponse> keywordPage = this.bookService.searchByKeyword(null, keyword, largePage);

            return (keywordPage != null && keywordPage.hasContent())
                    ? keywordPage.getContent()
                    : List.of();
        }, hybridSearchExecutor);

        CompletableFuture<List<BookSearchResponse>> vectorSearchFuture = CompletableFuture.supplyAsync(() -> {
            Page<BookSearchResponse> vectorPage = this.bookService.searchByVector(keyword, largePage);

            return (vectorPage != null && vectorPage.hasContent())
                    ? vectorPage.getContent()
                    : List.of();
        }, hybridSearchExecutor);

        CompletableFuture<List<BookSearchResponse>> resultFuture = keywordSearchFuture.thenCombineAsync(
                vectorSearchFuture,
                rrfService::fuse,
                hybridSearchExecutor
        );

        List<BookSearchResponse> result = resultFuture.join();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());

        List<BookSearchResponse> content = new ArrayList<>();

        if (start < result.size()) {
            content = result.subList(start, end);
        }

        return new PageImpl<>(content, pageable, result.size());
    }
}