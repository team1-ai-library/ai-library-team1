package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.QBook;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BookQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {
        QBook book = QBook.book;

        boolean isbnExists = (isbn != null && !isbn.isBlank());

        // ISBN 여부에 따라 조건 분기
        BooleanExpression condition = (isbnExists)
                ? book.isbn.eq(isbn).and(book.title.containsIgnoreCase(keyword))
                : book.title.containsIgnoreCase(keyword);

        List<BookSearchResponse> result =  queryFactory
                .select(Projections.constructor(BookSearchResponse.class,
                        book.id,
                        book.isbn,
                        book.title,
                        book.volumeTitle,
                        book.authorName,
                        book.publisherName,
                        book.price,
                        book.editionPublishDate,
                        book.bookContent,
                        book.imageUrl))
                .from(book)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이지 개수 표시를 위한 COUNT 쿼리
        JPAQuery<Long> countQuery = queryFactory.select(Wildcard.count)
                .from(book)
                .where(condition);

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }
}
