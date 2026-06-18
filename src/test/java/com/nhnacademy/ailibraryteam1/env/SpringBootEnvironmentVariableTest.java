package com.nhnacademy.ailibraryteam1.env;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest
@Slf4j
class SpringBootEnvironmentVariableTest {

    @Autowired
    Environment environment;

    @Test
    @DisplayName(".env 파일 환경변수 테스트 (PostgreSQL 패스워드)")
    void geminiApiKeyEnvVarTest() {

        String result = this.environment.getProperty("spring.datasource.password");
        log.info("PostgreSQL PW: {}", result);

        Assertions.assertNotNull(result, "PostgreSQL 패스워드가 null이면 안 됩니다.");
    }
}