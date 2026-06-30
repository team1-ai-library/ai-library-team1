package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RRF 알고리즘으로 키워드 검색 결과와 벡터 검색 결과를 하나로 합치는 서비스
 *
 * RRF 공식: score = weight  * (1 / (K + rank))
 * - rank: 각 검색 결과 리스트에서의 순위 (0부터 시작하니까 +1 해서 사용함)
 * - K: 순위 차이의 영향을 완만하게 만드는 상수 (값이 클수록 1~2위와 10위의 점수 차이가 작아짐)
 * - weight: 검색 방식별 가중치 (키워드 검색과 벡터 검색의 신뢰도가 다를 때 비중 조절)
 *
 * 같은 도서가 키워드/벡터 양쪽에서 모두 검색되면 두 점수를 더해서 최종 점수가 더 높아짐
 * -> 두 검색 방식 모두에서 상위권인 도서가 가장 신뢰도 높은 결과로 취급됨
 */
@Service
public class RrfService {

    // K 값이 클수록 순위에 따른 점수 차이가 완만해짐
    private static final int RRF_K = 60;

    /**
     * 키워드 검색 결과와 벡터 검색 결과(이미 관련도순으로 정렬되어 있다고 가정)를 RRF 점수 기준으로 병합 및 정렬
     * RRF 점수 내림차순으로 정렬된 도서 목록 (rrfScore 필드에 점수 포함)
     */
    public List<BookSearchResponse> fuse(List<BookSearchResponse> keywordResults,
                                         List<BookSearchResponse> vectorResults,
                                         double keywordWeight,
                                         double vectorWeight) {

        // key: 도서 ID
        // value: 도서 ID별 누적 RRF 점수
        // 키워드, 벡터 양쪽에 모두 등장한 도서는 점수가 두 번 더해짐
        Map<Long, Double> rrfScores = new HashMap<>();

        // key: 도서 ID
        // value: 도서 ID별 상세 정보
        // 최종 응답에 쓸 원본 데이터 보관용
        Map<Long, BookSearchResponse> bookMap = new HashMap<>();

        // 1. 키워드 검색 결과 점수 계산
        for (int i = 0; i < keywordResults.size(); i++) {
            BookSearchResponse book = keywordResults.get(i);
            double score = keywordWeight * (1.0 / (RRF_K + i + 1));

            // 같은 도서가 이미 있으면 점수 누적, 없으면 새로 추가
            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + score);

            // 도서 정보는 최초 1번만 저장 (중복 저장 방지)
            bookMap.putIfAbsent(book.id(), book);
        }

        // 2. 벡터 검색 결과 점수 계산
        for (int i = 0; i < vectorResults.size(); i++) {
            BookSearchResponse book = vectorResults.get(i);
            double score = vectorWeight * (1.0 / (RRF_K + i + 1));

            rrfScores.put(book.id(), rrfScores.getOrDefault(book.id(), 0.0) + score);

            if (!bookMap.containsKey(book.id())) {
                // 키워드 검색에 없던 도서라면 -> 벡터 검색 결과를 그대로 저장
                bookMap.put(book.id(), book);
            } else {
                // 키워드 검색에 이미 있던 도서라면 -> 벡터 검색에서만 계산되는 similarity(코사인 유사도) 값을 채워 넣어줌
                // (키워드 검색 결과에는 similarity가 널이니까)
                bookMap.computeIfPresent(book.id(), (k, existing) -> new BookSearchResponse(
                        existing.id(), existing.isbn(), existing.title(), existing.volumeTitle(),
                        existing.authorName(), existing.publisherName(), existing.price(),
                        existing.editionPublishDate(), existing.bookContent(), existing.imageUrl(),
                        book.similarity(), null, existing.averageRating(), existing.reviewCount(), existing.reviewSummary()
                ));
            }
        }

        // 3. 누적된 RRF 점수 기준으로 내림차순 정렬하면서 최종 DTO 생성
        return rrfScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 점수 높은 도서가 먼저 오도록 정렬 (내림차순)
                .map(entry -> {
                    Long id = entry.getKey();
                    BookSearchResponse original = bookMap.get(id);
                    Double rrfScore = entry.getValue();

                    // 계산된 rrfScore를 채워서 새 DTO로 리턴 (원본은 rrfScore가 널이였음)
                    return new BookSearchResponse(
                            original.id(), original.isbn(), original.title(), original.volumeTitle(),
                            original.authorName(), original.publisherName(), original.price(),
                            original.editionPublishDate(), original.bookContent(), original.imageUrl(),
                            original.similarity(), rrfScore, original.averageRating(), original.reviewCount(), original.reviewSummary()
                    );
                })
                .toList();
    }
}