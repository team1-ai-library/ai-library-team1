package com.nhnacademy.ailibraryteam1.common.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpConnectionLogger implements ApplicationRunner {

    private final List<McpSyncClient> mcpSyncClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        this.mcpSyncClient.forEach(client -> {
                log.info("[McpConnectionLogger] 연결된 서버: {}", client.getServerInfo());
                log.info("[McpConnectionLogger] 사용 가능한 Tool 목록:");
                client.listTools().tools()
                        .forEach(tool -> log.info(" - {}", tool.name()));
        });
    }
}