package com.nhnacademy.ailibraryteam1.common.client;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 임베딩 생성
 * AI 모델 호출하여 텍스트를 벡터로 변환
 */
@Component
@RequiredArgsConstructor
public class EmbeddingClient {

    // Spring AI가 자동구성으로 만들어준 OpenAiEmbeddingModel Bean이 주입됨
    private final EmbeddingModel embeddingModel;

    // 단일 텍스트 임베딩
    public float[] getEmbedding(String text) {

        return this.embeddingModel.embedForResponse(List.of(text)) // EmbeddingResponse
                .getResults()
                .getFirst()
                .getOutput();
    }

    // 배치 임베딩 여러 텍스트를 한 번에 처리
    public List<float[]> getEmbeddings(List<String> texts) {

        return this.embeddingModel.embedForResponse(texts) // EmbeddingResponse
                .getResults().stream()
                .map(Embedding::getOutput)
                .toList();
    }
}