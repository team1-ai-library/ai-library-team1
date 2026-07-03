package com.nhnacademy.ailibraryteam1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.ailibraryteam1.controller.rest.ReviewController;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewCreateRequest;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewDetailResponse;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewCreateUseCase;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewGetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewCreateUseCase reviewCreateUseCase;

    @MockitoBean
    private ReviewGetUseCase reviewGetUseCase;

    @Test
    @DisplayName("리뷰 목록 조회 시 200을 반환한다")
    void getReviews_Returns200() throws Exception {

        ReviewDetailResponse response = new ReviewDetailResponse(
                new BigDecimal("4.5"), 10L,
                0, 0, 1, 4, 5,
                List.of()
        );
        given(reviewGetUseCase.execute(1L)).willReturn(response);

        mockMvc.perform(get("/api/books/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.reviewCount").value(10));
    }

    @Test
    @DisplayName("리뷰 작성 시 201을 반환한다")
    void createReview_Returns201() throws Exception {

        ReviewCreateRequest request = new ReviewCreateRequest("좋은 책입니다.", 5);
        willDoNothing().given(reviewCreateUseCase).execute(eq(1L), any(ReviewCreateRequest.class));

        mockMvc.perform(post("/api/books/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("별점이 범위를 벗어나면 400을 반환한다")
    void createReview_WhenInvalidRating_Returns400() throws Exception {

        // 별점 6점 (유효 범위 1~5)
        ReviewCreateRequest request = new ReviewCreateRequest("좋은 책입니다.", 6);

        mockMvc.perform(post("/api/books/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 내용이 비어있으면 400을 반환한다")
    void createReview_WhenEmptyContent_Returns400() throws Exception {

        ReviewCreateRequest request = new ReviewCreateRequest("", 5);

        mockMvc.perform(post("/api/books/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}