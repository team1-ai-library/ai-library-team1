package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public void registerFeedback(Feedback feedback) {
        if (feedbackRepository.existsByChatIdAndBookIdAndQuery(feedback.getChatId(), feedback.getBookId(), feedback.getQuery())) {
            throw new BusinessException(ErrorCode.FEEDBACK_DUPLICATED);
        }

        feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<Feedback> getUserFeedbacks(long chatId) {
        return feedbackRepository.findAllByChatId(chatId);
    }

    @Transactional(readOnly = true)
    public boolean hasExistingFeedback(long chatId, long bookId, String query) {
        return feedbackRepository.existsByChatIdAndBookIdAndQuery(chatId, bookId, query);
    }
}
