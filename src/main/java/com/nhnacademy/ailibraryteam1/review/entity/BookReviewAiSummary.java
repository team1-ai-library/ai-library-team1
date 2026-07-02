package com.nhnacademy.ailibraryteam1.review.entity;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "book_review_ai_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookReviewAiSummary {

    @Id
    @Column(name = "book_id")
    private Long bookId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(columnDefinition = "TEXT")
    private String reviewSummary;

    @Column(name = "last_review_id")
    private Long lastReviewId;

    @Column(name = "generating_started_at")
    private OffsetDateTime generatingStartedAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    private Long version;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public static BookReviewAiSummary create(Book book) {
        return new BookReviewAiSummary(
                null,
                book,
                "",
                null,
                null,
                OffsetDateTime.now(),
                null
        );
    }

    public void startGenerating() {
        this.generatingStartedAt = OffsetDateTime.now();
    }

    public void stopGenerating() {
        this.generatingStartedAt = null;
    }

    public void updateSummary(long lastReviewId, String summary) {
        this.reviewSummary = summary;
        this.updatedAt = OffsetDateTime.now();
        this.generatingStartedAt = null;
        this.lastReviewId = lastReviewId;
    }

    public boolean isGenerating() {
        return this.generatingStartedAt != null;
    }
}
