package com.nhnacademy.ailibraryteam1.feedback.service;

import com.nhnacademy.ailibraryteam1.book.entity.BookEmbedding;
import com.nhnacademy.ailibraryteam1.book.repository.BookEmbeddingRepository;
import com.nhnacademy.ailibraryteam1.feedback.entity.Feedback;
import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import com.nhnacademy.ailibraryteam1.feedback.repository.FeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PersonalizationServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private BookEmbeddingRepository bookEmbeddingRepository;

    @InjectMocks
    private PersonalizationService personalizationService;

    @Test
    @DisplayName("요청된 도서 ID 목록이 비어있으면 빈 맵을 반환한다")
    void getPersonalizationScores_WhenEmptyInput_ReturnsEmptyMap() {
        // when
        Map<Long, Double> scores = personalizationService.getPersonalizationScores(1L, List.of());

        // then
        assertThat(scores)
                .isEmpty();
    }

    @Test
    @DisplayName("사용자의 긍정 피드백이 부족하면 모든 점수를 0.0으로 반환한다")
    void getPersonalizationScores_WhenInsufficientFeedback_ReturnsZeroScores() {
        // given
        long chatId = 1L;
        List<Long> bookIds = List.of(10L, 11L);

        given(feedbackRepository.findAllByChatId(chatId))
                .willReturn(List.of());

        // when
        Map<Long, Double> scores = personalizationService.getPersonalizationScores(chatId, bookIds);

        // then
        assertThat(scores)
                .hasSize(2)
                .containsEntry(10L, 0.0)
                .containsEntry(11L, 0.0);
    }

    @Test
    @DisplayName("사용자 취향 기반으로 도서 유사도 점수를 계산한다")
    void getPersonalizationScores_WhenValidFeedback_CalculatesSimilarities() {
        // given
        long chatId = 1L;
        List<Long> bookIds = List.of(10L);

        Feedback f1 = Feedback.create(chatId, 1L, "q", FeedbackType.GOOD);
        Feedback f2 = Feedback.create(chatId, 2L, "q", FeedbackType.GOOD);
        Feedback f3 = Feedback.create(chatId, 3L, "q", FeedbackType.GOOD);

        given(feedbackRepository.findAllByChatId(chatId))
                .willReturn(List.of(f1, f2, f3));

        BookEmbedding be1 = new BookEmbedding(1L, new float[]{1.0f, 0.0f});
        BookEmbedding be2 = new BookEmbedding(2L, new float[]{1.0f, 0.0f});
        BookEmbedding be3 = new BookEmbedding(3L, new float[]{1.0f, 0.0f});

        // 생성 시간이 빠른 피드백 순서대로 정렬되어서 들어감
        given(bookEmbeddingRepository.findAllByBookIdIn(List.of(3L, 2L, 1L)))
                .willReturn(List.of(be1, be2, be3));

        BookEmbedding targetBe = new BookEmbedding(10L, new float[]{0.0f, 1.0f});

        given(bookEmbeddingRepository.findAllByBookIdIn(bookIds))
                .willReturn(List.of(targetBe));

        // when
        Map<Long, Double> scores = personalizationService.getPersonalizationScores(chatId, bookIds);

        // then
        assertThat(scores)
                .containsEntry(targetBe.getBookId(), 0.0);
    }
}
