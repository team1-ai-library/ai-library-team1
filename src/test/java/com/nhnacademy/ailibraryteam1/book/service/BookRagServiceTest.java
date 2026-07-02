package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.cache.CachedEmbeddingService;
import com.nhnacademy.ailibraryteam1.common.cache.SemanticCacheService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BookRagServiceTest {

    @Mock
    private BookHybridSearchUseCase bookHybridSearchUseCase;

    @Mock
    private SemanticCacheService semanticCacheService;

    @Mock
    private CachedEmbeddingService cachedEmbeddingService;

    @Mock
    private ChatClient geminiChatClient;

    @Mock
    private ChatClient ollamaChatClient;

    @Mock
    private ChatClient localChatClient;

    @InjectMocks
    private BookRagService bookRagService;

    private BookSearchResponse bookWithRrfScore(long id, double rrfScore) {
        return new BookSearchResponse(
                id, null, "제목" + id, null, null, null,
                null, null, null, null,
                null, rrfScore, null, null, null
        );
    }

    private BookSearchResponse bookWithoutRrfScore(long id) {
        return new BookSearchResponse(
                id, null, "제목" + id, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );
    }

    @Test
    @DisplayName("캐시 히트 시 하이브리드 검색과 AI 호출을 하지 않는다")
    void recommendBooksWhenCacheHitReturnCacheTest() {

        float[] embedding = new float[]{0.1f, 0.2f};
        List<BookAiRecommendationResponse> cached = List.of(
                new BookAiRecommendationResponse(1L, 90, "좋은 책")
        );

        given(this.cachedEmbeddingService.embed("자바 책 추천")).willReturn(embedding);
        given(this.semanticCacheService.get(embedding)).willReturn(Optional.of(cached));

        List<BookAiRecommendationResponse> result = this.bookRagService.recommendBooks("자바 책 추천", "ollama", "conv-1");

        assertThat(result).isEqualTo(cached);
        then(this.bookHybridSearchUseCase).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("하이브리드 검색 결과가 없으면 빈 목록을 반환한다")
    void recommendBooksWhenNoSearchResultsReturnsEmptyListTest() {

        float[] embedding = new float[]{0.1f, 0.2f};

        given(this.cachedEmbeddingService.embed("자바 책 추천")).willReturn(embedding);
        given(this.semanticCacheService.get(embedding)).willReturn(Optional.empty());
        given(this.bookHybridSearchUseCase.searchByHybridForRag(any(), any())).willReturn(new PageImpl<>(List.of()));

        List<BookAiRecommendationResponse> result = this.bookRagService.recommendBooks("자바 책 추천", "ollama", "conv-1");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("RRF 임계값 이상인 도서가 없으면 빈 목록을 반환한다")
    void recommendBooksWhenNoBooksPassRrfThresholdReturnEmptyListTest() {

        float[] embedding = new float[]{0.1f, 0.2f};

        // RRF_THRESHOLD = 0.02 미만인 도서들
        List<BookSearchResponse> books = List.of(
                this.bookWithRrfScore(1L, 0.01),
                this.bookWithRrfScore(2L, 0.005)
        );

        given(this.cachedEmbeddingService.embed("자바 책 추천")).willReturn(embedding);
        given(this.semanticCacheService.get(embedding)).willReturn(Optional.empty());
        given(this.bookHybridSearchUseCase.searchByHybridForRag(any(), any())).willReturn(new PageImpl<>(books));

        List<BookAiRecommendationResponse> result = this.bookRagService.recommendBooks("자바 책 추천", "ollama", "conv-1");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("지원하지 않는 모델이면 예외를 던진다")
    void recommendBooksWithCandidatesWhenUnsupportedModelThrowsExceptionTest() {

        List<BookSearchResponse> topKBooks = List.of(this.bookWithRrfScore(1L, 0.05));

        assertThatThrownBy(() -> this.bookRagService.recommendBooksWithCandidates("질문", "unknown", topKBooks, "conv-1"))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.UNSUPPORTED_MODEL, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("추천 대상 도서가 비어있으면 빈 목록을 반환한다")
    void recommendBooksWithCandidatesWhenEmptyTopKBooksReturnEmptyListTest() {

        List<BookAiRecommendationResponse> result = this.bookRagService.recommendBooksWithCandidates("질문", "ollama", List.of(), "conv-1");

        assertThat(result).isEmpty();
        then(this.ollamaChatClient).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("rrfScore가 널인 도서는 Top-K에서 제외된다")
    void recommendBooksWhenRrfScoreIsNullExcludedFromTopKTest() {

        float[] embedding = new float[]{0.1f, 0.2f};
        List<BookSearchResponse> books = List.of(
                this.bookWithoutRrfScore(1L), // rrfScore 널 -> 제외
                this.bookWithoutRrfScore(2L)
        );

        given(this.cachedEmbeddingService.embed("자바 책 추천")).willReturn(embedding);
        given(this.semanticCacheService.get(embedding)).willReturn(Optional.empty());
        given(this.bookHybridSearchUseCase.searchByHybridForRag(any(), any())).willReturn(new PageImpl<>(books));

        List<BookAiRecommendationResponse> result = this.bookRagService.recommendBooks("자바 책 추천", "ollama", "conv-1");

        assertThat(result).isEmpty();
        then(this.geminiChatClient).shouldHaveNoInteractions();
    }
}