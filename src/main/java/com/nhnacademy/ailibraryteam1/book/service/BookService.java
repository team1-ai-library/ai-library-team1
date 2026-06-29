package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewStatistic;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewStatisticRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final ReviewAiSummaryRepository reviewAiSummaryRepository;
    private final ReviewStatisticRepository reviewStatisticRepository;

    public Book getBook(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "존재하지 않는 도서: " + id));
    }

    @Transactional(readOnly = true)
    public List<Book> getBooks(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return bookRepository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public BookSearchResponse getBookDetail(long id) {

        Book book = this.getBook(id);

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