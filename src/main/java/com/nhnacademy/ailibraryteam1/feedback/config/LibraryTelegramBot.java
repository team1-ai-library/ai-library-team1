package com.nhnacademy.ailibraryteam1.feedback.config;

import com.nhnacademy.ailibraryteam1.feedback.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramBookSearchResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramCallbackInfo;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramMessageInfo;
import com.nhnacademy.ailibraryteam1.feedback.keyboard.TelegramKeyboardFactory;
import com.nhnacademy.ailibraryteam1.feedback.usecase.CallbackUpdateUseCase;
import com.nhnacademy.ailibraryteam1.feedback.usecase.TelegramBookSearchUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class LibraryTelegramBot extends TelegramLongPollingBot {
    private final TelegramBotProperties telegramBotProperties;
    private final CallbackUpdateUseCase callbackUpdateUseCase;
    private final TelegramBookSearchUseCase telegramBookSearchUseCase;

    @Autowired
    public LibraryTelegramBot(
            TelegramBotProperties telegramBotProperties,
            CallbackUpdateUseCase callbackUpdateUseCase,
            TelegramBookSearchUseCase telegramBookSearchUseCase
    ) {
        super(telegramBotProperties.token());
        this.telegramBotProperties = telegramBotProperties;
        this.callbackUpdateUseCase = callbackUpdateUseCase;
        this.telegramBookSearchUseCase = telegramBookSearchUseCase;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            TelegramCallbackInfo info = TelegramCallbackInfo.from(update);

            handleCallbackQuery(info);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            TelegramMessageInfo info = TelegramMessageInfo.from(update);

            handleSearch(info);
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.username();
    }

    private void handleSearch(TelegramMessageInfo info) {
        List<TelegramBookSearchResult> result = telegramBookSearchUseCase.search(info);

        result.forEach(book -> {
            String caption = String.format("""
                제목: %s
                작가: %s
                출판사: %s
                연관도: %d%%
                이유: %s
                """, book.title(), book.authorName(), book.publisherName(), book.relevance(), book.reason());

            try {
                if (book.imageUrl() == null || book.imageUrl().isBlank()) {
                    SendMessage message = SendMessage.builder()
                            .chatId(info.chatId())
                            .text(caption)
                            .replyMarkup(TelegramKeyboardFactory.createdFeedbackKeyboard(book.id()))
                            .build();

                    this.execute(message);
                } else {
                    SendPhoto sendPhoto = SendPhoto.builder()
                            .chatId(info.chatId())
                            .photo(new InputFile(book.imageUrl()))
                            .caption(caption)
                            .replyMarkup(TelegramKeyboardFactory.createdFeedbackKeyboard(book.id()))
                            .build();
                    this.execute(sendPhoto);
                }
            } catch (TelegramApiException e) {
                log.error("도서 검색 중 오류 발생: {}", e.getMessage());
            }
        });
    }

    private void handleCallbackQuery(TelegramCallbackInfo info) {
        try {
            CallbackResult result = callbackUpdateUseCase.handleCallback(info);

            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(result.callbackQueryId())
                    .text(result.message())
                    .showAlert(result.isAlert())
                    .build();

            this.execute(answer);
        } catch (Exception e) {
            answerCallbackWithError(info.callbackQueryId());
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
}
