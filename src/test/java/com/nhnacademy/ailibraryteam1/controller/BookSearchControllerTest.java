package com.nhnacademy.ailibraryteam1.controller;

import com.nhnacademy.ailibraryteam1.book.dto.BookRecommendationResult;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookKeywordSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookRecommendWithDetailUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookVectorSearchUseCase;
import com.nhnacademy.ailibraryteam1.controller.rest.BookSearchController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookSearchController.class)
class BookSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookKeywordSearchUseCase bookKeywordSearchUseCase;

    @MockitoBean
    private BookVectorSearchUseCase bookVectorSearchUseCase;

    @MockitoBean
    BookHybridSearchUseCase bookHybridSearchUseCase;

    @MockitoBean
    private BookRecommendWithDetailUseCase bookRecommendWithDetailUseCase;

    private BookSearchResponse book(long id) {
        return new BookSearchResponse(
                id, null, "제목" + id, null, null, null,
                null, null, null, null,
                null, null, null, null, null
        );
    }

    @Test
    @DisplayName("키워드 검색 요청 시 200을 반환한다")
    void search_WhenKeyword_Returns200() throws Exception {

        given(bookKeywordSearchUseCase.searchByKeyword(any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(book(1L), book(2L))));

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "자바")
                        .param("searchType", "keyword"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("벡터 검색 요청 시 200을 반환한다")
    void search_WhenVector_Returns200() throws Exception {

        given(bookVectorSearchUseCase.searchByVector(any(), any()))
                .willReturn(new PageImpl<>(List.of(book(1L))));

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "자바")
                        .param("searchType", "vector"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("하이브리드 검색 요청 시 200을 반환한다")
    void search_WhenHybrid_Returns200() throws Exception {

        given(bookHybridSearchUseCase.searchByHybrid(any(), any()))
                .willReturn(new PageImpl<>(List.of(book(1L), book(2L), book(3L))));

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "자바")
                        .param("searchType", "hybrid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("지원하지 않는 검색 타입이면 400을 반환한다")
    void search_WhenUnsupportedType_Returns400() throws Exception {

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "자바")
                        .param("searchType", "unknown"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RAG 추천 요청 시 200을 반환한다")
    void recommend_WhenValidRequest_Returns200() throws Exception {

        List<BookRecommendationResult> results = List.of(
                new BookRecommendationResult(1L, "자바의 정석", "남궁성", "도우출판", null, 90, "좋은 책")
        );
        given(bookRecommendWithDetailUseCase.execute(anyString(), anyString(), anyString()))
                .willReturn(results);

        mockMvc.perform(get("/api/books/recommend/gemini")
                        .param("question", "자바 책 추천해줘"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("자바의 정석"));
    }

    @Test
    @DisplayName("RAG 추천 결과가 없으면 빈 목록을 반환한다")
    void recommend_WhenNoResults_ReturnsEmptyList() throws Exception {

        given(bookRecommendWithDetailUseCase.execute(anyString(), anyString(), anyString()))
                .willReturn(List.of());

        mockMvc.perform(get("/api/books/recommend/gemini")
                        .param("question", "자바 책 추천해줘"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}