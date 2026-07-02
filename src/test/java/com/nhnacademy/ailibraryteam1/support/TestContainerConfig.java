package com.nhnacademy.ailibraryteam1.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfig {

    private static final PostgreSQLContainer<?> POSTGRESQL = new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:pg18"))
            .withDatabaseName("test_db")
            .withInitScript("init.sql")
            .withReuse(true);

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return POSTGRESQL;
    }
}