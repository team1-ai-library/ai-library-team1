package com.nhnacademy.ailibraryteam1.review.entity;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(name = "book_review_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookReviewStatistic {

    @Id
    @Column(name = "book_id")
    private Long bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "review_count")
    private long reviewCount;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "rating_1_count")
    private int rating1Count;

    @Column(name = "rating_2_count")
    private int rating2Count;

    @Column(name = "rating_3_count")
    private int rating3Count;

    @Column(name = "rating_4_count")
    private int rating4Count;

    @Column(name = "rating_5_count")
    private int rating5Count;

    @Column(name = "last_reviewed_at")
    private OffsetDateTime lastReviewedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }


    public static BookReviewStatistic create(Book book) {
        return new BookReviewStatistic(null, book, 0L, BigDecimal.ZERO, 0, 0, 0, 0, 0, null, null);
    }

    public void addReview(BookReview review) {
        switch (review.getRating()) {
            case 1 -> this.rating1Count++;
            case 2 -> this.rating2Count++;
            case 3 -> this.rating3Count++;
            case 4 -> this.rating4Count++;
            case 5 -> this.rating5Count++;
            default -> throw new BusinessException(ErrorCode.RATING_INVALID);
        }

        this.reviewCount++;

        long totalSum = (this.rating1Count) +
                (this.rating2Count * 2L) +
                (this.rating3Count * 3L) +
                (this.rating4Count * 4L) +
                (this.rating5Count * 5L);

        this.averageRating = BigDecimal.valueOf(totalSum).divide(BigDecimal.valueOf(this.reviewCount), 2, RoundingMode.HALF_UP);

        this.lastReviewedAt = review.getCreatedAt();
    }
}
