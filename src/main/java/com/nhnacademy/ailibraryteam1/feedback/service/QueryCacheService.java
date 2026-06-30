package com.nhnacademy.ailibraryteam1.feedback.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QueryCacheService {

    // messageId -> AUTO_INCREMENT이므로 chatId와 조합해서 캐시 키로 사용
    @CachePut(value = "recentQueries", key = "#chatId + ':' + #messageId")
    public String putRecentQuery(long chatId, long messageId, String query) {
        log.info("[텔레그램 쿼리 캐시 저장]: chatId: {}, messageId: {}", chatId, messageId);
        return query; // 반환값이 messageId를 키로 캐시에 저장됨
    }

    @Cacheable(value = "recentQueries", key = "#chatId + ':' + #messageId")
    public String getRecentQuery(long chatId, long messageId) {
        log.info("[텔레그램 쿼리 캐시 미스]: chatId: {}, messageId: {}", chatId, messageId);
        return ""; // 캐시 미스 시 빈 문자열 반환
    }
}
