package com.nhnacademy.ailibraryteam1.feedback.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramBookSearchResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramMessageInfo;
import com.nhnacademy.ailibraryteam1.feedback.service.FeedbackService;
import com.nhnacademy.ailibraryteam1.feedback.service.PersonalizationService;
import com.nhnacademy.ailibraryteam1.feedback.service.QueryCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
public class TelegramBookSearchUseCase {
    private final QueryCacheService queryCacheService;
    private final BookHybridSearchUseCase hybridSearchUseCase; // 일단 기능 구현을 위해 유스케이스 주입
    private final PersonalizationService personalizationService;
    private final FeedbackService feedbackService;
    private final BookRagService bookRagService;
    private final BookService bookService;

    private static final int RETRIEVAL_K = 100;
    private static final int RERANK_K = 5;
    private static final double PERSONALIZATION_WEIGHT = 0.02;
    private static final double GLOBAL_FEEDBACK_WEIGHT = 0.01;

    public List<TelegramBookSearchResult> search(TelegramMessageInfo info) {
        queryCacheService.putRecentQuery(info.chatId(), info.text());

        String conversationId = "telegram-%s".formatted(info.chatId());

        // 1. 하이브리드 검색 수행 (100개 추출)
        List<BookSearchResponse> candidates = hybridSearchUseCase
                .searchByHybrid(info.text(), PageRequest.of(0, RETRIEVAL_K))
                .getContent();

        if (candidates.isEmpty()) {
            return List.of();
        }

        List<Long> candidateIds = candidates.stream().map(BookSearchResponse::id).toList();

        // 2. 글로벌 선호도 점수와 텔레그램 사용자 취향 선호도 점수 가져오기
        Map<Long, Double> personalizationScores = personalizationService.getPersonalizationScores(info.chatId(), candidateIds);
        Map<Long, Double> globalScore = feedbackService.getGlobalFeedbackScores(candidateIds);

        // 3. RRF 점수 + 개인화 점수로 리랭킹 후 최종 Top-5 선정
        List<BookSearchResponse> topKBooks = candidates.stream()
                .filter(book -> Objects.nonNull(book.rrfScore()))
                .sorted(Comparator.comparingDouble((BookSearchResponse book) -> {
                    double similarity = personalizationScores.getOrDefault(book.id(), 0.0);
                    double globalPriority = globalScore.getOrDefault(book.id(), 0.0);
                    return book.rrfScore() + (similarity * PERSONALIZATION_WEIGHT) + (globalPriority * GLOBAL_FEEDBACK_WEIGHT);
                }).reversed())
                .limit(RERANK_K)
                .toList();

        // 4. RAG 서비스 호출 (리랭킹된 도서 전달)
        List<BookAiRecommendationResponse> result = bookRagService.recommendBooksWithCandidates(info.text(), "ollama", topKBooks, conversationId);

        List<Long> bookIds = result.stream()
                .map(BookAiRecommendationResponse::id)
                .toList();

        // 5. 도서 정보 조회
        Map<Long, Book> bookMap = bookService.getBooks(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, book -> book));

        return result.stream()
                .map(aiResponse -> {
                    Book book = bookMap.get(aiResponse.id());
                    Double similarity = personalizationScores.get(aiResponse.id());
                    return TelegramBookSearchResult.of(book, aiResponse, similarity);
                })
                .toList();
    }
}
