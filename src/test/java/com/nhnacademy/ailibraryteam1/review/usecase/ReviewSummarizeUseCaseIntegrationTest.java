package com.nhnacademy.ailibraryteam1.review.usecase;

import com.nhnacademy.ailibraryteam1.book.entity.Book;
import com.nhnacademy.ailibraryteam1.book.repository.BookRepository;
import com.nhnacademy.ailibraryteam1.review.entity.BookReview;
import com.nhnacademy.ailibraryteam1.review.entity.BookReviewAiSummary;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewAiSummaryRepository;
import com.nhnacademy.ailibraryteam1.review.repository.ReviewRepository;
import com.nhnacademy.ailibraryteam1.review.service.ReviewSummarizer;
import com.nhnacademy.ailibraryteam1.support.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@IntegrationTest
class ReviewSummarizeUseCaseIntegrationTest {

    @Autowired
    private ReviewSummarizeUseCase reviewSummarizeUseCase;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewAiSummaryRepository reviewAiSummaryRepository;

    @MockitoBean
    private ReviewSummarizer reviewSummarizer;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Book savedBook;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            reviewAiSummaryRepository.deleteAll();
            reviewRepository.deleteAll();
            bookRepository.deleteAll();

            Book book = new Book(
                    "9788966263219",
                    "Volume 1",
                    "테스트 도서 제목",
                    "저자 이름",
                    "출판사",
                    LocalDate.of(2021, Month.DECEMBER, 1),
                    BigDecimal.valueOf(25000),
                    "http://example.com/image.jpg",
                    "이 책은 테스트 도서의 내용 설명입니다.",
                    "부제목",
                    LocalDate.of(2021, Month.DECEMBER, 1)
            );
            savedBook = bookRepository.save(book);

            for (int i = 1; i <= 5; i++) {
                BookReview review = BookReview.create(savedBook, "리뷰 내용 " + i, i);
                reviewRepository.save(review);
            }

            BookReviewAiSummary aiSummary = BookReviewAiSummary.create(savedBook);
            reviewAiSummaryRepository.save(aiSummary);
        });
    }

    @AfterEach
    void tearDown() {
        reviewAiSummaryRepository.deleteAll();
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("동시에 20개의 리뷰가 생성되어도, 리뷰 요약은 한 번만 생성되어야 한다.")
    void execute_ConcurrencyTest_OnlyOneThreadSucceeds() throws InterruptedException {
        // given
        given(reviewSummarizer.generateNewReviewSummary(anyString(), anyList()))
                .willAnswer(invocation -> {
                    Thread.sleep(150);
                    return "동시성 테스트로 생성된 AI 요약본입니다.";
                });

        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    reviewSummarizeUseCase.execute(savedBook.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean terminated = endLatch.await(5, TimeUnit.SECONDS);

        // then
        executorService.shutdown();

        assertThat(terminated)
                .isTrue();
        then(reviewSummarizer)
                .should(times(1))
                .generateNewReviewSummary(anyString(), anyList());
        then(reviewSummarizer)
                .should(never())
                .updateReviewSummary(anyString(), anyList());

        List<BookReviewAiSummary> summaries = reviewAiSummaryRepository.findAll();

        assertThat(summaries)
                .hasSize(1);
        assertThat(summaries.getFirst().getReviewSummary())
                .isEqualTo("동시성 테스트로 생성된 AI 요약본입니다.");
        assertThat(summaries.getFirst().isGenerating())
                .isFalse();
        assertThat(successCount.get())
                .isEqualTo(threadCount);
        assertThat(failureCount.get())
                .isZero();
    }
}
