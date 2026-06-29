package com.nhnacademy.ailibraryteam1.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange reviewExchange() {
        return new TopicExchange("library.team1.review.exchange");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("library.team1.review.dlx");
    }

    @Bean
    public Queue reviewQueue() {
        return QueueBuilder.durable("library.team1.review.embedding")
            .withArgument("x-dead-letter-exchange", "library.team1.review.dlx")
            .build();
    }

    @Bean
    public Queue reviewSummaryQueue() {
        return QueueBuilder.durable("library.team1.inner.review")
                .withArgument("x-dead-letter-exchange", "library.team1.review.dlx")
                .build();
    }

    @Bean
    public Binding reviewBinding() {
        return BindingBuilder
            .bind(reviewQueue())
            .to(reviewExchange())
            .with("library.team1.review.#");
    }

    @Bean
    public Binding reviewSummaryBinding() {
        return BindingBuilder
                .bind(reviewSummaryQueue())
                .to(reviewExchange())
                .with("library.team1.inner.#");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("library.team1.review.embedding.dlq").build();
    }

    // 4. DLX와 DLQ 바인딩(연결)하기
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("#"); // 모든 라우팅 키를 다 받아주겠다는 의미 (Topic 설정이 아닐 경우 그냥 기본 매칭)
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}