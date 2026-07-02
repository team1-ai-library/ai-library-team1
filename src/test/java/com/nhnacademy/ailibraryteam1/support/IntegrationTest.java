package com.nhnacademy.ailibraryteam1.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * MCP 모킹 + 테스트컨테이너 통합 테스트 환경
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootTest
@Import({TestContainerConfig.class, TestMcpConfig.class})
public @interface IntegrationTest {
    @AliasFor(annotation = SpringBootTest.class)
    String[] value() default {};

    @AliasFor(annotation = SpringBootTest.class)
    String[] properties() default {};
}
