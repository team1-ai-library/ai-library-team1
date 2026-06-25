package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@UseCase
@RequiredArgsConstructor
public class BookKeywordSearchUseCase {

    private final BookQuerydslRepository bookQuerydslRepository;

    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());
        boolean keywordExists = (Objects.nonNull(keyword) && !keyword.isBlank());

        if (!isbnExists && !keywordExists) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        return this.bookQuerydslRepository.searchByKeyword(isbn, keyword, pageable);
    }
}