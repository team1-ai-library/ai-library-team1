package com.nhnacademy.ailibraryteam1.feedback.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QueryCacheService {
    private final Map<Long, String> recentQueries = new ConcurrentHashMap<>();

    public void putRecentQuery(long chatId, String query) {
        recentQueries.put(chatId, query);
    }

    public String getRecentQuery(long chatId) {
        if (!recentQueries.containsKey(chatId)) {
            return "";
        }

        return recentQueries.get(chatId);
    }
}
