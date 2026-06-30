package com.nhnacademy.ailibraryteam1.telegram.command;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface TelegramCommandHandler {
    String getCommand();
    List<PartialBotApiMethod<?>> handle(Update update, String argument);
}
