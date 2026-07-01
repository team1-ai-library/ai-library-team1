//package com.nhnacademy.ailibraryteam1.common.mcp;
//
//import io.modelcontextprotocol.client.McpSyncClient;
//import io.modelcontextprotocol.spec.McpSchema;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@Slf4j
//@SpringBootTest
//class McpToolCallIntegrationTest {
//
//    @Autowired
//    private List<McpSyncClient> mcpSyncClients;
//
//    @Autowired
//    @Qualifier("ollamaChatClient")
//    private ChatClient ollamaChatClient;
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Test 1: MCP 서버에 연결된 모든 Tool 목록 출력
//    // ─────────────────────────────────────────────────────────────────────────
//    @Test
//    @DisplayName("MCP 서버 연결 확인 및 Tool 목록 출력")
//    void shouldListAllMcpTools() {
//        log.info("========== [TEST 1] MCP Tool 목록 출력 ==========");
//
//        assertThat(mcpSyncClients).isNotEmpty();
//
//        mcpSyncClients.forEach(client -> {
//            log.info("● 서버 정보: {}", client.getServerInfo());
//
//            List<McpSchema.Tool> tools = client.listTools().tools();
//            log.info("● 사용 가능한 Tool 수: {}", tools.size());
//
//            tools.forEach(tool -> {
//                log.info("  ┌─ Tool 이름 : {}", tool.name());
//                log.info("  ├─ 설명      : {}", tool.description());
//                log.info("  └─ 입력 스키마: {}", tool.inputSchema());
//            });
//
//            assertThat(tools).isNotEmpty();
//        });
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Test 2: McpSyncClient로 searchBooks Tool 직접 호출
//    //         title 하나만 넘기면 되는 searchBooks 로 파이썬 도서 검색
//    // ─────────────────────────────────────────────────────────────────────────
//    @Test
//    @DisplayName("MCP Tool 직접 호출 - searchBooks(title=파이썬)")
//    void shouldCallMcpToolDirectly() {
//        log.info("========== [TEST 2] MCP Tool 직접 호출 ==========");
//
//        McpSyncClient client = mcpSyncClients.get(0);
//
//        McpSchema.Tool searchBooksTool = client.listTools().tools().stream()
//                .filter(t -> t.name().equals("searchBooks"))
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("searchBooks Tool을 찾을 수 없습니다."));
//
//        log.info("● 호출 대상 Tool: {}", searchBooksTool.name());
//        log.info("● 설명          : {}", searchBooksTool.description());
//        log.info("● 입력 스키마   : {}", searchBooksTool.inputSchema());
//
//        Map<String, Object> arguments = Map.of("title", "파이썬");
//        log.info("● 요청 인자     : {}", arguments);
//
//        McpSchema.CallToolResult result = client.callTool(
//                new McpSchema.CallToolRequest(searchBooksTool.name(), arguments)
//        );
//
//        log.info("● Tool 호출 완료");
//        log.info("  ├─ isError: {}", result.isError());
//        result.content().forEach(content ->
//                log.info("  └─ 응답 내용: {}", content)
//        );
//
//        assertThat(result).isNotNull();
//        assertThat(result.isError()).isFalse();
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────
//    // Test 3: ChatClient(Ollama) + MCP Tool 통합 호출
//    //         AI가 자연어 요청을 받아 필요한 MCP Tool을 선택하고 호출하는 흐름
//    // ─────────────────────────────────────────────────────────────────────────
//    @Test
//    @DisplayName("ChatClient + MCP Tool 통합 호출 - 파이썬 책 추천")
//    void shouldCallMcpToolViaChatClient() {
//        log.info("========== [TEST 3] ChatClient + MCP Tool 통합 호출 ==========");
//
//        String userMessage = "뉴욕시에서 소년이 운다 책 찾을 수 있는 도서관 알려줘";
//        log.info("● 사용자 질문: {}", userMessage);
//
//        String response = ollamaChatClient.prompt()
//                .user(userMessage)
//                .call()
//                .content();
//
//        log.info("● AI 최종 응답:\n{}", response);
//
//        assertThat(response).isNotBlank();
//    }
//}
