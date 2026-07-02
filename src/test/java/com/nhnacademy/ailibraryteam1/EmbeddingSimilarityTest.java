package com.nhnacademy.ailibraryteam1;

import com.nhnacademy.ailibraryteam1.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@IntegrationTest
class EmbeddingSimilarityTest {

    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Test
    void compareTwoWords() {
        float[] vec1 = embeddingModel.embed("광주에서 토비의 스프링 빌릴 수 있는 도서관 알려줄 수 있을까?");
        float[] vec2 = embeddingModel.embed("광주에서 토비의 스프링 대여할 수 있는 도서관 알려줘");

        double similarity = cosineSimilarity(vec1, vec2);
        System.out.println(" 유사도: " + similarity);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}