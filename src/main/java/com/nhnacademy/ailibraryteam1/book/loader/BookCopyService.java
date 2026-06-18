package com.nhnacademy.ailibraryteam1.book.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookCopyService {

    // DB 커넥션 얻어오는 표준 인터페이스
    // JPA를 통해 EntityManager나 Repository를 통해서 DB와 통신하지 않고, JPA를 완전히 우회하고 쌩 JDBC 커넥션을 직접 다룸
    // PostgreSQL의 COPY는 JPA가 지원 안 하지 않나...? 아무튼 DataSource를 직접 주입 받음
    private final DataSource dataSource;

    // 트랜잭션 분리 이유로 주입받음
    private final BookTableTruncator bookTableTruncator;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public void copyFromCsv(String csvFilePath) throws Exception {

        log.info("[BookCopyService] CSV 적재 시작: 파일: {}", csvFilePath);

        long truncateStart = System.currentTimeMillis();
        this.bookTableTruncator.truncate(); // 기존 것들 삭제 (TRUNCATE)
        long truncateEnd = System.currentTimeMillis();
        log.info("[BookCopyService] 기존 데이터 삭제 완료 - 소요 시간: {} ms", truncateEnd - truncateStart);

        int parsedCount = 0;
        long parseStart = System.currentTimeMillis();

        try (
                // classpath에서 CSV 파일을 원본 바이트 스트림으로 읽음
                InputStream rawInputStream = getClass().getClassLoader().getResourceAsStream(csvFilePath);

                // 그 스트림을 감싸서 BOM(파일 맨 앞의 인코딩 표시 바이트)을 자동으로 건너뛰도록 함
                BOMInputStream bomInputStream = BOMInputStream.builder().setInputStream(rawInputStream).get();

                // 바이트 스트림을 UTF-8 텍스트로 해석하는 문자 스트림으로 변환
                Reader reader = new InputStreamReader(bomInputStream, StandardCharsets.UTF_8);

                // 그 텍스트를 CSV 규칙(쉼표구분, 헤더 행, 따옴표 등)에 따라 한 줄씩 읽을 수 있도록 파싱
                CSVParser parser = CSVFormat.Builder.create(CSVFormat.DEFAULT)
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreHeaderCase(true)
                        .setTrim(true)
                        .get()
                        .parse(reader);

                // DB에 연결할 커넥션을 받아둠 (카피는 이 커넥션으로 실행)
                Connection connection = this.dataSource.getConnection();

                // 메모리상의 문자열 버퍼
                // CSV를 한 줄씩 읽으면서 copy에 보낼 형태로 가공한 텍스트를 여기에 쌓아둠
                StringWriter stringWriter = new StringWriter();
        ) {
            // CSV 레코드를 순회하면서 컬럼 추출
            // CSVParser에서 CSVRecord(CSV의 한 줄)씩 꺼냄
            for (CSVRecord record : parser) {
                String isbn = record.get("ISBN_THIRTEEN_NO");
                String volumeTitle = record.get("VLM_NM");
                String title = record.get("TITLE_NM").replace("\t", " ").replace("\n", " "); // COPY 텍스트 포맷이 탭과 줄바꿈으로 컬럼/행을 구분하기 때문에, 데이터 안에 그런 문자가 있으면 미리 공백으로 치환해서 포맷 깨지는 것 방지
                String authorName = record.get("AUTHR_NM");
                String publisherName = record.get("PUBLISHER_NM");
                String firstPublishDate = this.toIsoDate(record.get("PBLICTE_DE")); // 헬퍼 거쳐서 CSV의 날짜 포맷을 PostgreSQL이 알아먹을 수 있는 yyyy-MM-dd로 변환
                String price = record.get("PRC_VALUE");
                String imageUrl = record.get("IMAGE_URL");
                String bookContent = record.get("BOOK_INTRCN_CN").replace("\t", " ").replace("\n", " "); // COPY 텍스트 포맷이 탭과 줄바꿈으로 컬럼/행을 구분하기 때문에, 데이터 안에 그런 문자가 있으면 미리 공백으로 치환해서 포맷 깨지는 것 방지
                String subtitle = record.get("TITLE_SBST_NM");
                String editionPublishDate = this.toIsoDate(record.get("TWO_PBLICTE_DE"));

                // 한 줄을 COPY 텍스트 포맷으로 직렬화
                // COPY 텍스트 포맷
                // PostgreSQL의 COPY 텍스트 포맷은 "한 줄 = 한 행", "탭으로 컬럼 구분"
                // String.join("\t", ...)이 11개 컬럼 값을 탭으로 이어붙여서 한 행을 만들고, 그 뒤에 줄바꿈(\n)을 붙여서 다음 행과 구분
                // 이게 한 줄씩 누적되어, 최종적으로 stringWriter에는 157,118줄짜리 텍스트가 쌓임
                // nullToEmpty()는 각 값이 null이거나 빈 문자열이면 PostgreSQL이 NULL로 인식하는 특수 표기(\N)로 바꿔주는 역할 (이전에 날짜/가격 컬럼에서 빈 문자열을 그대로 두면 타입 에러가 났던 문제를 해결한 부분)
                stringWriter.write(String.join("\t",
                        nullToEmpty(isbn), nullToEmpty(volumeTitle), nullToEmpty(title),
                        nullToEmpty(authorName), nullToEmpty(publisherName),
                        nullToEmpty(firstPublishDate), nullToEmpty(price),
                        nullToEmpty(imageUrl), nullToEmpty(bookContent),
                        nullToEmpty(subtitle), nullToEmpty(editionPublishDate)
                ));
                stringWriter.write("\n");
                parsedCount++;

                if (parsedCount % 50000 == 0) {
                    log.info("[BookCopyService] CSV 파싱 진행 중... {}건 처리", parsedCount);
                }
            }

            // CSV를 읽어서 COPY용 텍스트로 다 변환했다는 것
            long parseEnd = System.currentTimeMillis();
            log.info("[BookCopyService] CSV 파싱 완료. 총 {}건, 소요 시간: {} ms", parsedCount, parseEnd - parseStart);

            // HikariCP 프록시를 벗기고 진짜 PostgreSQL 커넥션 꺼내기
            // this.dataSource.getConnection()으로 받은 connection은 HikariCP가 감싸놓은 프록시 객체(HikariProxyConnection)임
            // 그런데 PostgreSQL 드라이버가 제공하는 CopyManager는 COPY를 쓰기 위해 BaseConnection이라는 PostgreSQL 전용 타입을 요구함
            // 프록시는 이 타입으로 직접 캐스팅이 안 되기 때문에 unwrap() 이라는 JDBC 표준 메서드로 프록시를 벗기고 그 안의 실제 드라이버 객체를 꺼내달라고 요청하는 것임
            BaseConnection pgConnection = connection.unwrap(BaseConnection.class);
            CopyManager copyManager = new CopyManager(pgConnection);

            // COPY 테이블명(컬럼목록) FROM STDIN 은 표준입력으로 들어오는 텍스트 스트림을 이 테이블의 이 컬럼들에 그대로 적재하라는 명령임
            String sql = "COPY books(isbn, volume_title, title, author_name, publisher_name, first_publish_date, price, image_url, book_content, subtitle, edition_publish_date) " +
                    "FROM STDIN WITH (FORMAT text)";

            // copyManager.copyIn(sql, reader)가 이 명령을 실행하면서,
            // 두 번째 인자로 준 Reader(stringWriter에 쌓아둔 텍스트를 다시 읽기용 StringReader로 감싼 것)의 내용을 표준입력 스트림처럼 DB 서버로 흘려보냄
            // id 컬럼은 목록에서 빠져 있으니 테이블에 설정된 시퀀스 기본값이 자동으로 채워짐 (반환값 rows는 실제로 적재된 행 수)
            long copyStart = System.currentTimeMillis();
            long rows = copyManager.copyIn(sql, new StringReader(stringWriter.toString()));
            long copyEnd = System.currentTimeMillis();

            log.info("[BookCopyService] COPY 적재 완료. {}건, 소요 시간: {} ms", rows, copyEnd - copyStart);

            if (rows != parsedCount) {
                log.warn("[BookCopyService] 파싱 건수({})와 적재 건수({})가 일치하지 않습니다", parsedCount, rows);
            }
        }
    }

    private String toIsoDate(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String digitsOnly = raw.replaceAll("[^0-9]", "");
        if (digitsOnly.isBlank()) return "";
        try {
            return LocalDate.parse(digitsOnly, DATE_FORMATTER).toString(); // yyyy-MM-dd
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 하지만, 이렇게 하면 String 타입 컬럼(isbn, title 등)에서 원래 빈 문자열이었던 값도 NULL로 바뀌게 됨
     * 빈 문자열과 NULL을 구분해야 한다면 컬럼별로 다르게 처리해야 함
     * 지금은 속도 측정하니까 일단 무방
     */
    private String nullToEmpty(String s) {
        if (s == null || s.isBlank()) {
            return "\\N";  // PostgreSQL COPY의 NULL 표현
        }

        return s.replace("\\", "\\\\")
                .replace("\t", " ")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}