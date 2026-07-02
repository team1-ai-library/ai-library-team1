package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookAiRecommendationResponse;
import com.nhnacademy.ailibraryteam1.book.dto.BookRecommendationResult;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class BookRecommendWithDetailUseCaseTest {

    @Mock
    private BookRagService bookRagService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookRecommendWithDetailUseCase bookRecommendWithDetailUseCase;

    @Test
    @DisplayName("AI 추천 결과가 없으면 빈 리스트 반환한다")
    void noAiResultsThenReturnEmptyListTest() {

        given(this.bookRagService.recommendBooks("자바 책 추천", "ollama", "conv1"))
                .willReturn(List.of());

        List<BookRecommendationResult> result = this.bookRecommendWithDetailUseCase.execute("자바 책 추천", "ollama", "conv1");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("AI 추천 결과와 도서 정보를 합쳐서 반환한다")
    void aiResultsExistThenReturnCombinedResultTest() {

        List<BookAiRecommendationResponse> aiResults = List.of(
                new BookAiRecommendationResponse(1L, 90, "자바 핵심 도서"),
                new BookAiRecommendationResponse(2L, 80, "자바 입문서")
        );

        Book book1 = mock(Book.class);
        Book book2 = mock(Book.class);

        given(bookRagService.recommendBooks("자바 책 추천", "gemini", "conv-1")).willReturn(aiResults);
        given(bookService.getBooks(List.of(1L, 2L))).willReturn(List.of(book1, book2));
        given(book1.getId()).willReturn(1L);
        given(book1.getTitle()).willReturn("자바의 정석");
        given(book1.getAuthorName()).willReturn("남궁성");
        given(book1.getPublisherName()).willReturn("도우출판");
        given(book1.getImageUrl()).willReturn("http://image1.jpg");
        given(book2.getId()).willReturn(2L);
        given(book2.getTitle()).willReturn("이펙티브 자바");
        given(book2.getAuthorName()).willReturn("조슈아 블로크");
        given(book2.getPublisherName()).willReturn("인사이트");
        given(book2.getImageUrl()).willReturn("http://image2.jpg");

        List<BookRecommendationResult> result = bookRecommendWithDetailUseCase.execute("자바 책 추천", "gemini", "conv-1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.getFirst().title()).isEqualTo("자바의 정석");
        assertThat(result.get(0).relevance()).isEqualTo(90);
        assertThat(result.get(0).why()).isEqualTo("자바 핵심 도서");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).title()).isEqualTo("이펙티브 자바");
    }

    @Test
    @DisplayName("도서 정보가 없으면 기본값으로 채운다")
    void bookNotFoundThenUsesDefaultValuesTest() {

        List<BookAiRecommendationResponse> aiResults = List.of(
                new BookAiRecommendationResponse(999L, 70, "추천 이유")
        );

        given(bookRagService.recommendBooks("자바 책 추천", "gemini", "conv-1")).willReturn(aiResults);
        given(bookService.getBooks(List.of(999L))).willReturn(List.of());

        List<BookRecommendationResult> result = bookRecommendWithDetailUseCase.execute("자바 책 추천", "gemini", "conv-1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("알 수 없음");
        assertThat(result.getFirst().authorName()).isEmpty();
        assertThat(result.getFirst().publisherName()).isEmpty();
        assertThat(result.getFirst().imageUrl()).isNull();
    }
}