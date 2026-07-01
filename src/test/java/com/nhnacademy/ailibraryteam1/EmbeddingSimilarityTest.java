package com.nhnacademy.ailibraryteam1;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmbeddingSimilarityTest {

    @Autowired
    @Qualifier("openAiEmbeddingModel")
    private EmbeddingModel embeddingModel;

    @Test
    void compareTwoWords() {
        float[] vec1 = embeddingModel.embed("커피");
        float[] vec2 = embeddingModel.embed("자바");

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