package com.nhnacademy.ailibraryteam1.rabbitmq.event;

public record ReviewCreatedEvent(
        long bookId
) {}
