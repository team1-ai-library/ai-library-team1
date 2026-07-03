package com.nhnacademy.ailibraryteam1.telegram.command;

import com.nhnacademy.ailibraryteam1.telegram.TelegramInteractionLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LibraryCommandHandlerTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private TelegramInteractionLog interactionLog;

    @InjectMocks
    private LibraryCommandHandler libraryCommandHandler;

    private Update mockUpdate(Long chatId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(chatId);
        return update;
    }

    private void mockChatClientCall(ChatClient chatClient, String response) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);

        given(chatClient.prompt()).willReturn(requestSpec);
        given(requestSpec.user(anyString())).willReturn(requestSpec);
        given(requestSpec.advisors(any(Consumer.class))).willReturn(requestSpec);
        given(requestSpec.call()).willReturn(callSpec);
        given(callSpec.content()).willReturn(response);
    }

    @Test
    @DisplayName("커맨드가 /library이다")
    void getCommand_ReturnsLibrary() {
        assertThat(libraryCommandHandler.getCommand()).isEqualTo("/library");
    }

    @Test
    @DisplayName("argument가 null이면 안내 메시지를 반환한다")
    void handle_WhenArgumentIsNull_ReturnsGuideMessage() {
        // given
        Update update = mockUpdate(12345L);

        // when
        List<PartialBotApiMethod<?>> result = libraryCommandHandler.handle(update, null);

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("질문을 입력해 주세요");
    }

    @Test
    @DisplayName("argument가 빈 문자열이면 안내 메시지를 반환한다")
    void handle_WhenArgumentIsBlank_ReturnsGuideMessage() {
        // given
        Update update = mockUpdate(12345L);

        // when
        List<PartialBotApiMethod<?>> result = libraryCommandHandler.handle(update, "  ");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("질문을 입력해 주세요");
    }

    @Test
    @DisplayName("정상적인 질문이면 AI 응답을 반환한다")
    void handle_WhenValidArgument_ReturnsAiResponse() {
        // given
        Update update = mockUpdate(12345L);
        mockChatClientCall(chatClient, "광주에 있는 도서관입니다.");

        // when
        List<PartialBotApiMethod<?>> result = libraryCommandHandler.handle(update, "광주 도서관 알려줘");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).isEqualTo("광주에 있는 도서관입니다.");
        then(interactionLog).should().record(TelegramInteractionLog.Type.REQUEST, "광주 도서관 알려줘");
        then(interactionLog).should().record(TelegramInteractionLog.Type.RESPONSE, "광주에 있는 도서관입니다.");
    }

    @Test
    @DisplayName("AI 응답이 null이면 오류 메시지를 반환한다")
    void handle_WhenAiResponseIsNull_ReturnsErrorMessage() {
        // given
        Update update = mockUpdate(12345L);
        mockChatClientCall(chatClient, null);

        // when
        List<PartialBotApiMethod<?>> result = libraryCommandHandler.handle(update, "광주 도서관 알려줘");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("오류가 발생했습니다");
    }

    @Test
    @DisplayName("AI 응답이 빈 문자열이면 오류 메시지를 반환한다")
    void handle_WhenAiResponseIsBlank_ReturnsErrorMessage() {
        // given
        Update update = mockUpdate(12345L);
        mockChatClientCall(chatClient, "");

        // when
        List<PartialBotApiMethod<?>> result = libraryCommandHandler.handle(update, "광주 도서관 알려줘");

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getText()).contains("오류가 발생했습니다");
    }
}