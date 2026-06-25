package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    // 최소 피드백 수
    private static final int MIN_FEEDBACK_THRESHOLD = 3;

    @Transactional
    public void registerFeedback(Feedback feedback) {
        if (feedbackRepository.existsByChatIdAndBookIdAndQuery(feedback.getChatId(), feedback.getBookId(), feedback.getQuery())) {
            throw new BusinessException(ErrorCode.FEEDBACK_DUPLICATED);
        }

        feedbackRepository.save(feedback);
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

        // DB에서 피드백 일괄 조회
        List<Feedback> allFeedbacks = feedbackRepository.findAllByBookIdIn(bookIds);

        // 도서 ID로 그룹화
        Map<Long, List<Feedback>> globalFeedbackMap = allFeedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getBookId));


        bookIds.forEach(bookId -> {
            List<Feedback> feedbacks = globalFeedbackMap.getOrDefault(bookId, List.of());

            if (feedbacks.size() < MIN_FEEDBACK_THRESHOLD) {
                scoreMap.put(bookId, 0.0);
                return;
            }

            long goodCount = feedbacks.stream()
                    .filter(f -> f.getType() == FeedbackType.GOOD)
                    .count();
            long badCount = feedbacks.size() - goodCount;

            double scoreRatio = (double) (goodCount - badCount) / feedbacks.size();

            scoreMap.put(bookId, scoreRatio);
        });

        return scoreMap;
    }

    // 도서 목록에 대한 사용자 선호도 점수
    @Transactional(readOnly = true)
    public Map<Long, Double> getPersonalizationFeedbackScores(long chatId, List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        List<Feedback> userFeedbacks = feedbackRepository.findAllByChatIdAndBookIdIn(chatId, bookIds);

        Map<Long, Double> userFeedbackScoreMap = userFeedbacks.stream()
                .collect(Collectors.toMap(
                        Feedback::getBookId,
                        Feedback::getFeedbackScore,
                        Double::sum // 동일한 도서에 대한 피드백 점수 합산
                ));

        return bookIds.stream()
                .collect(Collectors.toMap(id -> id, id -> userFeedbackScoreMap.getOrDefault(id, 0.0)));
    }
}
