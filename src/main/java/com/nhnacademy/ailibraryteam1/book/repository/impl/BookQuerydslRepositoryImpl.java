package com.nhnacademy.ailibraryteam1.book.repository.impl;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.dto.QBookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.entity.QBook;
import com.nhnacademy.ailibraryteam1.book.entity.QBookEmbedding;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.pgvector.PGvector;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
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
public class BookQuerydslRepositoryImpl implements BookQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    // 키워드 검색
    @Override
    public Page<BookSearchResponse> searchByKeyword(String isbn, String keyword, Pageable pageable) {
        QBook book = QBook.book;

        boolean isbnExists = (Objects.nonNull(isbn) && !isbn.isBlank());

        // 키워드 검색 시 '제목, 저자'는 LIKE로
        // bookContent는 GIN 인덱스 태우는 전문 검색으로
        BooleanExpression condition;
        if (isbnExists) {
            // ISBN이 있으면 ISBN 만으로 검색
            condition = book.isbn.eq(isbn);
        } else {
            // LIKE 검색 (제목 또는 저자명 키워드 포함 검색)
            BooleanExpression likeCondition = book.title.containsIgnoreCase(keyword)
                    .or(book.authorName.containsIgnoreCase(keyword));

            // 인덱스 전문 검색 (book_content)
            BooleanExpression ftsCondition = Expressions.booleanTemplate(
                    "function('ts_match_korean', {0}, {1}) = true",
                    book.bookContent,
                    keyword
            );

            condition = likeCondition.or(ftsCondition);
        }

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
                        book.imageUrl,
                        Expressions.nullExpression(Double.class))) // 널 그대로 넣으면 QueryDSL이 타입 추론 못 해서 문제 생길 수 있으므로 Double 타입 명시
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

    // 벡터 검색
    @Override
    public Page<BookSearchResponse> searchByVector(float[] queryVector, Pageable pageable) {

        QBook book = QBook.book;
        QBookEmbedding bookEmbedding = QBookEmbedding.bookEmbedding;

        // float[] -> "[0.1, 0.2, ...]" 형태 문자열로 변환
        String vectorString = new PGvector(queryVector).toString();

        NumberTemplate<Double> similarity = Expressions.numberTemplate(
                Double.class,
                "function('vector_cosine_similarity', {0}, {1})",
                bookEmbedding.embedding,
                vectorString
        );

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
                        book.imageUrl,
                        similarity))
                .from(book)
                .join(bookEmbedding).on(book.id.eq(bookEmbedding.bookId))
                .orderBy(similarity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(Wildcard.count)
                .from(book)
                .join(bookEmbedding).on(book.id.eq(bookEmbedding.bookId));

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }
}