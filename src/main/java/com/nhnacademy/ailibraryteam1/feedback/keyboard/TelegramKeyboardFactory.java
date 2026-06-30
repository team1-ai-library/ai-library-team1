package com.nhnacademy.ailibraryteam1.feedback.keyboard;

import com.nhnacademy.ailibraryteam1.feedback.entity.FeedbackType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TelegramKeyboardFactory {
    public static InlineKeyboardMarkup createdFeedbackKeyboard(long messageId, long bookId) {
        InlineKeyboardButton goodButton = InlineKeyboardButton.builder()
                .text("👍 좋았음")
                .callbackData("fb:" + messageId + ":" + bookId + ":GOOD")
                .build();

        InlineKeyboardButton badButton = InlineKeyboardButton.builder()
                .text("👎 별로였음")
                .callbackData("fb:" + messageId + ":" + bookId + ":BAD")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(goodButton, badButton))
                .build();
    }

    public static InlineKeyboardMarkup updatedFeedbackKeyboard(long messageId, long bookId, FeedbackType selectedType) {
        String goodText = (selectedType == FeedbackType.GOOD) ? "✅ 좋았음" : "👍 좋았음";
        String badText = (selectedType == FeedbackType.BAD) ? "✅ 별로였음" : "👎 별로였음";

        InlineKeyboardButton goodButton = InlineKeyboardButton.builder()
                .text(goodText)
                .callbackData("fb:" + messageId + ":" + bookId + ":GOOD")
                .build();

        InlineKeyboardButton badButton = InlineKeyboardButton.builder()
                .text(badText)
                .callbackData("fb:" + messageId + ":" + bookId + ":BAD")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(goodButton, badButton))
                .build();
    }
}
