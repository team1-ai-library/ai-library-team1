package com.nhnacademy.ailibraryteam1.feedback.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramBookSearchResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramMessageInfo;
import com.nhnacademy.ailibraryteam1.feedback.service.QueryCacheService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class TelegramBookSearchUseCase {
    private final QueryCacheService queryCacheService;
    private final BookRagService bookRagService;
    private final BookService bookService;

    public List<TelegramBookSearchResult> search(TelegramMessageInfo info) {
        queryCacheService.putRecentQuery(info.chatId(), info.text());

        // 텔레그램 대화 저장, maxMessage 10건
        String conversationId = "telegram-%s".formatted(info.chatId());
        List<BookAiRecommendationResponse> result = bookRagService.recommendBooks(info.text(), "ollama", conversationId);

        List<Long> bookIds = result.stream()
                .map(BookAiRecommendationResponse::id)
                .toList();

        Map<Long, Book> bookMap = bookService.getBooks(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        return result.stream()
                .map(aiResponse -> TelegramBookSearchResult.of(bookMap.get(aiResponse.id()), aiResponse))
                .toList();
    }
}
