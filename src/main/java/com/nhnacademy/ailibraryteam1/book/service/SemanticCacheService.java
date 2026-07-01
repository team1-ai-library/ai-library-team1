package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.SemanticCacheEntry;
import com.nhnacademy.ailibraryteam1.common.util.CalculateCosineSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 시맨틱 캐시 서비스
 * <p>
 * 질문 임베딩 벡터 간 코사인 유사도로 캐시 히트 여부를 판단
 * 완전히 동일한 질문은 유사도 1.0으로 즉시 히트
 * 유사한 질문은 임계값(0.98) 이상이면 히트
 * <p>
 * ConcurrentHashMap으로 스레드 세이프티하게 관리
 */
@Service
@Slf4j
public class SemanticCacheService {

    // 유사도 임계값 (0.98 이상이면 같은 질문으로 판단)
    private static final double SIMILARITY_THRESHOLD = 0.98;

    // 캐시 저장소 (key: 캐시 ID, value: 캐시 항목)
    private final ConcurrentHashMap<Long, SemanticCacheEntry> cache = new ConcurrentHashMap<>();

    // 캐시 ID 생성기
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * 캐시 조회
     * 새 질문 임베딩과 기존 캐시 항목들의 임베딩을 코사인 유사도로 비교
     *
     * @param queryEmbedding 새 질문의 임베딩 벡터
     * @return 캐시 히트 시 JSON 결과, 미스 시 empty
     */
    public Optional<List<BookAiRecommendationResponse>> get(float[] queryEmbedding) {

        List<BookAiRecommendationResponse> bestResult = null;
        double bestSimilarity = 0.0;

        for (SemanticCacheEntry entry : this.cache.values()) {
            double similarity = CalculateCosineSimilarity.cosineSimilarity(queryEmbedding, entry.embedding());

            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestResult = entry.result();
            }
        }

        if (bestSimilarity >= SIMILARITY_THRESHOLD) {
            log.info("[SemanticCacheService] 캐시 히트 - 유사도: {}", String.format("%.4f", bestSimilarity));
            return Optional.of(bestResult);
        }

        log.info("[SemanticCacheService] 캐시 미스 - 최고 유사도: {}", String.format("%.4f", bestSimilarity));
        return Optional.empty();
    }

    // 캐시 저장
    public void putToCache(String query, float[] embedding, List<BookAiRecommendationResponse> results) {
        long id = this.idGenerator.incrementAndGet();
        this.cache.put(id, new SemanticCacheEntry(query, embedding, results));
        log.info("[SemanticCacheService] 캐시 저장 - query: {}, 현재 캐시 수: {}", query, this.cache.size());
    }
}