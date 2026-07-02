package com.nhnacademy.ailibraryteam1.book.service;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("존재하는 도서 ID로 조회하면 도서를 반환한다")
    void getBookWhenExistBookIdTest() {

        Book book = new Book();

        given(this.bookRepository.findById(1L)).willReturn(Optional.of(book));

        Book result = this.bookService.getBook(1L);

        assertThat(result).isEqualTo(book);
        then(this.bookRepository).should().findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 도서 ID로 조회하면 예외를 던진다")
    void getBookWhenNotExistThrowsExceptionTest() {

        given(this.bookRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> this.bookService.getBook(999L))
                .asInstanceOf(type(BusinessException.class))
                .returns(ErrorCode.BOOK_NOT_FOUND, BusinessException::getErrorCode);
    }

    @Test
    @DisplayName("도서 ID 목록으로 조회하면 도서 목록을 반환한다")
    void getBooksWhenValidIdReturnBooksTest() {

        List<Long> ids = List.of(1L, 2L, 3L);
        List<Book> books = List.of(new Book(), new Book(), new Book());
        given(this.bookRepository.findAllById(ids)).willReturn(books);

        List<Book> result = this.bookService.getBooks(ids);

        assertThat(result).hasSize(3);
        then(this.bookRepository).should().findAllById(ids);
    }

    @Test
    @DisplayName("도서 ID 목록이 널이면 빈 목록 리턴한다")
    void getBooksWhenNullReturnEmptyListTest() {

        List<Book> result = this.bookService.getBooks(null);

        assertThat(result).isEmpty();
        then(this.bookRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("도서 ID 목록이 비어있으면 빈 목록 리턴한다")
    void getBooksWhenEmptyReturnEmptyList() {

        List<Book> result = this.bookService.getBooks(List.of());

        assertThat(result).isEmpty();
        then(this.bookRepository).shouldHaveNoInteractions();
    }
}