package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.common.util.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BookRagService {

    // 100개 검색 후 5개만 AI에게
    private static final int RETRIEVAL_K = 100;
    private static final int RERANK_K = 5;
    private static final double RRF_THRESHOLD = 0.02;

    private final BookHybridSearchUseCase hybridSearchUseCase;
    private final ChatClient geminiChatClient;
    private final ChatClient ollamaChatClient;
    private final ChatClient localChatClient;

    public BookRagService(BookHybridSearchUseCase hybridSearchUseCase,
                          @Qualifier("geminiChatClient") ChatClient geminiChatClient,
                          @Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                          @Qualifier("localChatClient") ChatClient localChatClient) {

        this.hybridSearchUseCase = hybridSearchUseCase;
        this.geminiChatClient = geminiChatClient;
        this.ollamaChatClient = ollamaChatClient;
        this.localChatClient = localChatClient;
    }

    private ChatClient selectClient(String model) {

        return switch (model.toLowerCase()) {
            case "gemini" -> this.geminiChatClient;
            case "ollama" -> this.ollamaChatClient;
            case "local" -> this.localChatClient;
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_MODEL);
        };
    }

    public List<BookAiRecommendationResponse> recommendBooks(String question, String model, String conversationId) {

        log.info("[BookRagService] RAG 추천 시작 - 질문: {}, 모델: {}", question, model);

        // RETRIEVAL_K개 요청 -> 내부에서 이미 RRF 정렬된 상태로 옴
        Pageable pageable = PageRequest.of(0, RETRIEVAL_K);
        List<BookSearchResponse> books = hybridSearchUseCase
                .searchByHybridForRag(question, pageable)
                .getContent();

        log.info("[BookRagService] 하이브리드 검색 완료 - 검색된 도서 수: {}", books.size());

        if (books.isEmpty()) {
            log.warn("[BookRagService] 검색된 도서가 없습니다 - 질문: {}", question);
            return List.of();
        }

        // RRF 임계값 필터링 + 상위 RERANK_K개만
        List<BookSearchResponse> topKBooks = this.selectTopKBooks(books);
        log.info("[BookRagService] Top-K 선정 완료 - {}권 -> {}권", books.size(), topKBooks.size());

        return recommendBooksWithCandidates(question, model, topKBooks, conversationId);
    }

    // 리랭킹된 도서 목록을 외부에서 주입받기 위해 메서드 분리
    public List<BookAiRecommendationResponse> recommendBooksWithCandidates(String question, String model, List<BookSearchResponse> topKBooks, String conversationId) {

        if (topKBooks.isEmpty()) {
            log.warn("[BookRagService] 추천 대상 도서가 없습니다 - 질문: {}", question);
            return List.of();
        }

        ChatClient chatClient = this.selectClient(model);
        String context = this.buildContext(topKBooks);
        log.debug("[BookRagService] 컨텍스트 생성 완료 - 길이: {} 자", context.length());

        log.info("[BookRagService] AI 모델 호출 시작");
        try {
            List<BookAiRecommendationResponse> result = chatClient.prompt()
                    .system(PromptTemplate.DEFAULT_SYSTEM_MESSAGE + "\n" + PromptTemplate.RULE_MESSAGE)
                    .user(u -> u.text(PromptTemplate.USER_MESSAGE)
                            .param("question", question)
                            .param("context", context)
                    )
                    .advisors(advisorSpec -> advisorSpec.param(
                            ChatMemory.CONVERSATION_ID, conversationId
                    ))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });

            log.info("[BookRagService] AI 모델 호출 완료: \n{}", result);

            // relevance 내림차순 정렬
            return Objects.nonNull(result)
                    ? result.stream()
                      .sorted(Comparator.comparingInt(BookAiRecommendationResponse::relevance).reversed())
                      .toList()
                    : List.of();
        } catch (Exception e) {
            log.error("[BookRagService] AI 모델 호출 실패 - 모델: {}, 질문: {}, 원인: {}",
                    model, question, e.getMessage(), e);

            throw new BusinessException(ErrorCode.AI_MODEL_CALL_FAILED);
        }
    }

    // 하이브리드 검색 결과를 컨텍스트로 변환
    private String buildContext(List<BookSearchResponse> books) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < books.size(); i++) {

            BookSearchResponse book = books.get(i);

            sb.append("### 도서 ").append(i + 1).append("\n"); // 도서 번호
            sb.append("- ID: ").append(book.id()).append("\n");
            sb.append("- 제목: ").append(book.title()).append("\n");
            sb.append("- 저자: ").append(book.authorName()).append("\n");
            sb.append("- 출판사: ").append(book.publisherName()).append("\n");

            // 내용
            if (Objects.nonNull(book.bookContent())) {
                String content = book.bookContent();

                // 내용 길이 제한
                if (content.length() > 300) {
                    content = content.substring(0, 300) + "...";
                }

                sb.append("- 내용: ").append(content).append("\n");
            }

            // 평점 정보
            if (Objects.nonNull(book.averageRating()) && Objects.nonNull(book.reviewCount()) && book.reviewCount() > 0) {
                sb.append("- 평점: ")
                        .append(String.format("%.1f/5.0 (%d개 리뷰)", book.averageRating(), book.reviewCount()))
                        .append("\n");
            }

            // 리뷰 요약
            if (Objects.nonNull(book.reviewSummary()) && !book.reviewSummary().isBlank()) {
                String summary = book.reviewSummary().length() > 100
                        ? book.reviewSummary().substring(0, 100) + "..."
                        : book.reviewSummary();

                sb.append("- 리뷰 요약: ").append(summary).append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    private List<BookSearchResponse> selectTopKBooks(List<BookSearchResponse> books) {

        return books.stream()
                .filter(book -> Objects.nonNull(book.rrfScore()))
                .filter(book -> book.rrfScore() >= RRF_THRESHOLD)
                .limit(RERANK_K) // 이미 RRF 정렬된 상태니까 바로 자름
                .toList();
    }
}