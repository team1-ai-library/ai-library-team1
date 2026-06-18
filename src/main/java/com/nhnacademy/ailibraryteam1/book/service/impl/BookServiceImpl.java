package com.nhnacademy.ailibraryteam1.book.service.impl;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQueryRepository;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookQueryRepository bookQueryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {
        return bookQueryRepository.searchByKeyword(isbn, keyword, pageable);
    }
}
