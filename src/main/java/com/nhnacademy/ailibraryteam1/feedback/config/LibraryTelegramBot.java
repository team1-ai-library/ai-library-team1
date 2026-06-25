package com.nhnacademy.ailibraryteam1.feedback.config;

import com.nhnacademy.ailibraryteam1.feedback.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.feedback.keyboard.TelegramKeyboardFactory;
import com.nhnacademy.ailibraryteam1.feedback.usecase.CallbackUpdateUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class LibraryTelegramBot extends TelegramLongPollingBot {
    private final TelegramBotProperties telegramBotProperties;
    private final CallbackUpdateUseCase callbackUpdateUseCase;

    @Autowired
    public LibraryTelegramBot(
            TelegramBotProperties telegramBotProperties,
            CallbackUpdateUseCase callbackUpdateUseCase
    ) {
        super(telegramBotProperties.token());
        this.telegramBotProperties = telegramBotProperties;
        this.callbackUpdateUseCase = callbackUpdateUseCase;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            sendTestBookMessage(update);
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.username();
    }

    private void handleCallbackQuery(Update update) {
        try {
            CallbackResult result = callbackUpdateUseCase.handleCallback(update);

            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(result.callbackQueryId())
                    .text(result.message())
                    .showAlert(result.isAlert())
                    .build();

            this.execute(answer);
        } catch (Exception e) {
            answerCallbackWithError(update.getCallbackQuery().getId());
        }
    }

    private void answerCallbackWithError(String callbackQueryId) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text("피드백 처리 중 오류가 발생했습니다.")
                    .showAlert(true)
                    .build();

            this.execute(answer);
        } catch (TelegramApiException e) {
            log.warn("텔레그램 에러 응답 전송 실패: {}", e.getMessage());
        }
    }

    private void sendTestBookMessage(Update update) {
        long chatId = update.getMessage().getChatId();

        SendMessage testMessage = SendMessage.builder()
                .chatId(chatId)
                .text("테스트 도서: 토비의 스프링 3.1\n")
                .replyMarkup(TelegramKeyboardFactory.createdFeedbackKeyboard(1L))
                .build();

        try {
            this.execute(testMessage);
        } catch (TelegramApiException e) {
            log.error("테스트 메시지 전송 실패: {}", e.getMessage());
        }
    }
}
