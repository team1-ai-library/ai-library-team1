package com.nhnacademy.ailibraryteam1.telegram;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Component
public class TelegramInteractionLog {

    private static final int MAX_ENTRIES = 30;
    private static final int MAX_TEXT_LENGTH = 1000;

    private final Deque<Entry> entries = new ArrayDeque<>();

    public synchronized void record(Type type, String text) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.removeFirst();
        }
        entries.addLast(new Entry(type, truncate(text), Instant.now()));
    }

    private static String truncate(String text) {
        if (text == null || text.length() <= MAX_TEXT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_TEXT_LENGTH) + "…";
    }

    public synchronized List<Entry> recent() {
        return List.copyOf(entries);
    }

    public enum Type {
        REQUEST, RESPONSE
    }

    public record Entry(Type type, String text, Instant timestamp) {
    }
}