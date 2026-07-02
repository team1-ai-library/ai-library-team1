package com.nhnacademy.ailibraryteam1.rabbitmq;

import com.nhnacademy.ailibraryteam1.rabbitmq.event.ReviewEmbeddingEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Service
public class ReviewEmbeddingProducer {
    private static final Logger log = LoggerFactory.getLogger(ReviewEmbeddingProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void sendReviewEmbedding(ReviewEmbeddingEvent event) {
        log.info("[RabbitMQ] 리뷰 임베딩 이벤트 발생");
        rabbitTemplate.convertAndSend(
                "library.team1.review.exchange",
                "library.team1.review.embedding",
                event
        );
    }
}
