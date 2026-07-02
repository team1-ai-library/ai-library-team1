package com.nhnacademy.ailibraryteam1.telegram.config;

import com.nhnacademy.ailibraryteam1.telegram.LibraryTelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true")
public class TelegramBotConfig {

    @Bean
    public BotSession botSession(LibraryTelegramBot libraryTelegramBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        return telegramBotsApi.registerBot(libraryTelegramBot);
    }
}
