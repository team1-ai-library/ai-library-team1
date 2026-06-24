package com.nhnacademy.ailibraryteam1.review.service;

import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewSummarizer {
    private final ChatClient chatClient;

    public ReviewSummarizer(@Qualifier("geminiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String generateNewReviewSummary(String title, List<BookReview> allReviews) {

        String reviewText = allReviews.stream()
                .map(r -> String.format("[평점: %d] %s", r.getRating(), r.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                    다음은 '%s' 도서 리뷰 목록입니다. 전체 내용을 2~3문장으로 요약해주세요.
                """.formatted(title);

        return chatClient.prompt()
                .system(prompt)
                .user(reviewText)
                .call()
                .content();
    }

    public String updateReviewSummary(String prevSummary, List<BookReview> newReviews) {
        String reviewText = newReviews.stream()
                .map(r -> String.format("[평점: %d] %s", r.getRating(), r.getContent()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                    기존 요약에 새 리뷰를 반영하여 2~3문장으로 업데이트해주세요.
                """;


        return chatClient.prompt()
                .system(prompt)
                .user("기존 요약:\n" + prevSummary + "\n\n새로운 리뷰:\n" + reviewText)
                .call()
                .content();
    }
}
