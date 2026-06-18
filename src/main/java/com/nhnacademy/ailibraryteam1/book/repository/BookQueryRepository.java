package com.nhnacademy.ailibraryteam1.book.repository;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.dto.QBookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.QBook;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
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
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class BookQueryRepository {
    private final JPAQueryFactory queryFactory;

    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {
        QBook book = QBook.book;

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());

        // ISBN이 있으면 ISBN만으로 검색, 없으면 제목 또는 저자명에 키워드 포함 검색
        BooleanExpression condition = isbnExists
                ? book.isbn.eq(isbn)
                : book.title.containsIgnoreCase(keyword)
                  .or(book.authorName.containsIgnoreCase(keyword));

        // Projections.constructor 대신 new QBookSearchResponse()
        List<BookSearchResponse> result = queryFactory
                .select(new QBookSearchResponse(
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
