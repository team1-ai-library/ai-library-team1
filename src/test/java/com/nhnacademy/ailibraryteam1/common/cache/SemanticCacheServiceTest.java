package com.nhnacademy.ailibraryteam1.common.cache;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticCacheServiceTest {

    private SemanticCacheService semanticCacheService;

    private static final int EMBEDDING_DIMENSION = 1024;

    @BeforeEach
    void setUp() {
        semanticCacheService = new SemanticCacheService();
    }

    @Nested
    @DisplayName("캐시 조화")
    class Get {

        @Test
        void emptyCache() {
            float[] embedding = generateVector(EMBEDDING_DIMENSION, 0.5f);

            Optional<List<BookAiRecommendationResponse>> result = semanticCacheService.get(embedding);
            assertThat(result).isEmpty();
        }

        @Test
        void similar() {
            float[] embeddingA = generateVector(EMBEDDING_DIMENSION, 0.5f);
            float[] embeddingB = generateVector(EMBEDDING_DIMENSION, 0.500001f);
            List<BookAiRecommendationResponse> resultA = List.of(sampleRecommendation("결과A"));
            List<BookAiRecommendationResponse> resultB = List.of(sampleRecommendation("결과B"));

            semanticCacheService.putToCache("질문A", embeddingA, resultA);
            semanticCacheService.putToCache("질문B", embeddingB, resultB);

            Optional<List<BookAiRecommendationResponse>> result = semanticCacheService.get(embeddingA);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(resultA);
        }}

    private BookAiRecommendationResponse sampleRecommendation(String title) {
        // 실제 BookAiRecommendationResponse의 생성자/필드 구조에 맞춰 조정 필요
        return new BookAiRecommendationResponse(1L, 70, "이유");
    }

    private float[] generateVector(int dimension, float baseValue) {
        float[] vector = new float[dimension];
        Arrays.fill(vector, baseValue);
        return vector;
    }
}