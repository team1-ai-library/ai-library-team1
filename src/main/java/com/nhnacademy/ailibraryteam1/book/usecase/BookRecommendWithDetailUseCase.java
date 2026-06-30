package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookRecommendationResult;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * AI 추천(RAG)을 받아 도서 상세 정보(제목, 저자, 출판사, 이미지)와 결합
 * BookRecommendWithDetailUseCase는 id, relevance, why만 갖고 있으므로, 내용이 부족함
 * 한 번의 도서 조회로 N+1 문제 없이 묶어서 리턴
 */
@UseCase
@RequiredArgsConstructor
public class BookRecommendWithDetailUseCase {

    private final BookRagService bookRagService;
    private final BookService bookService;

    public List<BookRecommendationResult> execute(String question, String model, String conversationId) {

        List<BookAiRecommendationResponse> aiResults = this.bookRagService.recommendBooks(question, model, conversationId);

        if (aiResults.isEmpty()) {
            return List.of();
        }

        // AI 추천 결과에서 도서 ID만 추출
        List<Long> ids = aiResults.stream()
                .map(BookAiRecommendationResponse::id)
                .toList();

        // ID로 도서 정보 한 번에 조회
        List<Book> books = this.bookService.getBooks(ids);

        // 맵으로 변환 (key는 도서 ID)
        Map<Long, Book> bookMap = new HashMap<>();
        for (Book book : books) {
            bookMap.put(book.getId(), book);
        }

        // AI 추천 결과 + 도서 상세 정보 합치기
        List<BookRecommendationResult> result = new ArrayList<>();

        for (BookAiRecommendationResponse aiResult : aiResults) {
            Book book = bookMap.get(aiResult.id());

            String title = Objects.nonNull(book) ? book.getTitle() : "알 수 없음";
            String authorName = Objects.nonNull(book) ? book.getAuthorName() : "";
            String publisherName = Objects.nonNull(book) ? book.getPublisherName() : "";
            String imageUrl = Objects.nonNull(book) ? book.getImageUrl() : null;

            result.add(new BookRecommendationResult(
                    aiResult.id(),
                    title,
                    authorName,
                    publisherName,
                    imageUrl,
                    aiResult.relevance(),
                    aiResult.why()
            ));
        }

        return result;
    }
}