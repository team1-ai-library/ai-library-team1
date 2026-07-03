# AI Library — Team 1

도서 검색 · AI 추천 · 챗봇을 하나로 묶은 AI 기반 도서관 서비스입니다.  
웹 서비스(이 프로젝트), MCP Tool 서버(`ai-library-mcp-server`), 임베딩 배치 서버(`book_embeddings`) 3개 프로젝트가 함께 동작합니다.

---

## 목차

1. [주요 기능](#주요-기능)
2. [시스템 아키텍처](#시스템-아키텍처)
3. [기술 스택](#기술-스택)
4. [프로젝트 구조](#프로젝트-구조)
5. [검색 흐름 상세](#검색-흐름-상세)
6. [API 엔드포인트](#api-엔드포인트)
7. [환경 변수 설정](#환경-변수-설정)
8. [실행 방법](#실행-방법)
9. [테스트](#테스트)

---

## 주요 기능

| 기능 | 설명 |
|---|---|
| **키워드 검색** | 제목·저자 LIKE 검색 + `book_content` PostgreSQL GIN 한국어 전문 검색 |
| **벡터 검색** | BGE-M3 임베딩 + pgvector 코사인 유사도 검색 (임계값 0.55 이상) |
| **하이브리드 검색** | 키워드·벡터 검색을 비동기로 동시 실행하고 RRF 알고리즘으로 결합 |
| **RAG 도서 추천** | 하이브리드 검색 → RRF 재랭킹 → LLM이 최대 5권 선별 + 추천 이유 생성 |
| **AI 리뷰 요약** | 도서에 리뷰가 쌓이면 Gemini가 2~3문장 요약본을 자동 생성·갱신 |
| **도서관 챗봇** | Gemini / Ollama / Local AI 중 선택해 스트리밍 대화 |
| **텔레그램 봇** | `/start` `/help` `/search` `/library` 명령어로 동일 서비스를 메신저에서 이용 |
| **개인화** | 텔레그램 피드백(좋아요/싫어요)을 벡터 평균으로 취향 프로파일을 만들어 다음 추천에 반영 |
| **시맨틱 캐시** | 질문 임베딩 간 코사인 유사도 0.98 이상이면 LLM 호출 없이 즉시 응답 (TTL 30분, 최대 100건) |
| **MCP 통합** | `ai-library-mcp-server`의 국립중앙도서관 Open API 도구 7종을 LLM이 직접 호출 |

---

## 시스템 아키텍처

```
┌────────────────────────────────────┐
│         ai-library-team1           │  ← 이 프로젝트 (웹 + 텔레그램)
│  Spring Boot + Thymeleaf           │
│  ┌──────────┐  ┌────────────────┐  │
│  │ 웹 UI    │  │  텔레그램 봇   │  │
│  └──────────┘  └────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ 검색 UseCase (KW/Vec/Hybrid) │  │
│  └──────────────────────────────┘  │
│  ┌──────────────────────────────┐  │
│  │ RAG → Gemini / Ollama / Local│  │
│  └──────────────────────────────┘  │
└──────┬──────────────────┬──────────┘
       │ pgvector 검색     │ MCP Tool 호출
       ▼                   ▼
┌─────────────┐   ┌──────────────────┐
│book_embed-  │   │ai-library-mcp-   │
│dings        │   │server            │
│(임베딩 배치) │   │(국립중앙도서관   │
│RabbitMQ 수신│   │ Open API 래핑)   │
└─────────────┘   └──────────────────┘
```

**RabbitMQ 이벤트 흐름**

```
리뷰 등록 API
    │
    ├─ library.team1.review.embedding  →  book_embeddings (임베딩 갱신)
    └─ library.team1.inner.review      →  team1 내부 (리뷰 AI 요약 생성)
```

---

## 기술 스택

### Core
- **Java 21** / **Spring Boot 3.5.15**
- **Spring AI 1.1.8** — ChatClient, EmbeddingModel, MCP Client
- **Spring Data JPA** + **QueryDSL 7.1**

### AI 모델
| 역할 | 모델 | 엔드포인트 |
|---|---|---|
| 임베딩 | BGE-M3 | `https://emb.java21.net` (OpenAI 호환) |
| LLM (기본) | Gemini 2.5 Flash | Google GenAI API |
| LLM (로컬) | qwen2.5:latest | Ollama (`http://ollama.java21.net`) |
| LLM (로컬2) | Qwen3-4B-4bit (MLX) | Local AI 서버 |

### 데이터 저장소
| 용도 | 기술 |
|---|---|
| 관계형 DB | PostgreSQL + pgvector (벡터 검색) |
| 애플리케이션 캐시 | Caffeine (인메모리) |
| 세션 / 분산 캐시 | Redis |
| 메시지 큐 | RabbitMQ (Spring AMQP) |

### Web / UI
- **Spring MVC** + **Thymeleaf** (서버사이드 렌더링)
- REST API — JSON 응답 / SSE 스트리밍 (`/api/chat`)
- **TelegramBots 6.8.0** (Long Polling)

### 개발 환경
- Lombok · Commons Lang3 / Text / CSV / IO
- JaCoCo 0.8.12 · SonarQube (코드 커버리지)
- Testcontainers (PostgreSQL + RabbitMQ 통합 테스트)
- spring-dotenv 4.0.0 (`.env` 파일 자동 로딩)

---

## 프로젝트 구조

```
src/main/java/com/nhnacademy/ailibraryteam1/
│
├── AiLibraryTeam1Application.java          # 진입점 (@EnableAsync)
│
├── book/                                   # 도서 도메인
│   ├── config/AsyncConfig.java             # 하이브리드 검색용 스레드풀
│   ├── dto/                                # BookSearchResponse, BookAiRecommendationResponse 등
│   ├── entity/Book.java                    # 도서 엔티티
│   ├── entity/BookEmbedding.java           # 벡터 임베딩 엔티티
│   ├── repository/BookQuerydslRepository   # 키워드·벡터 검색 (QueryDSL)
│   ├── search/SearchType.java              # KEYWORD / VECTOR / HYBRID
│   ├── service/BookService.java            # 단순 도서 조회
│   ├── service/BookRagService.java         # RAG 파이프라인 (임베딩→캐시→검색→LLM)
│   ├── service/RrfService.java             # RRF 알고리즘 융합
│   └── usecase/                            # 검색 유즈케이스 4종 + 상세 조회
│
├── review/                                 # 리뷰 도메인
│   ├── entity/                             # BookReview, BookReviewAiSummary, BookReviewStatistic
│   ├── service/ReviewSummarizer.java       # Gemini로 리뷰 요약 생성·갱신
│   └── usecase/                            # ReviewCreate / ReviewGet / ReviewSummarize
│
├── feedback/                               # 텔레그램 피드백 (개인화)
│   ├── entity/Feedback.java                # chatId + bookId + FeedbackType (GOOD/BAD)
│   └── service/PersonalizationService.java # 좋아요 도서 벡터 평균 → 취향 프로파일
│
├── telegram/                               # 텔레그램 봇
│   ├── LibraryTelegramBot.java             # Long Polling 봇 본체
│   ├── command/                            # Start / Help / Search / Library 핸들러
│   └── usecase/                            # TelegramBookSearchUseCase, CallbackUpdateUseCase
│
├── rabbitmq/                               # RabbitMQ 이벤트
│   ├── ReviewEmbeddingProducer.java        # 리뷰 임베딩 요청 발행
│   ├── ReviewSummaryProducer.java          # 리뷰 요약 요청 발행
│   └── ReviewSummaryCustomer.java          # 리뷰 요약 이벤트 소비 (manual ACK)
│
├── common/
│   ├── cache/SemanticCacheService.java     # 시맨틱 캐시 (코사인 유사도 기반)
│   ├── cache/CachedEmbeddingService.java   # 임베딩 결과 Caffeine 캐시
│   ├── config/                             # ChatClient(Gemini/Ollama/Local), RabbitMQ, QueryDSL 등
│   ├── exception/                          # BusinessException, ErrorCode, GlobalExceptionHandler
│   ├── init/loader/                        # 초기 도서 CSV 적재 (BookCopyService, BookInitRunner)
│   ├── mcp/McpConnectionLogger.java        # 기동 시 MCP 서버 연결 상태 로깅
│   └── util/                               # PromptTemplate, VectorConverter, CosineSimilarity
│
└── controller/
    ├── rest/BookSearchController.java      # GET /api/books/search, GET /api/books/recommend/{model}
    ├── rest/ChatController.java            # GET /api/chat (SSE 스트리밍)
    ├── rest/ReviewController.java          # GET/POST /api/books/{id}/reviews
    └── view/                               # IndexController, BookController, PresentationController
```

---

## 검색 흐름 상세

### RAG 추천 파이프라인

```
질문 입력
    │
    ▼
BGE-M3로 임베딩
    │
    ▼ 캐시 히트? (유사도 ≥ 0.98)
 ┌──YES──▶ 즉시 응답
 │
 NO
 │
 ▼
하이브리드 검색 (키워드 0.6 : 벡터 1.4 가중치)
  ├─ 키워드 검색 ─┐  CompletableFuture 병렬 실행
  └─ 벡터 검색  ─┘
    │
    ▼
RRF 융합 → 100권 후보
    │
    ▼
RRF 점수 ≥ 0.02 필터 + 상위 5권 선택
    │
    ▼
LLM 프롬프트 구성 (제목·저자·내용·평점·리뷰 요약 포함)
    │
    ▼
Gemini / Ollama / Local 중 선택해 호출
  ├─ 필요 시 MCP Tool(국립중앙도서관 API) 호출
    │
    ▼
relevance 점수 내림차순 정렬 후 반환 + 캐시 저장
```

### RRF 알고리즘

```
score(d) = keywordWeight × 1/(K + rank_keyword(d))
         + vectorWeight  × 1/(K + rank_vector(d))
K = 60  (순위 영향 완화 상수)
```

일반 하이브리드: 가중치 1.0 : 1.0  
RAG 전용: 가중치 0.6 : 1.4 (자연어 질문에서 벡터 비중 강화)

---

## API 엔드포인트

### 도서 검색

```
GET /api/books/search
  ?keyword=파이썬
  &searchType=keyword|vector|hybrid
  &page=0&size=10
```

### AI 도서 추천 (RAG)

```
GET /api/books/recommend/{model}
  ?question=공부하기 좋은 자바 책 추천해줘
  model = gemini | ollama | local
```

### 채팅 (SSE 스트리밍)

```
GET /api/chat
  ?question=...
  &model=gemini|ollama|local
```

### 리뷰

```
GET  /api/books/{book-id}/reviews        # 리뷰 목록 + 통계 조회
POST /api/books/{book-id}/reviews        # 리뷰 등록 (rating, content)
```

### 웹 페이지

```
GET /            # 도서 검색 메인
GET /books/{id}  # 도서 상세 페이지
GET /presentation/slide1.html  # 발표자료 슬라이드 (14슬라이드)
```

---

## 환경 변수 설정

프로젝트 루트의 `.env` 파일에 아래 값을 설정합니다.

```env
POSTGRESQL_PW=<PostgreSQL 비밀번호>
OPENAI_API_KEY=<BGE-M3 서버 API 키>
GEMINI_API_KEY=<Google Gemini API 키>
TELEGRAM_BOT_TOKEN=<텔레그램 봇 토큰>

rabbitmq-host=<RabbitMQ 호스트>
rabbitmq-port=5672
rabbitmq-username=<사용자명>
rabbitmq-password=<비밀번호>

local-ai-base-url=<Local AI 서버 주소>
```

> `spring-dotenv` 라이브러리가 `.env`를 자동으로 스프링 환경에 주입합니다.

---

## 실행 방법

### 사전 요구 사항

- Java 21
- PostgreSQL (pgvector 확장 설치 필요)
- RabbitMQ
- Redis
- Ollama (로컬 AI 사용 시)

### 빌드 및 실행

```bash
# 빌드
./mvnw clean package -DskipTests

# 실행
java -jar target/library.jar
```

기본 포트: `http://localhost:8080`

### 도서 데이터 초기 적재

`application.yaml`에서 `init.enable: true`로 설정하면 기동 시 `BOOK_DB_202112_filled.csv`를 PostgreSQL로 COPY 적재합니다. 적재 완료 후 반드시 `false`로 되돌리세요.

### MCP 서버 연동

`ai-library-mcp-server`를 `localhost:8081`에서 실행한 후 애플리케이션을 시작합니다.  
(`application.yaml`의 `spring.ai.mcp.client.sse.connections.data4library.url` 참고)

---

## 테스트

```bash
# 전체 테스트 실행
./mvnw test

# 커버리지 리포트 생성 (target/site/jacoco/index.html)
./mvnw package
```

### 테스트 구성

- **단위 테스트**: Mockito로 Service/UseCase 격리 테스트
- **통합 테스트**: Testcontainers로 PostgreSQL + RabbitMQ 실제 컨테이너 기동
- **컨트롤러 테스트**: `@WebMvcTest` + MockMvc
- **환경 변수 테스트**: `SpringBootEnvironmentVariableTest`로 필수 변수 바인딩 검증

코드 품질 분석은 SonarQube + JaCoCo로 수행합니다 (`SonarQube.png` 참고).

---

## 텔레그램 봇 명령어

| 명령어 | 설명 |
|---|---|
| `/start` | 봇 시작 및 환영 메시지 |
| `/help` | 사용 방법 안내 |
| `/search <키워드>` | RAG 기반 도서 추천 검색 |
| `/library <질문>` | 도서관 챗봇 (자유 대화) |

자연어 메시지를 그냥 보내면 자동으로 `/library` 챗봇으로 처리됩니다.  
검색 결과마다 좋아요/싫어요 버튼이 제공되며, 피드백은 이후 추천 개인화에 반영됩니다.
