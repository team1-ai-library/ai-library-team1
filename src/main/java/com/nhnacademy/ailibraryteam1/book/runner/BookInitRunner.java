package com.nhnacademy.ailibraryteam1.book.runner;


import com.nhnacademy.ailibraryteam1.book.config.InitProperties;
import com.nhnacademy.ailibraryteam1.book.init.BookCopyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * run() 실행부 (애플리케이션 뜰 때마다 run() 실행됨)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class BookInitRunner implements ApplicationRunner {

    private final BookCopyService bookCopyService;
    private final InitProperties initProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        /**
         * init.enable 야멜 설정값으로 적재 로직 켤지 끌지
         */
        if (!this.initProperties.isEnable()) {
            log.info("[BookInitRunner] init.enable=false, CSV 적재를 건너뜁니다");
            return;
        }

        log.info("[BookInitRunner] 도서 CSV 적재 시작");

        // truncate + 파싱 + copy 모두 포함한 총합 시간
        long start = System.currentTimeMillis();
        this.bookCopyService.copyFromCsv(this.initProperties.getBookFile());
        long end = System.currentTimeMillis();

        log.info("[BookInitRunner] TRUNCATE + 파싱 + COPY 총 소요 시간: {} ms", end - start);
    }
}