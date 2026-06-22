package com.nhnacademy.ailibraryteam1.book.service.impl;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.book.service.BookService;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());
        boolean keywordExists = (Objects.nonNull(keyword) && !keyword.isBlank());

        if (!isbnExists && !keywordExists) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        return this.bookRepository.searchByKeyword(isbn, keyword, pageable);
    }

    @Override
    public Book getBook(long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, "존재하지 않는 도서: " + id));
    }
}