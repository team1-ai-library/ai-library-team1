package com.nhnacademy.ailibraryteam1;

import com.nhnacademy.ailibraryteam1.book.config.InitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InitProperties.class)
public class AiLibraryTeam1Application {

    public static void main(String[] args) {
        SpringApplication.run(AiLibraryTeam1Application.class, args);
    }

}