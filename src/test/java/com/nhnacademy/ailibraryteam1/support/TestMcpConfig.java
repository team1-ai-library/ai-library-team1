package com.nhnacademy.ailibraryteam1.support;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestMcpConfig {

    @Bean
    public ToolCallbackProvider mcpTools() {
        return () -> new ToolCallback[0];
    }
}
