package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.cache.CachedEmbeddingService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookVectorSearchUseCaseTest {

    @Mock
    private BookQuerydslRepository bookQuerydslRepository;

    @Mock
    private CachedEmbeddingService cachedEmbeddingService;

    private BookVectorSearchUseCase bookVectorSearchUseCase;

    @BeforeEach
    void setUp() {
        bookVectorSearchUseCase = new BookVectorSearchUseCase(bookQuerydslRepository, cachedEmbeddingService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void parameterNullTest(String invalidKeyword) {
       assertThatThrownBy(() -> bookVectorSearchUseCase.searchByVector(invalidKeyword, PageRequest.of(0, 10)))
               .isInstanceOf(BusinessException.class);
    }

    @Test
    void commonReturnTest() {
        String keyword = "스피링 입문서";
        Pageable pageable = PageRequest.of(0, 10);

        float[] queryVector = new float[] {
                0.1f, 0.2f, 0.3f, 0.4f, 0.5f
        };
        Page<BookSearchResponse> expectedPage = new PageImpl<>(List.of());

        given(cachedEmbeddingService.embed(keyword)).willReturn(queryVector);
        given(bookQuerydslRepository.searchByVector(queryVector, pageable)).willReturn(expectedPage);

        Page<BookSearchResponse> result = bookVectorSearchUseCase.searchByVector(keyword, pageable);

        assertThat(result).isEqualTo(expectedPage);
    }

}