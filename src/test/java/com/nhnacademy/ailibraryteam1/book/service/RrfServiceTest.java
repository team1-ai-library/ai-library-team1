package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RrfServiceTest {

    private final RrfService rrfService = new RrfService();

    // 테스트용 BookSearchResponse 생성 헬퍼 (필드 너무 많으므로)
    private BookSearchResponse book(long id, Double similarity) {
        return new BookSearchResponse(
                id, null, "제목" + id, null, null, null,
                null, null, null, null,
                similarity, null, null, null, null
        );
    }

    @Test
    @DisplayName("키워드 결과만 있을 때 RRF 점수가 계산된다")
    void fuseWhenOnlyKeywordResultsCalculateRrfTest() {

        List<BookSearchResponse> keyword = List.of(this.book(1L, null), this.book(2L, null));
        List<BookSearchResponse> vector = List.of();

        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 1.0, 1.0);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(1L); // 1위가 더 높은 점수여야 함
        assertThat(result.getFirst().rrfScore()).isGreaterThan(result.get(1).rrfScore());
    }

    @Test
    @DisplayName("벡터 결과만 있을 때 RRF 점수가 계산된다")
    void fuseWhenOnlyVectorResultsCalculateRrfTest() {

        List<BookSearchResponse> keyword = List.of();
        List<BookSearchResponse> vector = List.of(this.book(1L, 0.9), this.book(2L, 0.8));

        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 1.0, 1.0);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().rrfScore()).isGreaterThan(result.get(1).rrfScore());
    }

    @Test
    @DisplayName("양쪽에 모두 있는 도서는 RRF 점수가 합산된다")
    void fuseWhenInBothResultsScoresAreCombinedTest() {

        List<BookSearchResponse> keyword = List.of(this.book(1L, null));
        List<BookSearchResponse> vector = List.of(this.book(1L, 0.9), this.book(2L, 0.8));

        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 1.0, 1.0);

        // 도서 1은 키워드 + 벡터 양쪽 점수 합산 -> 도서 2보다 높아야 함
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().rrfScore()).isGreaterThan(result.get(1).rrfScore());
    }

    @Test
    @DisplayName("양쪽 결과가 모두 비어있으면 빈 목록을 반환한다")
    void fuseWhenBothEmptyReturnEmptyListTest() {

        List<BookSearchResponse> keyword = List.of();
        List<BookSearchResponse> vector = List.of();

        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 1.0, 1.0);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("벡터 가중치가 높으면 벡터 1위 도서의 점수가 더 높다")
    void fuseWhenVectorWeightHigherVectorTopBookScoreHigherTest() {

        // 키워드 1위: 도서 1
        // 벡터 1위: 도서 2
        // (서로 다른 도서)
        List<BookSearchResponse> keyword = List.of(this.book(1L, null));
        List<BookSearchResponse> vector = List.of(this.book(2L, 0.9));

        // 벡터 가중치 높음 (0.6 : 1.4)
        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 0.6, 1.4);

        // 벡터 1위인 도서 2가 더 높은 점수
        assertThat(result.getFirst().id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("RRF 점수는 순위가 높을수록 더 높다")
    void fuseHigherRankHigherScore() {

        List<BookSearchResponse> keyword = List.of(
                this.book(1L, null), // 1위
                this.book(2L, null), // 2위
                this.book(3L, null) // 3위
        );

        List<BookSearchResponse> vector = List.of();

        List<BookSearchResponse> result = this.rrfService.fuse(keyword, vector, 1.0, 1.0);

        // 1위 > 2위 > 3위 순으로 점수 내림차
        assertThat(result.getFirst().rrfScore())
                .isGreaterThan(result.get(1).rrfScore())
                .isGreaterThan(result.get(2).rrfScore());
    }
}