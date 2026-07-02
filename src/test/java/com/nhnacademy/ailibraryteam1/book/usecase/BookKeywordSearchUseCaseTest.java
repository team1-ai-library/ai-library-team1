package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookKeywordSearchUseCaseTest {

    @Mock
    private BookQuerydslRepository bookQuerydslRepository;

    @InjectMocks
    private BookKeywordSearchUseCase bookKeywordSearchUseCase;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void invalidTest() {
        assertThatThrownBy(() -> bookKeywordSearchUseCase.searchByKeyword(null, null, pageable))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void onlyIsbn() {
        Page<BookSearchResponse> expectedPage = new PageImpl<>(List.of());
        given(bookQuerydslRepository.searchByKeyword("9788960777330", null, pageable)).willReturn(expectedPage);
        Page<BookSearchResponse> result = bookKeywordSearchUseCase.searchByKeyword("9788960777330", null, pageable);
        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    void onlyKeyword() {
        Page<BookSearchResponse> expectedPage = new PageImpl<>(List.of());
        given(bookQuerydslRepository.searchByKeyword(null, "스프링", pageable)).willReturn(expectedPage);

        Page<BookSearchResponse> result = bookKeywordSearchUseCase.searchByKeyword(null, "스프링", pageable);
        assertThat(result).isEqualTo(expectedPage);
    }
}