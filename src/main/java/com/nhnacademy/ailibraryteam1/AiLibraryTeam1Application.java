package com.nhnacademy.ailibraryteam1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class AiLibraryTeam1Application {

    public static void main(String[] args) {
        SpringApplication.run(AiLibraryTeam1Application.class, args);
    }

}
