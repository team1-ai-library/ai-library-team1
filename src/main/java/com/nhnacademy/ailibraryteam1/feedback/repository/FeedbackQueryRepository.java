package com.nhnacademy.ailibraryteam1.feedback.repository;

import com.nhnacademy.ailibraryteam1.feedback.dto.BookFeedbackCount;
import com.nhnacademy.ailibraryteam1.feedback.dto.QBookFeedbackCount;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.entity.QFeedback;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedbackQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<BookFeedbackCount> findBookFeedbackCount(List<Long> bookIds) {
        QFeedback feedback = QFeedback.feedback;

        return queryFactory.select(new QBookFeedbackCount(
                feedback.bookId,
                // SUM(CASE WHEN type = 'GOOD' THEN 1 ESLE 0 END)
                new CaseBuilder()
                        .when(feedback.type.eq(FeedbackType.GOOD))
                        .then(1L)
                        .otherwise(0L)
                        .sumLong(),
                feedback.count()))
                .from(feedback)
                .where(feedback.bookId.in(bookIds))
                .groupBy(feedback.bookId)
                .fetch();
    }
}
