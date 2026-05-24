# 예시 (Examples)

4가지 원칙을 보여주는 실제 코드 예시입니다. 각 예시는 LLM이 흔히 저지르는 실수와 이를 어떻게 수정해야 하는지를 보여줍니다.

---

## 1. 코딩 전에 생각하기 (Think Before Coding)

### 예시 1: 숨겨진 가정 (Hidden Assumptions)

**사용자 요청:** "사용자 데이터를 내보내는(export) 기능을 추가해줘"

**❌ LLM의 잘못된 접근 (잘못된 가정)**

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    public String exportUsers(String format) throws IOException {
        List<User> users = userRepository.findAll();
        
        if ("json".equals(format)) {
            JsonMapper.builder().build().writeValue(new File("users.json"), users);
        } else if ("csv".equals(format)) {
            try (FileWriter writer = new FileWriter("users.csv")) {
                writer.write("id,email,name\n");
                for (User u : users) {
                    writer.write(u.getId() + "," + u.getEmail() + "," + u.getName() + "\n");
                }
            }
        }
        
        return "Exported " + users.size() + " users";
    }
}
```

**문제점:**
- 모든 사용자를 내보낸다고 가정함 (페이지네이션이나 개인정보 보호는?)
- 묻지도 않고 파일 저장 위치를 가정함
- 어떤 필드를 포함할지 임의로 가정함
- 실제 데이터 구조를 확인하지 않고 CSV 필드명을 가정함

**✅ 올바른 접근 (가정 드러내기)**

```
구현하기 전에 몇 가지 확인이 필요합니다:

1. **범위(Scope)**: 모든 사용자를 내보낼까요, 아니면 필터링된 일부만 내보낼까요? (개인정보 보호 고려)
2. **형식(Format)**: "내보내기"라고 하셨는데, 다음 중 어떤 방식인가요?
   - 브라우저에서 파일 다운로드?
   - 이메일 알림을 포함한 백그라운드 작업?
   - 데이터를 반환하는 API 엔드포인트?
3. **필드(Fields)**: 어떤 사용자 필드를 포함해야 하나요? (민감한 정보가 있을 수 있음)
4. **규모(Volume)**: 일반적으로 몇 명의 사용자 데이터를 다루나요? (구현 방식에 영향)

가장 간단한 방법은 페이지네이션 처리된 JSON을 반환하는 API 엔드포인트를 추가하는 것입니다.
파일 기반으로 내보내려면 추가 정보가 필요합니다.

어떤 방식을 선호하시나요?
```

### 예시 2: 다양한 해석의 여지 (Multiple Interpretations)

**사용자 요청:** "검색 속도를 더 빠르게 만들어줘"

**❌ LLM의 잘못된 접근 (말없이 하나를 선택함)**

```java
// Adds caching, database indexes, and async processing
// without asking which "faster" matters
@Cacheable(value = "searchResults", key = "#query")
@Async
public CompletableFuture<List<Result>> search(String query) {
    // ... 200 lines of optimization code
    return CompletableFuture.completedFuture(Collections.emptyList());
}
```

**✅ 올바른 접근**

```
"검색 속도를 더 빠르게" 한다는 것은 다음 중 하나를 의미할 수 있습니다:

1. **더 빠른 응답 시간** (500ms 대신 100ms 미만으로)
   - 데이터베이스 인덱스 추가
   - 자주 묻는 쿼리 캐싱
   - 예상 소요 시간: 2시간

2. **더 많은 동시 검색 처리** (처리량 향상)
   - 비동기 처리(async/await) 추가
   - 커넥션 풀링
   - 예상 소요 시간: 4시간

3. **더 빠르게 느껴지는 속도** (UX 향상)
   - 일부 결과를 즉시 표시
   - 프로그레시브 로딩
   - 예상 소요 시간: 3시간

현재 검색은 일반적인 쿼리에 대해 약 500ms가 소요됩니다.
사용자님의 유스케이스에서 어떤 측면이 가장 중요한가요?
```

---

## 2. 단순함 우선 (Simplicity First)

### 예시 1: 과도한 추상화 (Over-abstraction)

**사용자 요청:** "할인액을 계산하는 함수를 추가해줘"

**❌ LLM의 잘못된 접근 (과도한 엔지니어링)**

```java
public interface DiscountStrategy {
    double calculate(double amount);
}

public class PercentageDiscount implements DiscountStrategy {
    private final double percentage;
    public PercentageDiscount(double percentage) { this.percentage = percentage; }
    @Override public double calculate(double amount) { return amount * (percentage / 100.0); }
}

public class FixedDiscount implements DiscountStrategy {
    private final double fixedAmount;
    public FixedDiscount(double fixedAmount) { this.fixedAmount = fixedAmount; }
    @Override public double calculate(double amount) { return Math.min(fixedAmount, amount); }
}

public class DiscountConfig {
    private DiscountStrategy strategy;
    private double minPurchase = 0.0;
    private double maxDiscount = Double.MAX_VALUE;
    // Getters and setters omitted
}

@Service
public class DiscountCalculator {
    private final DiscountConfig config;
    
    public DiscountCalculator(DiscountConfig config) { this.config = config; }
    
    public double applyDiscount(double amount) {
        if (amount < config.getMinPurchase()) return 0;
        double discount = config.getStrategy().calculate(amount);
        return Math.min(discount, config.getMaxDiscount());
    }
}

// Usage requires 30+ lines of setup for a simple calculation
```

**✅ 올바른 접근 (단순함)**

```java
@Service
public class DiscountService {
    /** Calculate discount amount. percent should be 0-100. */
    public double calculateDiscount(double amount, double percent) {
        return amount * (percent / 100.0);
    }
}

// Usage
// double discount = discountService.calculateDiscount(100.0, 10.0); // $10 off
```

**언제 복잡성을 추가해야 하는가:** 실제로 여러 가지 할인 유형이 필요해질 때 추가합니다. 그런 요구사항이 나중에 발생하면 그때 리팩토링하세요.

### 예시 2: 추측성 기능 (Speculative Features)

**사용자 요청:** "사용자 환경설정을 데이터베이스에 저장해줘"

**❌ LLM의 잘못된 접근 (불필요한 기능 추가)**

```java
@Service
@RequiredArgsConstructor
public class PreferenceManager {
    private final JdbcTemplate db;
    private final CacheManager cache;
    private final PreferenceValidator validator;

    @Transactional
    public boolean save(Long userId, Map<String, Object> prefs, 
                        boolean merge, boolean validate, boolean notify) throws Exception {
        /* Save preferences with optional merging, validation, and notifications. */
        if (validate) {
            List<String> errors = validator.validate(prefs);
            if (!errors.isEmpty()) throw new ValidationException(errors.toString());
        }

        Map<String, Object> existing = merge ? get(userId) : new HashMap<>();
        if (merge) existing.putAll(prefs);
        Map<String, Object> finalPrefs = merge ? existing : prefs;

        db.update("UPDATE users SET preferences = ? WHERE id = ?", 
                  JsonMapper.builder().build().writeValueAsString(finalPrefs), userId);

        if (cache != null) {
            cache.getCache("prefs").put(userId, finalPrefs);
        }

        if (notify) notifyPreferenceChange(userId, finalPrefs);

        return true;
    }

    private void notifyPreferenceChange(Long userId, Map<String, Object> prefs) {
        // Another 30 lines for a feature nobody asked for
    }
    
    private Map<String, Object> get(Long userId) { return new HashMap<>(); }
}
```

**✅ 올바른 접근 (요청한 내용만)**

```java
@Repository
@RequiredArgsConstructor
public class PreferenceRepository {
    private final JdbcTemplate db;

    /** Save user preferences to database. */
    public void savePreferences(Long userId, Map<String, Object> preferences) throws Exception {
        db.update("UPDATE users SET preferences = ? WHERE id = ?",
                  JsonMapper.builder().build().writeValueAsString(preferences), userId);
    }
}
```

**필요해질 때 나중에 추가할 것:** 캐싱(성능이 중요해질 때), 유효성 검사(잘못된 데이터가 들어올 때), 병합 처리(관련 요구사항이 생길 때).

---

## 3. 국소적인 수정 (Surgical Changes)

### 예시 1: 충동적인 리팩토링 (Drive-by Refactoring)

**사용자 요청:** "빈 이메일이 들어올 때 유효성 검사기가 중단되는 버그를 수정해줘"

**❌ LLM의 잘못된 접근 (너무 많이 건드림)**

```diff
- public void validateUser(UserDto user) {
-     // Check email format
-     if (user.getEmail() == null || user.getEmail().isEmpty()) {
+ /**
+  * Validate user data.
+  */
+ public void validateUser(UserDto user) {
+     String email = user.getEmail() != null ? user.getEmail().trim() : "";
+     
+     // Validate email
+     if (email.isEmpty()) {
          throw new IllegalArgumentException("Email required");
      }
-     
-     // Basic email validation
-     if (!user.getEmail().contains("@")) {
+     if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".")) {
          throw new IllegalArgumentException("Invalid email");
      }
      
-     // Check username
-     if (user.getUsername() == null || user.getUsername().isEmpty()) {
+     // Validate username
+     String username = user.getUsername() != null ? user.getUsername().trim() : "";
+     if (username.isEmpty()) {
          throw new IllegalArgumentException("Username required");
      }
+     if (username.length() < 3) {
+         throw new IllegalArgumentException("Username too short");
+     }
+     if (!username.matches("^[a-zA-Z0-9]+$")) {
+         throw new IllegalArgumentException("Username must be alphanumeric");
+     }
  }
```

**문제점:**
- 버그 수정을 넘어서 이메일 유효성 검사 로직까지 "개선"함
- 아무도 요청하지 않은 사용자명(username) 유효성 검사를 추가함
- 주석을 수정함
- JavaDoc(docstring)을 추가함

**✅ 올바른 접근 (국소적 수정)**

```diff
  public void validateUser(UserDto user) {
      // Check email format
-     if (user.getEmail() == null || user.getEmail().isEmpty()) {
+     String email = user.getEmail();
+     if (email == null || email.trim().isEmpty()) {
          throw new IllegalArgumentException("Email required");
      }
      
      // Basic email validation
-     if (!user.getEmail().contains("@")) {
+     if (!email.contains("@")) {
          throw new IllegalArgumentException("Invalid email");
      }
      
      // Check username
      if (user.getUsername() == null || user.getUsername().isEmpty()) {
          throw new IllegalArgumentException("Username required");
      }
  }
```

**수정된 부분:** 빈 이메일 처리 오류를 해결하는 특정 줄만 수정했습니다.

### 예시 2: 스타일 일관성 깨기 (Style Drift)

**사용자 요청:** "파일 업로드 함수에 로깅을 추가해줘"

**❌ LLM의 잘못된 접근 (모두 다시 포맷팅함)**

```diff
- public boolean uploadFile(String filePath, String destination) {
+ /**
+  * Upload file to destination with logging.
+  */
+ public boolean uploadFile(String filePath, String destination) {
+     log.info("Uploading {} to {}", filePath, destination);
+     
      try {
-         byte[] data = Files.readAllBytes(Paths.get(filePath));
-         ResponseEntity<String> response = restTemplate.postForEntity(destination, new HttpEntity<>(data), String.class);
-         
-         if (response.getStatusCode() == HttpStatus.OK) {
-             return true;
-         } else {
-             return false;
-         }
+         byte[] data = Files.readAllBytes(Paths.get(filePath));
+         ResponseEntity<String> response = restTemplate.postForEntity(destination, new HttpEntity<>(data), String.class);
+         boolean success = response.getStatusCode() == HttpStatus.OK;
+         
+         if (success) {
+             log.info("Upload successful: {}", filePath);
+         } else {
+             log.error("Upload failed: {}, status={}", filePath, response.getStatusCode());
+         }
+         
+         return success;
      } catch (Exception e) {
-         System.out.println("Error: " + e.getMessage());
+         log.error("Upload error for {}: ", filePath, e);
          return false;
      }
  }
```

**문제점:**
- 따옴표 스타일을 변경함 ('' 에서 "" 로, 혹은 그 반대)
- 요청하지 않은 타입 힌트나 주석을 추가함
- JavaDoc을 추가함
- 공백 및 들여쓰기를 다시 포맷팅함
- boolean 반환 로직을 불필요하게 변경함

**✅ 올바른 접근 (기존 스타일 유지)**

```diff
+ import org.slf4j.Logger;
+ import org.slf4j.LoggerFactory;
+ 
+     private static final Logger logger = LoggerFactory.getLogger(UploadService.class);
+ 
      public boolean uploadFile(String filePath, String destination) {
+         logger.info("Starting upload: " + filePath);
          try {
              byte[] data = Files.readAllBytes(Paths.get(filePath));
              
              ResponseEntity<String> response = restClient.post()
                      .uri(destination)
                      .body(data)
                      .retrieve()
                      .toEntity(String.class);
              
              if (response.getStatusCode().is2xxSuccessful()) {
+                 logger.info("Upload successful: " + filePath);
                  return true;
              } else {
+                 logger.error("Upload failed: " + filePath + ", status=" + response.getStatusCode());
                  return false;
              }
          } catch (Exception e) {
-             System.out.println("Error: " + e.getMessage());
+             logger.error("Upload error: " + filePath, e);
              return false;
          }
      }
```

**유지된 부분:** 기존의 로직 구조와 스타일, 불필요한 포맷 변경 방지.

---

## 4. 목표 지향적 실행 (Goal-Driven Execution)

### 예시 1: 모호함 vs. 검증 가능성 (Vague vs. Verifiable)

**사용자 요청:** "인증 시스템을 고쳐줘"

**❌ LLM의 잘못된 접근 (모호한 접근 방식)**

```
인증 시스템을 다음과 같이 수정하겠습니다:
1. 코드 리뷰
2. 문제점 식별
3. 개선 사항 적용
4. 변경 사항 테스트

[명확한 성공 기준 없이 바로 수정을 진행함]
```

**✅ 올바른 접근 (검증 가능한 목표)**

```
"인증 시스템 수정"을 위해 성공 기준을 정의해야 합니다.
정확히 어떤 문제를 해결하려는 것인가요?

만약 문제가 "비밀번호 변경 후에도 사용자의 로그인이 유지되는 현상"이라면:

계획:
1. 테스트 작성: 비밀번호 변경 → 기존 세션이 무효화되는지 확인
   검증: 테스트 실패 (버그 재현)

2. 구현: 비밀번호 변경 시 세션 무효화 로직 추가
   검증: 테스트 통과

3. 엣지 케이스 확인: 다중 활성 세션, 동시 변경 등
   검증: 추가 테스트 통과

4. 회귀 테스트 확인: 기존 인증 테스트가 여전히 통과하는지 확인
   검증: 전체 테스트 스위트 통과(Green)

현재 인증 관련 테스트 커버리지: [커버리지 표시]
구체적으로 어떤 인증 문제를 겪고 계신가요?
```

### 예시 2: 검증을 동반한 다단계 작업 (Multi-Step with Verification)

**사용자 요청:** "API에 처리율 제한(Rate Limiting)을 추가해줘"

**❌ LLM의 잘못된 접근 (한 번에 모든 것을 처리)**

```python
// Redis, 여러 전략, 설정 시스템, 모니터링을 포함한 
// 완전한 처리율 제한 기능을 한 번에 300줄짜리 커밋으로 구현함.
// 명확한 검증 단계가 없음.
```

**✅ 올바른 접근 (검증을 동반한 점진적 접근)**

```
처리율 제한을 위한 계획:

1. 기본적인 인메모리 처리율 제한 추가 (단일 엔드포인트 대상)
   검증: 
   - 테스트: 100번 요청 → 처음 10번은 성공하고 나머지는 429 에러 반환
   - 수동 검증: 동일 엔드포인트에 11번 curl 요청 후 에러 발생 확인

2. 필터/인터셉터로 분리 (모든 엔드포인트에 적용)
   검증:
   - 테스트: /users 와 /posts 엔드포인트 모두에 제한 적용 확인
   - 기존 엔드포인트 테스트가 계속 통과하는지 확인

3. Redis 백엔드 추가 (다중 서버용)
   검증:
   - 테스트: 앱을 재시작해도 처리율 제한 수치가 유지됨
   - 테스트: 두 개의 앱 인스턴스가 카운터를 공유함

4. 설정 기능 추가 (엔드포인트별 다른 제한율 적용)
   검증:
   - 테스트: /search는 분당 10회, /users는 분당 100회 허용
   - 설정 파일이 올바르게 파싱됨

각 단계는 독립적으로 검증 및 배포가 가능해야 합니다.
1단계부터 시작할까요?
```

### 예시 3: 테스트 우선 검증 (Test-First Verification)

**사용자 요청:** "중복된 점수가 있을 때 정렬이 깨집니다"

**❌ LLM의 잘못된 접근 (재현 없이 바로 수정)**

```java
// Immediately changes sort logic without confirming the bug
public List<Score> sortScores(List<Score> scores) {
    return scores.stream()
            .sorted(Comparator.comparing(Score::getScore).reversed()
                              .thenComparing(Score::getName))
            .collect(Collectors.toList());
}
```

**✅ 올바른 접근 (먼저 재현하기)**

```java
// 1. First, write a test that reproduces the issue
@Test
public void testSortWithDuplicateScores() {
    // Test sorting when multiple items have same score.
    List<Score> scores = List.of(
        new Score("Alice", 100),
        new Score("Bob", 100),
        new Score("Charlie", 90)
    );
    
    List<Score> result = sortScores(new ArrayList<>(scores));
    
    // The bug: order is non-deterministic for duplicates
    // Run this test multiple times, it should be consistent
    assertEquals(100, result.get(0).getScore());
    assertEquals(100, result.get(1).getScore());
    assertEquals(90, result.get(2).getScore());
}

// Verify: Run test 10 times → fails with inconsistent ordering

// 2. Now fix with stable sort
public List<Score> sortScores(List<Score> scores) {
    // Sort by score descending, then name ascending for ties.
    return scores.stream()
            .sorted(Comparator.comparing(Score::getScore).reversed()
                              .thenComparing(Score::getName))
            .collect(Collectors.toList());
}

// Verify: Test passes consistently
```

---

## 안티패턴 요약 (Anti-Patterns Summary)

| 원칙 (Principle) | 안티패턴 (Anti-Pattern) | 해결책 (Fix) |
|-----------|-------------|-----|
| 코딩 전에 생각하기 | 파일 형식, 필드, 범위를 임의로 가정함 | 가정을 명시적으로 나열하고 명확한 요구사항을 물어봄 |
| 단순함 우선 | 단일 할인 계산을 위해 Strategy 패턴을 도입함 | 복잡성이 실제로 필요해질 때까지 단일 메서드로 유지함 |
| 국소적인 수정 | 버그를 고치면서 따옴표 스타일을 바꾸거나 불필요한 변경을 함 | 보고된 문제를 해결하는 코드 라인만 수정함 |
| 목표 지향적 | "코드를 검토하고 개선하겠습니다" | "버그 X에 대한 테스트 작성 → 통과하도록 수정 → 회귀(Regression) 없는지 검증" |

## 핵심 통찰 (Key Insight)

"과도하게 복잡한" 예시들이 명백하게 틀린 것은 아닙니다. 디자인 패턴이나 모범 사례를 따르고 있을 수도 있습니다. 하지만 진짜 문제는 **타이밍**입니다. 그들은 필요해지기 전에 미리 복잡성을 추가해버립니다. 이는 다음과 같은 결과를 낳습니다:

- 코드를 이해하기 어렵게 만듭니다.
- 더 많은 버그를 유발합니다.
- 구현하는 데 더 오랜 시간이 걸립니다.
- 테스트하기가 더 어려워집니다.

"단순한" 버전들은:
- 이해하기 더 쉽습니다.
- 구현 속도가 더 빠릅니다.
- 테스트하기 더 쉽습니다.
- 복잡성이 실제로 필요해지는 시점에 나중에 리팩토링할 수 있습니다.

**좋은 코드란 아직 오지도 않은 내일의 문제를 섣불리 해결하는 것이 아니라, 오늘의 문제를 단순하게 해결하는 코드입니다.**