package com.nhnacademy.ailibraryteam1.common.cache;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.SemanticCacheEntry;
import com.nhnacademy.ailibraryteam1.common.util.CalculateCosineSimilarity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    private static final int TTL_MINUTES = 30; // 30분 후 만료
    private static final int MAX_CACHE_SIZE = 100; // 최대 100개

    // 캐시 저장소 (key: 캐시 ID, value: 캐시 항목)
    private final ConcurrentHashMap<Long, SemanticCacheEntry> cache = new ConcurrentHashMap<>();

    // 캐시 ID 생성기
    private final AtomicLong idGenerator = new AtomicLong(0);

    /**
     * 캐시 조회
     * 새 질문 임베딩과 기존 캐시 항목들의 임베딩을 코사인 유사도로 비교
     * TTL 만료 항목은 제외하고, 유사도가 임계값 이상인 결과를 리턴
     *
     * @param queryEmbedding 새 질문의 임베딩 벡터
     * @return 캐시 히트 시 JSON 결과, 미스 시 empty
     */
    public Optional<List<BookAiRecommendationResponse>> get(float[] queryEmbedding) {

        List<BookAiRecommendationResponse> bestResult = null;
        double bestSimilarity = 0.0;
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TTL_MINUTES);

        for (SemanticCacheEntry entry : this.cache.values()) {

            // TTL 만료 항목 스킵
            if (entry.createdAt().isBefore(threshold)) {
                continue;
            }

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

    /**
     * 캐시 저장
     * 저장 전 만료된 항목을 먼저 정리하고, 최대 크기 초과 시 가장 오래된 항목 제거
     */
    public void putToCache(String query, float[] embedding, List<BookAiRecommendationResponse> results) {

        // 만료된 항목 먼저 제거 (createdAt가 30분 전보다 이전이면 만료된 항목)
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TTL_MINUTES);
        this.cache.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(threshold));

        // 만료 항목 제거 후에도 100개 이상이면 가장 오래된 항목 하나 제거
        if (this.cache.size() >= MAX_CACHE_SIZE) {

            // 캐시의 모든 항목 중 createdAt가 가장 오래된 항목의 key를 찾음
            Long oldestKey = null;
            LocalDateTime oldestTime = LocalDateTime.now();

            for (Map.Entry<Long, SemanticCacheEntry> entry : this.cache.entrySet()) {
                if (entry.getValue().createdAt().isBefore(oldestTime)) {
                    oldestTime = entry.getValue().createdAt();
                    oldestKey = entry.getKey();
                }
            }

            if (Objects.nonNull(oldestKey)) {
                this.cache.remove(oldestKey);
                log.info("[SemanticCacheService] 캐시 최대 크기 초과 - 가장 오래된 캐시 제거");
            }
        }

        // 새 항목 저장
        long id = this.idGenerator.incrementAndGet();
        this.cache.put(id, new SemanticCacheEntry(query, embedding, results, LocalDateTime.now()));
        log.info("[SemanticCacheService] 캐시 저장 - query: {}, 현재 캐시 수: {}", query, this.cache.size());
    }
}