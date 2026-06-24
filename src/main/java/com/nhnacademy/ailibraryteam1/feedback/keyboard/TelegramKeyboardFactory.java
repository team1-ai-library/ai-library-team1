package com.nhnacademy.ailibraryteam1.feedback.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
public class TelegramKeyboardFactory {

    public InlineKeyboardMarkup createdFeedbackKeyboard(long bookId) {
        InlineKeyboardButton goodButton = InlineKeyboardButton.builder()
                .text("👍 좋았음")
                .callbackData("fb:" + bookId + ":GOOD")
                .build();

        InlineKeyboardButton badButton = InlineKeyboardButton.builder()
                .text("👎 별로였음")
                .callbackData("fb:" + bookId + ":BAD")
                .build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(goodButton, badButton))
                .build();
    }
}
