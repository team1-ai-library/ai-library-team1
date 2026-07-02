package com.nhnacademy.ailibraryteam1.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(false); // null 값 캐시 안함, 검색결과가 없는 것은 cache 할 필요가 없음

        // 텔레그램 최근 검색어 세션 캐시
        cacheManager.registerCustomCache("recentQueries",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .maximumSize(1000)
                        .build());

        // 개인화 선호도 벡터 캐시
        cacheManager.registerCustomCache("userPreferenceVectors",
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(1000)
                        .build());

        // EmbeddingModel.embed(text) 캐시
        cacheManager.registerCustomCache("embeddings",
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(5000)
                        .build()
        );

        return cacheManager;
    }
}