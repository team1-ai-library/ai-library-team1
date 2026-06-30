package com.nhnacademy.ailibraryteam1.feedback.repository;

import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByChatId(Long chatId);

    boolean existsByChatIdAndBookIdAndQuery(Long chatId, Long bookId, String query);

    List<Feedback> findAllByBookIdIn(Collection<Long> bookIds);
    
    List<Feedback> findAllByChatIdAndBookIdIn(Long chatId, Collection<Long> bookIds);

    Optional<Feedback> findByChatIdAndBookIdAndQuery(Long chatId, Long bookId, String query);
}
