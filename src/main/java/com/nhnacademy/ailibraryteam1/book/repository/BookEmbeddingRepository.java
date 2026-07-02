package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.entity.BookEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    List<BookEmbedding> findAllByBookIdIn(Collection<Long> bookIds);
}
