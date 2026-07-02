package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@UseCase
@Slf4j
@RequiredArgsConstructor
public class BookKeywordSearchUseCase {

    private final BookQuerydslRepository bookQuerydslRepository;

    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {

        log.info("[BookKeywordSearchUseCase] 키워드 검색 - isbn: {}, keyword: {}", isbn, keyword);

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());
        boolean keywordExists = (Objects.nonNull(keyword) && !keyword.isBlank());

        if (!isbnExists && !keywordExists) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        Page<BookSearchResponse> result = this.bookQuerydslRepository.searchByKeyword(isbn, keyword, pageable);

        log.info("[BookKeywordSearchUseCase] 키워드 검색 완료 - 검색된 도서 수: {}", result.getTotalElements());

        return result;
    }
}