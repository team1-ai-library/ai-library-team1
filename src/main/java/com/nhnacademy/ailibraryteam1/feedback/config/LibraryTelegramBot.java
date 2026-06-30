package com.nhnacademy.ailibraryteam1.feedback.config;

import com.nhnacademy.ailibraryteam1.feedback.command.TelegramCommandHandler;
import com.nhnacademy.ailibraryteam1.feedback.dto.CallbackResult;
import com.nhnacademy.ailibraryteam1.feedback.dto.TelegramCallbackInfo;
import com.nhnacademy.ailibraryteam1.feedback.usecase.CallbackUpdateUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LibraryTelegramBot extends TelegramLongPollingBot {
    private final TelegramBotProperties telegramBotProperties;
    private final CallbackUpdateUseCase callbackUpdateUseCase;
    private final Map<String, TelegramCommandHandler> handlerMap;

    @Autowired
    public LibraryTelegramBot(
            TelegramBotProperties telegramBotProperties,
            CallbackUpdateUseCase callbackUpdateUseCase,
            List<TelegramCommandHandler> handlers // 스프링 컨테이너가 빈으로 생성된 TelegramCommandHandler 목록을 주입
    ) {
        super(telegramBotProperties.token());
        this.telegramBotProperties = telegramBotProperties;
        this.callbackUpdateUseCase = callbackUpdateUseCase;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(TelegramCommandHandler::getCommand, h -> h));
        
        registerBotCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 콜백 쿼리 처리
        if (update.hasCallbackQuery()) {
            TelegramCallbackInfo info = TelegramCallbackInfo.from(update);
            handleCallbackQuery(info);
            return;
        }

        // 사용자 입력 처리
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userInput = update.getMessage().getText().trim();
            Long chatId = update.getMessage().getChatId();

            if (userInput.startsWith("/")) {
                // 사용자 입력에서 명령어와 인자 분리
                String[] parts = userInput.split("\\s+", 2);
                String command = parts[0].toLowerCase();
                String argument = parts.length > 1 ? parts[1].trim() : "";

                // 명령어에 해당하는 핸들러 탐색
                TelegramCommandHandler handler = handlerMap.get(command);

                // 핸들러 실행
                if (handler != null) {
                    executeResponses(handler.handle(update, argument));
                } else {
                    executeResponses(List.of(SendMessage.builder()
                            .chatId(chatId)
                            .text("알 수 없는 명령어입니다. `/help`를 입력해 보세요.")
                            .build()));
                }
            } else {
                // TODO: 일반 자연어 처리 로직 추가
            }
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.username();
    }

    private void executeResponses(List<PartialBotApiMethod<?>> responses) {
        if (responses == null) return;
        for (PartialBotApiMethod<?> response : responses) {
            // this.execute(response)로 해결 불가 -> instanceof로 분기 필요
            try {
                if (response instanceof SendMessage sendMessage) {
                    this.execute(sendMessage);
                } else if (response instanceof SendPhoto sendPhoto) {
                    this.execute(sendPhoto);
                }
            } catch (TelegramApiException e) {
                log.error("메시지 전송 실패: {}", e.getMessage(), e);
            }
        }
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

    private void registerBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "봇 시작 및 환영 메시지"),
                new BotCommand("help", "사용 방법 안내"),
                new BotCommand("search", "도서 RAG 추천 검색 (예: /search 자바)"),
                new BotCommand("library", "도서나루 API 호출")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
            log.info("텔레그램 봇 명령어가 성공적으로 등록되었습니다.");
        } catch (TelegramApiException e) {
            log.error("봇 명령어 등록 중 예외 발생: {}", e.getMessage(), e);
        }
    }
}
