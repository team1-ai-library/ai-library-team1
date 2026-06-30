package com.nhnacademy.ailibraryteam1.controller.rest;

import com.nhnacademy.ailibraryteam1.book.dto.BookRecommendationResult;
import com.nhnacademy.ailibraryteam1.book.dto.BookSearchResponse;
import com.nhnacademy.ailibraryteam1.book.search.SearchType;
import com.nhnacademy.ailibraryteam1.book.service.BookRagService;
import com.nhnacademy.ailibraryteam1.book.usecase.BookHybridSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookKeywordSearchUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookRecommendWithDetailUseCase;
import com.nhnacademy.ailibraryteam1.book.usecase.BookVectorSearchUseCase;
import com.nhnacademy.ailibraryteam1.common.exception.BusinessException;
import com.nhnacademy.ailibraryteam1.common.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookSearchController {

    private final BookKeywordSearchUseCase bookKeywordSearchUseCase;
    private final BookVectorSearchUseCase bookVectorSearchUseCase;
    private final BookHybridSearchUseCase bookHybridSearchUseCase;
    private final BookRecommendWithDetailUseCase bookRecommendWithDetailUseCase;

    @GetMapping("search")
    public ResponseEntity<Page<BookSearchResponse>> search(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String keyword,
            @RequestParam String searchType,
            Pageable pageable
    ) {

        SearchType type = SearchType.from(searchType);

        Page<BookSearchResponse> result = switch (type) {
            case KEYWORD -> this.bookKeywordSearchUseCase.searchByKeyword(isbn, keyword, pageable);
            case VECTOR -> this.bookVectorSearchUseCase.searchByVector(keyword, pageable);
            case HYBRID -> this.bookHybridSearchUseCase.searchByHybrid(keyword, pageable);
            default -> throw new BusinessException(ErrorCode.UNSUPPORTED_SEARCH_TYPE);
        };

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(result);
    }

    /**
     * RAG 추천은 페이징 안 함.
     * 일반 검색은 15만 건 중 결과를 가져오니 페이징 필수지만,
     * RAG 추천은 상위 10권만 뽑아서 AI에게 넘기고, AI가 그 중에서 관련성 높은 최대 5권을 선별해서 반환하므로.
     */
    @GetMapping("/recommend/{model}")
    public ResponseEntity<List<BookRecommendationResult>> recommend(
            @PathVariable String model,
            @RequestParam String question,
            HttpSession session) {

        String conversationId = "web-%s".formatted(session.getId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.bookRecommendWithDetailUseCase.execute(question, model, conversationId));
    }
}