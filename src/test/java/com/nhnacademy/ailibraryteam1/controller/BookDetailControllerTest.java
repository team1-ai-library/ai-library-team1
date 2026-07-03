package com.nhnacademy.ailibraryteam1.controller;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.usecase.BookDetailUseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.controller.rest.BookDetailController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookDetailController.class)
class BookDetailControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private BookDetailUseCase bookDetailUseCase;

    private BookSearchResponse bookSearchResponse(long id) {
        return new BookSearchResponse(
                id, "9788960777330", "자바의 정석", null,
                "남궁성", "도우출판", null, null,
                "자바 입문서", "http://image.jpg",
                null, null, null, null, null
        );
    }

    @Test
    @DisplayName("도서 ID로 조회하면 200을 반환한다")
    void existsIdReturns200Test() throws Exception {

        given(this.bookDetailUseCase.getBookDetail(1L)).willReturn(this.bookSearchResponse(1L));

        this.mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("자바의 정석"))
                .andExpect(jsonPath("$.authorName").value("남궁성"));
    }

    @Test
    @DisplayName("존재하지 않는 도서 ID로 조회하면 404 반환한다")
    void notExistsIdReturns404Test() throws Exception {

        given(this.bookDetailUseCase.getBookDetail(999L))
                .willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        this.mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }
}