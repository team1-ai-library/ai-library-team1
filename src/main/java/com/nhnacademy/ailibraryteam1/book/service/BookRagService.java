package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.common.util.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// 도서 Recommend
@Service
@Slf4j
public class BookRagService {

    // 만약 100권이 검출된다면 토큰이 고갈될 수 있음
    // 상위 10권 처럼 관련성 높은 것만 넣어야 함 (토큰도 생각하여)
    // 컨텍스트로 몇 개를 쓸 지 상수로 빼둠
    private static final int RAG_CONTEXT_SIZE = 10;

    private final BookHybridSearchUseCase hybridSearchUseCase;
    private final ChatClient geminiChatClient;
    private final ChatClient ollamaChatClient;

    public BookRagService(BookHybridSearchUseCase hybridSearchUseCase,
                          @Qualifier("geminiChatClient") ChatClient geminiChatClient,
                          @Qualifier("ollamaChatClient") ChatClient ollamaChatClient) {

        this.hybridSearchUseCase = hybridSearchUseCase;
        this.geminiChatClient = geminiChatClient;
        this.ollamaChatClient = ollamaChatClient;
    }

    private ChatClient selectClient(String model) {

        return switch (model.toLowerCase()) {
            case "gemini" -> this.geminiChatClient;
            case "ollama" -> this.ollamaChatClient;
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_MODEL);
        };
    }

    public List<BookAiRecommendationResponse> recommendBooks(String question, String model) {

        ChatClient chatClient = this.selectClient(model);

        log.info("[BookRagService] RAG 추천 시작 - 질문: {}, 모델: {}", question, model);

        log.info("[BookRagService] 질문: {}", question);

        Pageable pageable = PageRequest.of(0, RAG_CONTEXT_SIZE);

        List<BookSearchResponse> books = hybridSearchUseCase
                .searchByHybrid(question, pageable)
                .getContent();

        log.info("[BookRagService] 하이브리드 검색 완료 - 검색된 도서 수: {}", books.size());

        if (books.isEmpty()) {
            log.warn("[BookRagService] 검색된 도서가 없습니다 - 질문: {}", question);
            return List.of();
        }

        String context = this.buildContext(books);
        log.debug("[BookRagService] 컨텍스트 생성 완료 - 길이: {} 자", context.length());

        log.info("[BookRagService] AI 모델 호출 시작");
        try {
            List<BookAiRecommendationResponse> result = chatClient.prompt()
                    .system(PromptTemplate.DEFAULT_SYSTEM_MESSAGE + "\n" + PromptTemplate.RULE_MESSAGE)
                    .user(u -> u.text(PromptTemplate.USER_MESSAGE)
                            .param("question", question)
                            .param("context", context)
                    )
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

            // 유사도 점수
            if (Objects.nonNull(book.similarity())) {
                sb.append("- 유사도: ").append(book.getSimilarityPercent()).append("\n");
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
}