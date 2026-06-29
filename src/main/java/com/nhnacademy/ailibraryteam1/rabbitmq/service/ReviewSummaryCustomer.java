package com.nhnacademy.ailibraryteam1.rabbitmq.service;

import com.nhnacademy.ailibraryteam1.review.event.ReviewCreatedEvent;
import com.nhnacademy.ailibraryteam1.review.usecase.ReviewSummarizeUseCase;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReviewSummaryCustomer {
    private static final Logger log = LoggerFactory.getLogger(ReviewSummaryCustomer.class);

    private final ReviewSummarizeUseCase summarizeUseCase;

    @RabbitListener(queues = "library.team1.inner.review")
    public void processSummary(
            ReviewCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws Exception {
        log.info("[RabbitMQ] 리뷰요약 이벤트 수신");
        try {
            sendReview(event);

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.info("[Review Summery] 리뷰 이벤트 처리 실패");
            channel.basicNack(deliveryTag, false, false);
        }
    }

    private void sendReview(ReviewCreatedEvent event) {
        summarizeUseCase.execute(event.bookId());
    }
}
