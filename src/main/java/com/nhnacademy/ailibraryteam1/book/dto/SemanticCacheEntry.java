package com.nhnacademy.ailibraryteam1.book.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시맨틱 캐시 항목
 * 질문 임베딩 벡터와 RAG 결과를 함께 보관
 */
public record SemanticCacheEntry(
        String query, // 원본 질문 (로깅용)
        float[] embedding, // 질문 임베딩 벡터 (유사도 비교용)
        List<BookAiRecommendationResponse> result,
        LocalDateTime createdAt // 생성 시각(TTL 판단용)
) {}