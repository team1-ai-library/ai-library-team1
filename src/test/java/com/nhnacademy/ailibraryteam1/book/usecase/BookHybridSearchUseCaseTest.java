package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.book.service.RrfService;
import com.nhnacademy.ailibraryteam1.common.cache.CachedEmbeddingService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookHybridSearchUseCaseTest {

    @Mock
    private BookQuerydslRepository bookQuerydslRepository;

    @Mock
    private RrfService rrfService;

    @Mock
    private CachedEmbeddingService cachedEmbeddingService;

    // 비동기 처리를 테스트에서는 동기로 실행하기 위해 직접 실행 Executor 사용
    private final Executor syncExecutor = Runnable::run;

    private BookHybridSearchUseCase bookHybridSearchUseCase;

    @BeforeEach
    void setUp() {
        this.bookHybridSearchUseCase = new BookHybridSearchUseCase(
                this.bookQuerydslRepository,
                this.rrfService,
                this.syncExecutor,
                this.cachedEmbeddingService
        );
    }

    private BookSearchResponse book(long id) {
        return new BookSearchResponse(
                id, null, "제목" + id, null, null, null,
                null, null, null, null,
                null, 0.05, null, null, null
        );
    }

    @Test
    @DisplayName("키워드가 빈 널이면 예외를 던진다")
    void searchWhenKeywordIsNullThrowsExceptionTest() {
        assertThatThrownBy(() -> this.bookHybridSearchUseCase.searchByHybrid(null, PageRequest.of(0, 10)))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.SEARCH_CONDITION_REQUIRED, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("키워드가 빈 문자열이면 예외를 던진다")
    void searchWhenKeywordIsBlankThrowsExceptionTest() {
        assertThatThrownBy(() -> this.bookHybridSearchUseCase.searchByHybrid("  ", PageRequest.of(0, 10)))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.SEARCH_CONDITION_REQUIRED, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("일반 하이브리드 검색이 정상적으로 동작한다")
    void searchByHybridReturnsPagedResultTest() {

        List<BookSearchResponse> keywordResult = List.of(this.book(1L), this.book(2L));
        List<BookSearchResponse> vectorResult = List.of(this.book(1L), this.book(3L));
        List<BookSearchResponse> fusedResult = List.of(this.book(1L), this.book(2L), this.book(3L));

        given(this.bookQuerydslRepository.searchByKeyword(isNull(), any(), any()))
                .willReturn(new PageImpl<>(keywordResult));
        given(this.cachedEmbeddingService.embed(any()))
                .willReturn(new float[]{0.1f});
        given(this.bookQuerydslRepository.searchByVector(any(), any()))
                .willReturn(new PageImpl<>(vectorResult));
        given(this.rrfService.fuse(keywordResult, vectorResult, 1.0, 1.0))
                .willReturn(fusedResult);

        Page<BookSearchResponse> result = this.bookHybridSearchUseCase.searchByHybrid("자바", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("RAG 전용 하이브리드 검색은 벡터 가중치(1.4)가 더 높다")
    void searchByHybridForRagUseHigherVectorWeightTest() {

        List<BookSearchResponse> keywordResult = List.of(this.book(1L));
        List<BookSearchResponse> vectorResult = List.of(this.book(2L));
        List<BookSearchResponse> fusedResult = List.of(this.book(2L), this.book(1L));

        given(this.bookQuerydslRepository.searchByKeyword(isNull(), any(), any()))
                .willReturn(new PageImpl<>(keywordResult));
        given(this.cachedEmbeddingService.embed(any()))
                .willReturn(new float[]{0.1f});
        given(this.bookQuerydslRepository.searchByVector(any(), any()))
                .willReturn(new PageImpl<>(vectorResult));
        given(this.rrfService.fuse(keywordResult, vectorResult, 0.6, 1.4))
                .willReturn(fusedResult);

        Page<BookSearchResponse> result = this.bookHybridSearchUseCase.searchByHybridForRag("파이썬 입문 책 추천해줘", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().getFirst().id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("pageable로 결과를 잘라서 반환한다")
    void returnPageableResultCutTest() {

        List<BookSearchResponse> fusedResult = List.of(this.book(1L), this.book(2L), this.book(3L), this.book(4L), this.book(5L));

        given(this.bookQuerydslRepository.searchByKeyword(isNull(), any(), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(this.cachedEmbeddingService.embed(any()))
                .willReturn(new float[]{0.1f});
        given(this.bookQuerydslRepository.searchByVector(any(), any()))
                .willReturn(new PageImpl<>(List.of()));
        given(this.rrfService.fuse(any(), any(), any(Double.class), any(Double.class)))
                .willReturn(fusedResult);

        Page<BookSearchResponse> result = this.bookHybridSearchUseCase.searchByHybrid("자바", PageRequest.of(0, 2));

        Assertions.assertThat(result.getContent()).hasSize(2);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(5);
    }
}