package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.dto.BookFeedbackCount;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackQueryRepository;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final FeedbackQueryRepository feedbackQueryRepository;

    // 최소 피드백 수
    private static final int MIN_FEEDBACK_THRESHOLD = 3;

    @Transactional
    public void registerOrUpdateFeedback(Feedback feedback) {
        Optional<Feedback> existingFeedback = feedbackRepository.findByChatIdAndBookIdAndQuery(feedback.getChatId(), feedback.getBookId(), feedback.getQuery());

        // 피드백이 이미 존재하는 경우
        existingFeedback.ifPresentOrElse(f -> {
            if (f.getType() == feedback.getType()) {
                // 동일한 타입이면 중복 알림 던지기
                throw new BusinessException(ErrorCode.FEEDBACK_DUPLICATED);
            } else {
                // 다른 타입이라면 피드백 업데이트
                f.updateType(feedback.getType());
            }
        },
        // 피드백이 존재하지 않는 경우 저장
        () -> feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public boolean hasExistingFeedback(long chatId, long bookId, String query) {
        return feedbackRepository.existsByChatIdAndBookIdAndQuery(chatId, bookId, query);
    }

    // 도서 목록에 대한 글로벌 선호도 점수
    @Transactional(readOnly = true)
    public Map<Long, Double> getGlobalFeedbackScores(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        // Null 방지를 위한 기본값 설정
        Map<Long, Double> scoreMap = bookIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0.0));

        // DB에서 도서별 피드백 통계 조회
        List<BookFeedbackCount> allFeedbacks = feedbackQueryRepository.findBookFeedbackCount(bookIds);

        // 도서 ID로 그룹화
        Map<Long, BookFeedbackCount> globalFeedbackMap = allFeedbacks.stream()
                .collect(Collectors.toMap(BookFeedbackCount::bookId, count -> count));


        bookIds.forEach(bookId -> {
            BookFeedbackCount count = globalFeedbackMap.get(bookId);

            // 피드백 개수가 너무 적은 경우는 반영 X
            if (count == null || count.totalCount() < MIN_FEEDBACK_THRESHOLD) {
                scoreMap.put(bookId, 0.0);
                return;
            }

            long goodCount = count.goodCount();
            long badCount = count.totalCount() - goodCount;

            double scoreRatio = (double) (goodCount - badCount) / count.totalCount();

            scoreMap.put(bookId, scoreRatio);
        });

        return scoreMap;
    }
}
