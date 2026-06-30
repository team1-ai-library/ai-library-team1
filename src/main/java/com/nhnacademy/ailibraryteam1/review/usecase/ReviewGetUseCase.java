package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewDetailResponse;
import com.nhnacademy.ailibraryteam1.review.dto.ReviewResponse;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.service.ReviewService;
import com.nhnacademy.ailibraryteam1.review.service.ReviewStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewGetUseCase {

    private final ReviewService reviewService;
    private final ReviewStatisticService reviewStatisticService;

    /**
     * 특정 도서의 리뷰 상세 정보 조회
     * 리뷰 통계(평점, 별점들)와 리뷰 목록을 리턴
     * 통계 데이터가 없는 경우 리뷰 목록만 리턴
     */
    public ReviewDetailResponse execute(long bookId) {

        // 리뷰 통계 조회 (리뷰가 없으면 널)
        BookReviewStatistic reviewStatistic = this.reviewStatisticService.findStatistic(bookId)
                .orElse(null);

        // 리뷰 목록 조회 및 ReviewResponse DTO로 변환
        List<ReviewResponse> reviews = this.reviewService.getAllBookReviews(bookId)
                .stream()
                .map(review -> new ReviewResponse(
                        review.getId(),
                        review.getContent(),
                        review.getRating(),
                        review.getCreatedAt()
                ))
                .toList();

        // 통계 데이터가 없으면 리뷰 목록만 리턴
        if (Objects.isNull(reviewStatistic)) {
            return new ReviewDetailResponse(
                    null, 0L, 0, 0, 0, 0, 0, reviews
            );
        }

        return new ReviewDetailResponse(
                reviewStatistic.getAverageRating(),
                reviewStatistic.getReviewCount(),
                reviewStatistic.getRating1Count(),
                reviewStatistic.getRating2Count(),
                reviewStatistic.getRating3Count(),
                reviewStatistic.getRating4Count(),
                reviewStatistic.getRating5Count(),
                reviews
        );
    }
}