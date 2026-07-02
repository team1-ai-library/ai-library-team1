package com.nhnacademy.ailibraryteam1.controller.rest;

import com.nhnacademy.ailibraryteam1.telegram.TelegramInteractionLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/telegram")
public class TelegramDemoController {

    private final TelegramInteractionLog interactionLog;

    public TelegramDemoController(TelegramInteractionLog interactionLog) {
        this.interactionLog = interactionLog;
    }

    @GetMapping("/recent")
    public List<TelegramInteractionLog.Entry> recent() {
        return interactionLog.recent();
    }
}