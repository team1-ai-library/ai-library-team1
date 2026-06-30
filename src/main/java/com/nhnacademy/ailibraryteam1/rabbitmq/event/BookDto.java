package com.nhnacademy.ailibraryteam1.rabbitmq.event;

import com.nhnacademy.ailibraryteam1.book.entity.Book;

public record BookDto(
        long id,
        String isbn,
        String title,
        String authorName,
        String bookContent
) {
    public static BookDto create(Book book) {
        return new BookDto(book.getId(), book.getIsbn(), book.getTitle(), book.getAuthorName(), book.getBookContent());
    }
}
