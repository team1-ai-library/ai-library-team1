package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.common.cache.CachedEmbeddingService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@UseCase
@Slf4j
public class BookVectorSearchUseCase {

    private final BookQuerydslRepository bookQuerydslRepository;
    private final CachedEmbeddingService cachedEmbeddingService;

    public BookVectorSearchUseCase(BookQuerydslRepository bookQuerydslRepository,
                                   CachedEmbeddingService cachedEmbeddingService
                                   ) {

        this.bookQuerydslRepository = bookQuerydslRepository;
        this.cachedEmbeddingService = cachedEmbeddingService;
    }

    // 벡터 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByVector(String keyword, Pageable pageable) {

        log.info("[BookVectorSearchUseCase] 벡터 검색 - keyword: {}", keyword);

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // 검색어를 벡터로 변환
        float[] queryVector = this.cachedEmbeddingService.embed(keyword);

        Page<BookSearchResponse> result = this.bookQuerydslRepository.searchByVector(queryVector, pageable);

        log.info("[BookVectorSearchUseCase] 벡터 검색 완료 - 검색된 도서 수: {}", result.getTotalElements());

        return result;
    }
}