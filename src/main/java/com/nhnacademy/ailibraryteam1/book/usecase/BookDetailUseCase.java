package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 도서 + 리뷰 통계 + AI 요약 조합
 */
@UseCase
@RequiredArgsConstructor
public class BookDetailUseCase {

    private final BookService bookService;
    private final ReviewAiSummaryRepository reviewAiSummaryRepository;
    private final ReviewStatisticRepository reviewStatisticRepository;

    @Transactional(readOnly = true)
    public BookSearchResponse getBookDetail(long id) {

        Book book = this.bookService.getBook(id);

        BookReviewStatistic reviewStatistic = this.reviewStatisticRepository.findByBookId(id)
                .orElse(null);

        BookReviewAiSummary reviewAiSummary = this.reviewAiSummaryRepository.findByBookId(id)
                .orElse(null);

        return new BookSearchResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getVolumeTitle(),
                book.getAuthorName(),
                book.getPublisherName(),
                book.getPrice(),
                book.getEditionPublishDate(),
                book.getBookContent(),
                book.getImageUrl(),
                null,  // similarity
                null,  // rrfScore
                Objects.nonNull(reviewStatistic) ? reviewStatistic.getAverageRating() : null,  // averageRating
                Objects.nonNull(reviewStatistic) ? reviewStatistic.getReviewCount() : null,  // reviewCount
                Objects.nonNull(reviewAiSummary) ? reviewAiSummary.getReviewSummary() : null   // reviewSummary
        );
    }
}