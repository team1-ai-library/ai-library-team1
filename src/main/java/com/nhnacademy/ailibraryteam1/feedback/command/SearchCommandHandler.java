package com.nhnacademy.ailibraryteam1.feedback.command;

import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramBookSearchResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramMessageInfo;
import com.nhnacademy.ailibraryteam1.feedback.usecase.TelegramBookSearchUseCase;
import com.nhnacademy.ailibraryteam1.feedback.keyboard.TelegramKeyboardFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchCommandHandler implements TelegramCommandHandler {
    private final TelegramBookSearchUseCase telegramBookSearchUseCase;

    @Override
    public String getCommand() {
        return "/search";
    }

    @Override
    public List<PartialBotApiMethod<?>> handle(Update update, String argument) {
        Long chatId = update.getMessage().getChatId();

        if (argument == null || argument.isBlank()) {
            return List.of(SendMessage.builder()
                    .chatId(chatId)
                    .text("검색어를 입력해 주세요. (예: '/search 자바 도서 추천해줘')")
                    .build());
        }

        TelegramMessageInfo info = TelegramMessageInfo.from(update, argument);
        List<TelegramBookSearchResult> results = telegramBookSearchUseCase.search(info);

        if (results.isEmpty()) {
            return List.of(SendMessage.builder()
                    .chatId(chatId)
                    .text("검색 결과가 없습니다.")
                    .build());
        }

        List<PartialBotApiMethod<?>> responses = new ArrayList<>();

        results.forEach(book -> {
            String caption = formatCaption(book);

            if (book.imageUrl() == null || book.imageUrl().isBlank()) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text(caption)
                        .replyMarkup(TelegramKeyboardFactory.createdFeedbackKeyboard(book.id()))
                        .build();
                responses.add(message);
            } else {
                SendPhoto photo = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(book.imageUrl()))
                        .caption(caption)
                        .replyMarkup(TelegramKeyboardFactory.createdFeedbackKeyboard(book.id()))
                        .build();
                responses.add(photo);
            }
        });

        return responses;
    }

    private String formatCaption(TelegramBookSearchResult book) {
        return String.format("""
            제목: %s
            작가: %s
            출판사: %s
            연관도: %d%%
            취향 일치도: %s
            추천 이유: %s
            """, book.title(), book.authorName(), book.publisherName(), book.relevance(), book.getPreferencePercent(), book.reason());
    }
}
