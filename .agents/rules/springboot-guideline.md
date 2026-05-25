---
trigger: glob
description: Core technology stack, architectural decisions, and API coding guidelines for the Spring Boot 4.x backend project. Enforces Java 25, REST/GraphQL separation, and modern library choices (Resilience4j, RestClient, JSpecify).
globs: ["**/*.java","build.gradle","settings.gradle","**/*.yml","**/*.properties"]
---

# Spring Boot Guideline

당신은 Java 프로그래밍, Spring Boot, Spring Framework, Gradle, JUnit 및 관련 Java 기술 분야의 전문가입니다.
SOLID 원칙을 준수하고 높은 응집력과 낮은 결합도를 유지하는 설계를 지향합니다.

## 1. 기술 스택 (Tech Stack)
- **Language**: Java 25 (GraalVM CE 25.x), Kotlin 미사용
- **Framework**: Spring Boot 4.0.6+ (Spring Framework 7.0.10+)
- **Build Tool**: Gradle 9.1.0+
- **Database**: PostgreSQL 18.15+ (prod, dev), H2 (local)
- **Annotation**: Lombok
- **Configuration**: YAML

## 2. 코드 스타일 및 아키텍처 (Code Style & Architecture)
- **계층 분리 (Layered Architecture)**: Controller, Service, Repository, Entity, DTO, Config 계층으로 명확히 구분합니다. 앱 계층 전반에 걸쳐 책임의 명확한 구분을 유지하십시오.
- **의존성 주입**: Lombok을 이용한 생성자 기반 의존성 주입을 원칙으로 하며, Spring의 IoC 컨테이너를 활용해 Bean 수명 주기를 관리합니다.
- **불변성 (Immutability)**: 응답 객체 등은 불변(Immutable) 객체를 지향하며, 레코드(Record), 봉인된 클래스(Sealed Class), 패턴 매칭 등 Java 25의 최신 기능을 적극 활용합니다.
- **검증 (Validation)**: 모든 데이터 검증 로직은 Service 계층에서 구현하며, Bean Validation(`@Valid` 등) 및 사용자 정의 검증자를 사용합니다.
- **오류 처리**: `@ControllerAdvice`와 `@ExceptionHandler`를 사용하여 일관된 전역 에러 처리 및 적절한 HTTP 상태 코드를 반환합니다. 의미 있는 에러 메시지를 제공하기 위해 필요시 사용자 정의 예외 클래스를 작성합니다.
- **주석 및 문서화**: 모든 코드는 팀원이 이해할 수 있도록 한국어로 주석을 작성합니다. 메모리 뱅크(지식 베이스)는 중요한 진행이 있을 때마다 업데이트되어야 합니다.
- **명명 규칙 (Naming Rule)**:
  - 클래스명: PascalCase (`UserController`)
  - 메서드 및 변수명: camelCase (`findUserById`)
  - 상수: ALL_CAPS (`MAX_RETRY_ATTEMPTS`)
- **Import 규칙**: 와일드카드(`*`) Import를 피하고, 각 클래스를 명시적으로 Import 하여 의존성의 명확성을 유지합니다.

## 3. 웹 및 API 명세 (Web & API Specification)
- **웹 스택**: Spring Web MVC, Tomcat 11.x
- **API 설계**:
  - **프론트엔드 통신**: 원칙적으로 GraphQL로 제공
  - **외부 연동 및 기타**: 파일 업로드, 표준 CRUD 및 외부 연동 기능은 RESTful API로 제공
- **RESTful API 규칙**:
  - HTTP 메서드에 맞는 어노테이션 사용 (`@GetMapping`, `@PostMapping` 등)
  - 버저닝을 위해 `@RequestMapping` 활용
  - 적절한 Content-Type 분리 (`application/json`, `multipart/form-data`)
- **JSON 및 응답(Response) 표준**:
  - **정상 응답 (Success)**: 응답 최상위에 메타 정보와 비즈니스 데이터가 철저히 격리 분리된 **래핑형 DTO `CommonResponse<T>`** 형식만 반환하도록 제한합니다. (`meta`와 `data` 키가 완전히 분리된 계층 구조)
  - **에러 응답 (Error)**: 스프링 부트 내장 스펙인 **`ProblemDetail`**을 전적으로 활용해 **국제 표준 에러 규격인 RFC 9457(Problem Details)** 형식을 무조건적으로 반환합니다.
  - Jackson 라이브러리를 사용하되, `ObjectMapper` 대신 **`JsonMapper`** 사용
- **null 처리**: `@Nullable` 대신 **JSpecify** 표준 어노테이션 사용
- **API 문서화**:
  - REST API: Springdoc OpenAPI (Swagger, OpenAPI 3.0.1 스펙 준수)
  - GraphQL API: GraphiQL

## 4. 데이터 액세스 및 ORM (Data Access & ORM)
- **ORM**: Spring Data JPA를 사용하여 적절한 엔티티 관계와 계층화를 구현합니다.
- **마이그레이션**: Flyway 또는 Liquibase를 사용하여 데이터베이스 마이그레이션을 수행합니다.
- **성능 및 확장성**:
  - 데이터베이스 인덱싱 및 쿼리 최적화
  - Spring Cache 추상화를 이용한 캐싱 전략 구현

## 5. 외부 연동 및 비동기 처리 (Integration & Async)
- **HTTP 클라이언트**: `RestClient` 및 `@HttpExchange` 계열 어노테이션을 사용하여 외부 HTTP 서비스를 호출합니다.
- **외부 연동 오류 처리**: Spring Retry 대신 **Resilience4j**를 사용합니다 (`@Retryable`, `@CircuitBreaker`, `@Bulkhead`, `@RateLimiter` 등).
- **비동기 처리**: 비차단(Non-blocking) 작업에 `@Async`와 함께 비동기 처리를 활용합니다.

## 6. 인증 및 인가 (Security)
- **보안 프레임워크**: Spring Security OAuth2 / OIDC Resource Server로 백엔드 구성
- **인증 구조 (Authentik)**: 
  - 사내 기존 SAML 서버와 연동(Identity Brokering)하여 최종적으로 애플리케이션에 OIDC/JWT 토큰 발급
  - Frontend는 Authentik을 활용한 SSO 인증 처리
  - Backend는 Authentik으로부터 JWT Token을 발급받고 검증
- **비밀번호 인코딩**: 안전한 비밀번호 인코딩 적용 (예: BCrypt)
- **CORS**: 프론트엔드 연동을 위한 적절한 CORS 구성 적용
- **보안 화이트리스트 규정**: 신규 공개(Public) API를 배포하거나 Swagger 등의 문서/모니터링 경로를 추가할 때는 **반드시 `SecurityConfig.java`에 인바운드 우회 경로(`requestMatchers().permitAll()`)를 명시적으로 등록**하여 `401/403` 차단 장애를 사전에 완벽히 예방합니다.

## 7. 로깅 및 모니터링 (Observability)
- **로깅 (Logging)**: SLF4J API 및 Logback 구현체 사용. 적절한 로그 레벨(ERROR, WARN, INFO, DEBUG)을 명확히 구현합니다.
- **모니터링 (Monitoring)**:
  - Spring Boot Actuator 활성화
  - OpenTelemetry 기반 로깅, 메트릭, 트레이싱 연동
  - Prometheus 기반 메트릭 수집
  - LGTM (Loki, Grafana, Tempo, Mimir) 스택을 통한 통합 모니터링 체계 구성

## 8. 테스트 (Testing)
- **프레임워크**: JUnit 6 기반, 도메인별 단위 및 통합 테스트 구성
- **단위 테스트**: `@ExtendWith(MockitoExtension.class)` 사용
- **통합/웹 계층 테스트**: `@SpringBootTest`, `@MockitoBean` (Mocking), `MockMvc` 활용
- **데이터 계층 테스트**: `@DataJpaTest` 사용 (실제 DB 환경 기반 테스트 선호)
- **커버리지 관리**: JaCoCo를 통해 테스트 커버리지 측정, **60% 미만 시 빌드 실패(Fail)** 처리

## 9. 빌드, 포맷팅 및 컨테이너화 (Build, Formatting & Deployment)
- **빌드 및 패키징**: Gradle을 활용하여 의존성 관리 및 실행 가능한 JAR 패키지 생성
- **환경 분리**: Spring Profiles를 이용하여 환경(local, dev, prod)별 구성을 구현하며, 타입 안전 구성 속성(`@ConfigurationProperties`)을 사용
- **코드 포맷팅**: Spotless 플러그인을 활용하여 `google-java-format` 기반의 코드 스타일 빌드 검증 수행
- **인프라**: Docker를 통한 애플리케이션 컨테이너화 및 Kubernetes 기반 클러스터링/오케스트레이션 적용