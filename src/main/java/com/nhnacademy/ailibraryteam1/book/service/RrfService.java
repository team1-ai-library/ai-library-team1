package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RrfService {

    private static final int RRF_K = 60;

    public List<BookSearchResponse> fuse(List<BookSearchResponse> keywordResults,
                                         List<BookSearchResponse> vectorResults) {
        // 도서 ID별 누적 RRF 점수를 저장하는 맵
        Map<Long, Double> rrfScores = new HashMap<>();

        // 도서 ID별 상세 정보를 저장하는 맵 (최종 반환용)
        Map<Long, BookSearchResponse> bookMap = new HashMap<>();

        // 1. 키워드 검색 결과 처리 및 점수 계산
        for (int i = 0; i < keywordResults.size(); i++) {
            BookSearchResponse book = keywordResults.get(i);
            // 1 / (k + rank) 점수 누적
            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + 1.0 / (RRF_K + i + 1));
            bookMap.putIfAbsent(book.id(), book);
        }

        // 2. 벡터 검색 결과 처리 및 점수 계산
        for (int i = 0; i < vectorResults.size(); i++) {
            BookSearchResponse book = vectorResults.get(i);

            // 1 / (k + rank) 점수 누적
            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + 1.0 / (RRF_K + i + 1));
            if (!bookMap.containsKey(book.id())) {
                bookMap.put(book.id(), book);
            } else {
                // 키워드 결과에 이미 존재하는 경우, 벡터 검색에서 추출된 유사도(Similarity) 정보를 보존하여 업데이트합니다.
                bookMap.computeIfPresent(book.id(), (k, existing) -> new BookSearchResponse(
                        existing.id(), existing.isbn(), existing.title(), existing.volumeTitle(),
                        existing.authorName(), existing.publisherName(), existing.price(),
                        existing.editionPublishDate(), existing.bookContent(), existing.imageUrl(),
                        book.similarity(), null
                ));
            }
        }

        // 3. 점수 기준 정렬 및 최종 응답 DTO 생성
        return rrfScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 점수 내림차순 정렬
                .map(entry -> {
                    Long id = entry.getKey();
                    BookSearchResponse original = bookMap.get(id);
                    Double rrfScore = entry.getValue();
                    // 최종 필드에 RRF 점수를 포함하여 반환
                    return new BookSearchResponse(
                            original.id(), original.isbn(), original.title(), original.volumeTitle(),
                            original.authorName(), original.publisherName(), original.price(),
                            original.editionPublishDate(), original.bookContent(), original.imageUrl(),
                            original.similarity(), rrfScore
                    );
                })
                .toList();
    }
}