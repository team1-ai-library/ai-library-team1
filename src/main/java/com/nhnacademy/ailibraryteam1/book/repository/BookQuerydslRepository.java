package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookQuerydslRepository {

    Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable);

    Page<BookSearchResponse> searchByVector(float[] queryVector, Pageable pageable);
}