package com.nhnacademy.ailibraryteam1.common.util;

import java.util.Objects;

public class CalculateCosineSimilarity {

    private CalculateCosineSimilarity() {}

    public static double cosineSimilarity(float[] vectorA, float[] vectorB) {

        if (Objects.isNull(vectorA) || Objects.isNull(vectorB) || vectorA.length != vectorB.length) {
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