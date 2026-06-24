package com.nhnacademy.ailibraryteam1.feedback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "query", nullable = false)
    private String query;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackType type;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public static Feedback create(long chatId, long bookId, String query, FeedbackType type) {
        return new Feedback(null, chatId, bookId, query, type, null);
    }
}
