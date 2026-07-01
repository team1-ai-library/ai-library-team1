package com.nhnacademy.ailibraryteam1.common.cache;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 임베딩 캐시 서비스
 * 동일한 텍스트에 대한 임베딩 계산을 캐싱하여 중복 호출 방지
 * Caffeine 캐시 "embeddings" 사용 (TTL 24시간, 최대 5000개)
 */
@Component
public class CachedEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public CachedEmbeddingService(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * #text
     * -> Spring Cache의 SpEL 표현식
     * -> 메서드 파라미터 이름을 참조 (파라미터 이름이 text)
     * -> embed("자바")를 한 후, 또 embed("자바") 하면 "자바" 키가 이미 있으니 캐시 히트
     */
    @Cacheable(value = "embeddings", key = "#text")
    public float[] embed(String text) {
        return this.embeddingModel.embed(text);
    }
}