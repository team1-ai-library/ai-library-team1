package com.nhnacademy.ailibraryteam1.telegram.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class HelpCommandHandlerTest {

    private final HelpCommandHandler helpCommandHandler = new HelpCommandHandler();

    @Test
    @DisplayName("커맨드가 /help이다")
    void getCommand_ReturnsHelp() {
        assertThat(helpCommandHandler.getCommand()).isEqualTo("/help");
    }

    @Test
    @DisplayName("/help 명령어 처리 시 도움말 메시지를 반환한다")
    void handle_ReturnsHelpMessage() {
        // given
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        given(update.getMessage()).willReturn(message);
        given(message.getChatId()).willReturn(12345L);

        // when
        List<PartialBotApiMethod<?>> result = helpCommandHandler.handle(update, null);

        // then
        assertThat(result).hasSize(1);
        SendMessage sendMessage = (SendMessage) result.getFirst();
        assertThat(sendMessage.getChatId()).isEqualTo("12345");
        assertThat(sendMessage.getText()).contains("/start");
        assertThat(sendMessage.getText()).contains("/help");
        assertThat(sendMessage.getText()).contains("/search");
        assertThat(sendMessage.getText()).contains("/library");
    }
}