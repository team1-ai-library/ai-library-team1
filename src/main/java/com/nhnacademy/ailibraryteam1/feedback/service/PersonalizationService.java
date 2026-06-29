package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.book.entity.BookEmbedding;
import com.nhnacademy.ailibraryteam1.book.repository.BookEmbeddingRepository;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalizationService {
    private final FeedbackRepository feedbackRepository;
    private final BookEmbeddingRepository bookEmbeddingRepository;

    // 개인화를 위해 필요한 최소 피드백 수
    private static final int MIN_GOOD_PREFERENCE_THRESHOLD = 3;

    // 개인화에 사용되는 최대 피드백 수
    private static final int MAX_LATEST_FEEDBACK_LIMIT = 20;

    @Transactional(readOnly = true)
    public Map<Long, Double> getPersonalizationScores(long chatId, List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Double> scoreMap = bookIds.stream()
                .collect(Collectors.toMap(id -> id, id -> 0.0));

        float[] userPreferenceVector = calculatorUserPreferenceVector(chatId);

        // 사용자의 취향 정보가 부족하면 개인화 점수 미반영
        if (userPreferenceVector == null) {
            return scoreMap;
        }

        List<BookEmbedding> embeddings = bookEmbeddingRepository.findAllByBookIdIn(bookIds);

        embeddings.forEach(embedding -> {
            long bookId = embedding.getBookId();

            if (scoreMap.containsKey(bookId)) {
                double similarity = cosineSimilarity(userPreferenceVector, embedding.getEmbedding());

                scoreMap.put(bookId, similarity);
            }
        });

        return scoreMap;
    }

    private float[] calculatorUserPreferenceVector(long chatId) {
        List<Feedback> userFeedbacks = feedbackRepository.findAllByChatId(chatId);

        List<Long> likedBookIds = userFeedbacks.stream()
                .filter(f -> f.getType() == FeedbackType.GOOD)
                .sorted(Comparator.comparing(Feedback::getCreatedAt).reversed())
                .map(Feedback::getBookId)
                .distinct()
                .limit(MAX_LATEST_FEEDBACK_LIMIT)
                .toList();

        if (likedBookIds.size() < MIN_GOOD_PREFERENCE_THRESHOLD) {
            return null;
        }

        List<BookEmbedding> bookEmbeddings = bookEmbeddingRepository.findAllByBookIdIn(likedBookIds);

        List<float[]> vectorList = bookEmbeddings.stream()
                .map(BookEmbedding::getEmbedding)
                .filter(Objects::nonNull)
                .toList();

        if (vectorList.isEmpty()) {
            return null;
        }

        return calculateAverageVector(vectorList);
    }

    private float[] calculateAverageVector(List<float[]> vectors) {
        int dimension = vectors.getFirst().length;
        float[] avgVector = new float[dimension];

        for (float[] vector: vectors) {
            for (int i = 0; i < dimension; i++) {
                avgVector[i] += vector[i];
            }
        }

        for (int i = 0; i < dimension; i++) {
            avgVector[i] /= vectors.size();
        }

        return avgVector;
    }

    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
