package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final EmbeddingModel embeddingModel;
    private final RrfService rrfService;
    private final Executor hybridSearchExecutor;

    // 키워드 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());
        boolean keywordExists = (Objects.nonNull(keyword) && !keyword.isBlank());

        if (!isbnExists && !keywordExists) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        return this.bookRepository.searchByKeyword(isbn, keyword, pageable);
    }

    // 벡터 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByVector(String keyword, Pageable pageable) {

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // 검색어를 벡터로 변환
        float[] queryVector = this.embeddingModel.embed(keyword);

        return this.bookRepository.searchByVector(queryVector, pageable);
    }

    // 하이브리드 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByHybrid(String keyword, Pageable pageable) {

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        Pageable largePage = PageRequest.of(0, 100); // 100개만

        CompletableFuture<List<BookSearchResponse>> keywordSearchFuture = CompletableFuture.supplyAsync(() -> {
            Page<BookSearchResponse> keywordPage = searchByKeyword(null, keyword, largePage);

            return (keywordPage != null && keywordPage.hasContent())
                    ? keywordPage.getContent()
                    : List.of();
        }, hybridSearchExecutor);

        CompletableFuture<List<BookSearchResponse>> vectorSearchFuture = CompletableFuture.supplyAsync(() -> {
            Page<BookSearchResponse> vectorPage = searchByVector(keyword, largePage);

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

    public Book getBook(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "존재하지 않는 도서: " + id));
    }
}