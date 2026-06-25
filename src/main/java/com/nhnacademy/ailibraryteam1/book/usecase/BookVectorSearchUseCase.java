package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
public class BookVectorSearchUseCase {

    private final BookQuerydslRepository bookQuerydslRepository;
    private final EmbeddingModel embeddingModel;

    public BookVectorSearchUseCase(BookQuerydslRepository bookQuerydslRepository,
                                   @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {

        this.bookQuerydslRepository = bookQuerydslRepository;
        this.embeddingModel = embeddingModel;
    }

    // 벡터 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByVector(String keyword, Pageable pageable) {

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // 검색어를 벡터로 변환
        float[] queryVector = this.embeddingModel.embed(keyword);

        return this.bookQuerydslRepository.searchByVector(queryVector, pageable);
    }
}