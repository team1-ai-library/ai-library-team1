package com.nhnacademy.ailibraryteam1.telegram.command;

import com.nhnacademy.ailibraryteam1.telegram.TelegramInteractionLog;
import com.nhnacademy.ailibraryteam1.telegram.dto.TelegramBookSearchResult;
import com.nhnacademy.ailibraryteam1.telegram.dto.TelegramMessageInfo;
import com.nhnacademy.ailibraryteam1.telegram.usecase.TelegramBookSearchUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SearchCommandHandlerTest {

    @Mock
    private TelegramBookSearchUseCase telegramBookSearchUseCase;

    @Mock
    private TelegramInteractionLog interactionLog;

    @InjectMocks
    private SearchCommandHandler searchCommandHandler;

    private Update mockUpdate(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        return update;
    }

    private TelegramBookSearchResult bookResult(long id, String imageUrl) {
        return new TelegramBookSearchResult(id, "자바의 정석", "남궁성", "도우출판", imageUrl, 90, "좋은 책", 0.8);
    }

    @Test
    @DisplayName("커맨드가 /search이다")
    void getCommand_ReturnsSearch() {
        assertThat(searchCommandHandler.getCommand()).isEqualTo("/search");
    }

    @Test
    @DisplayName("argument가 null이면 안내 메시지를 반환한다")
    void handle_WhenArgumentIsNull_ReturnsGuideMessage() {
        // given
        Update update = mockUpdate(12345L);

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, null);

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("검색어를 입력해 주세요");
    }

    @Test
    @DisplayName("argument가 빈 문자열이면 안내 메시지를 반환한다")
    void handle_WhenArgumentIsBlank_ReturnsGuideMessage() {
        // given
        Update update = mockUpdate(12345L);

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, "  ");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("검색어를 입력해 주세요");
    }

    @Test
    @DisplayName("검색 결과가 없으면 결과 없음 메시지를 반환한다")
    void handle_WhenNoResults_ReturnsEmptyMessage() {
        // given
        Update update = mockUpdate(12345L);
        given(telegramBookSearchUseCase.search(any(TelegramMessageInfo.class))).willReturn(List.of());

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, "자바");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).isEqualTo("검색 결과가 없습니다.");
        then(interactionLog).should().record(TelegramInteractionLog.Type.RESPONSE, "검색 결과가 없습니다.");
    }

    @Test
    @DisplayName("이미지가 있는 도서는 SendPhoto를 반환한다")
    void handle_WhenBookHasImage_ReturnsSendPhoto() {
        // given
        Update update = mockUpdate(12345L);
        given(telegramBookSearchUseCase.search(any(TelegramMessageInfo.class)))
                .willReturn(List.of(bookResult(1L, "http://image.jpg")));

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, "자바");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(SendPhoto.class);
    }

    @Test
    @DisplayName("이미지가 없는 도서는 SendMessage를 반환한다")
    void handle_WhenBookHasNoImage_ReturnsSendMessage() {
        // given
        Update update = mockUpdate(12345L);
        given(telegramBookSearchUseCase.search(any(TelegramMessageInfo.class)))
                .willReturn(List.of(bookResult(1L, null)));

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, "자바");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isInstanceOf(SendMessage.class);
    }

    @Test
    @DisplayName("여러 도서가 있으면 도서 수만큼 응답을 반환한다")
    void handle_WhenMultipleResults_ReturnsMultipleResponses() {
        // given
        Update update = mockUpdate(12345L);
        given(telegramBookSearchUseCase.search(any(TelegramMessageInfo.class)))
                .willReturn(List.of(
                        bookResult(1L, "http://image1.jpg"),
                        bookResult(2L, null),
                        bookResult(3L, "http://image3.jpg")
                ));

        // when
        List<PartialBotApiMethod<?>> result = searchCommandHandler.handle(update, "자바");

        // then
        assertThat(result).hasSize(3);
    }
}