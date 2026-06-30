package com.nhnacademy.ailibraryteam1.book.usecase;

import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.repository.BookQuerydslRepository;
import com.nhnacademy.ailibraryteam1.book.service.RrfService;
import com.nhnacademy.ailibraryteam1.common.annotation.UseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 키워드 검색과 벡터 검색 병렬로 실행하고, RRF로 결과를 합쳐 리턴
 * <p>
 * 일반 검색용(searchByHybrid)과 RAG 추천용(searchByHybridForRag)으로 메서드를 분리한 이유
 * -> 일반 검색은 사용자가 짧은 키워드('자바', '토비의 스프링')를 입력하니까 키워드 검색이 꽤 잘 맞음
 * -> RAG는 자연어 문장('파이썬 입문 책 추천해줘')이 그대로 들어오니까, LIKE/전문 검색 기반 키워드 검색과 매칭률이 낮음 -> 벡터 검색 비중을 높여서 보정
 */
@UseCase
@Slf4j
public class BookHybridSearchUseCase {

    private final BookQuerydslRepository bookQuerydslRepository;
    private final RrfService rrfService;
    private final Executor hybridSearchExecutor;
    private final EmbeddingModel embeddingModel;

    public BookHybridSearchUseCase(BookQuerydslRepository bookQuerydslRepository,
                                   RrfService rrfService,
                                   Executor hybridSearchExecutor,
                                   @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {

        this.bookQuerydslRepository = bookQuerydslRepository;
        this.rrfService = rrfService;
        this.hybridSearchExecutor = hybridSearchExecutor;
        this.embeddingModel = embeddingModel;
    }

    // 일반 하이브리드 검색 (키워드 : 벡터 = 1 : 1)
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByHybrid(String keyword, Pageable pageable) {

        log.info("[BookHybridSearchUseCase] 일반 하이브리드 검색 - keyword: {}", keyword);
        return this.search(keyword, pageable, 1.0, 1.0);
    }

    /**
     * RAG 전용 하이브리드 검색 (키워드 : 벡터 = 3 : 7)
     * RAG 질문은 자연어 문장이므로 키워드 검색(LIKE/전문 검색)과 매칭률이 낮으므로, 벡터 검색 비중 높여 RRF 점수가 임계값 넘도록 보정
     * <p>
     * 기존 (가중치 0.3 : 0.7)
     * -> 0.3 * 1/(60+1) + 0.7 * 1/(60+1) = 0.3*0.0164 + 0.7*0.0164 = 0.0164 * (0.3+0.7) = 0.0164
     * -> 가중치 합이 0.3 + 0.7 = 1.0이라 양쪽 모두 1위인 경우도 점수가 0.0164로 줄어들음
     * -> RRF_THRESHOLD(0.02)를 못 넘김
     * <p>
     * 가중치 0.6 : 1.4
     * -> 가중치 합은 2.0(스케일 동일), 비율은 그대로 3:7
     * -> 0.6 * 1/(60+1) + 1.4 * 1/(60+1) = (0.6+1.4) * 0.0164 = 2.0 * 0.0164 = 0.0328
     * -> 기존 1:1 가중치 때와 정확히 같은 0.0328 나옴
     * -> 즉, 키워드/벡터 모두 1위인 경우의 최대 점수가 기존과 동일하게 유지되니, RRF_THRESHOLD(0.02)와의 정합성도 그대로 보존됨
     * -> 그러면서, 키워드:벡터 영향력 비율만 3:7로 벡터 쪽에 쏠리게 됨
     */
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> searchByHybridForRag(String keyword, Pageable pageable) {

        log.info("[BookHybridSearchUseCase] RAG 전용 하이브리드 검색 - keyword: {}", keyword);
        return this.search(keyword, pageable, 0.6, 1.4);
    }

    /**
     * 실제 하이브리드 검색 공통 로직
     * 1. 키웓 검색과 벡터 검색을 비동기로 동시에 실행 (CompletableFuture)
     * 2. 두 결과를 RRF로 병합 (가중치 적용)
     * 3. 외부에서 받은 pageable 크기만큼 잘라서 리턴
     */
    private Page<BookSearchResponse> search(String keyword, Pageable pageable, double keywordWeight, double vectorWeight) {

        if (Objects.isNull(keyword) || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.SEARCH_CONDITION_REQUIRED);
        }

        // 검색 자체는 항상 100개 가져옴 (RRF 융합했을 때 충분한 후보를 확보하기 위해서)
        // 실제로 응답에 포함되는 개수는 마지막에 pageable로 잘라서 결정
        Pageable largePage = PageRequest.of(0, 100);

        // 키워드 검색 (LIKE/전문 검색)
        CompletableFuture<List<BookSearchResponse>> keywordSearchFuture = CompletableFuture.supplyAsync(() -> {
            Page<BookSearchResponse> keywordPage = this.bookQuerydslRepository.searchByKeyword(null, keyword, largePage); // 검색은 largePage로

            List<BookSearchResponse> content = (Objects.nonNull(keywordPage) && keywordPage.hasContent())
                    ? keywordPage.getContent()
                    : List.of();

            log.debug("[BookHybridSearchUseCase] 키워드 검색 완료 - {}건", content.size());
            return content;
        }, hybridSearchExecutor);

        // 벡터 검색 (질문 임베딩 모델로 변환 후 코사인 유사도 검색)
        CompletableFuture<List<BookSearchResponse>> vectorSearchFuture = CompletableFuture.supplyAsync(() -> {
            float[] queryVector = this.embeddingModel.embed(keyword); // 검색어를 벡터로 변환

            Page<BookSearchResponse> vectorPage = this.bookQuerydslRepository.searchByVector(queryVector, largePage); // 검색은 largePage로

            List<BookSearchResponse> content = (vectorPage != null && vectorPage.hasContent())
                    ? vectorPage.getContent()
                    : List.of();

            log.debug("[BookHybridSearchUseCase] 벡터 검색 완료 - {}건", content.size());
            return content;
        }, hybridSearchExecutor);

        // 두 검색이 모두 끝나면 RRF로 병합 (가중치 적용)
        CompletableFuture<List<BookSearchResponse>> resultFuture = keywordSearchFuture.thenCombineAsync(
                vectorSearchFuture,
                (keywordResults, vectorResults) -> this.rrfService.fuse(keywordResults, vectorResults, keywordWeight, vectorWeight),
                hybridSearchExecutor
        );

        // RRF 점수 내림차순으로 정렬된 전체(최대 100건) 결과
        List<BookSearchResponse> result = resultFuture.join();

        log.info("[BookHybridSearchUseCase] RRF 병합 완료 - 가중치(키워드: {}, 벡터: {}), 병합 결과: {}건", keywordWeight, vectorWeight, result.size());

        // 위에서 항상 100개를 가져왔으므로, 실제 페이지 크기만큼 여기서 잘라냄 (주입받은 pageable로)
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), result.size());

        List<BookSearchResponse> content = new ArrayList<>();

        if (start < result.size()) {
            content = result.subList(start, end);
        }

        return new PageImpl<>(content, pageable, result.size());
    }
}