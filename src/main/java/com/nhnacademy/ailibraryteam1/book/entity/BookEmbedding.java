package com.nhnacademy.ailibraryteam1.book.entity;

import com.nhnacademy.ailibraryteam1.common.util.VectorConverter;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_embeddings")
@Getter
@NoArgsConstructor
public class BookEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Convert(converter = VectorConverter.class)
    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public BookEmbedding(Long bookId, float[] embedding) {
        this.bookId = bookId;
        this.embedding = embedding;
        this.createdAt = LocalDateTime.now();
    }
}