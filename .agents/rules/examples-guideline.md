---
trigger: glob
globs: ["**/*.java"]
---

# AI Coding Guidelines & Spring Boot 4.x Best Practices

## Core Principles

1. **Think Before Coding**: Clarify assumptions before writing code.
   - ❌ Assuming scope, formats, or performance targets.
   - ✅ Ask user for specifics (e.g., "Which fields to export?", "Optimize for latency or throughput?").
2. **Simplicity First**: Implement only what is requested.
   - ❌ Speculative features (caching, complex patterns) not explicitly asked for.
   - ✅ Keep it simple. Add abstractions/features only when requirements demand them.
3. **Surgical Changes**: Restrict changes to the specific request.
   - ❌ Drive-by refactoring, style drift, unrequested validation/javadocs.
   - ✅ Modify minimum required lines. Respect existing code style.
4. **Goal-Driven Execution**: Work in verifiable steps.
   - ❌ Vague plans or huge multi-step commits without validation.
   - ✅ Define success criteria (e.g., write reproducing test -> fix -> verify).

## Spring Boot 4.x & Java 25 Snippets

**1. Records & DTOs**
```java
public record OrderResponseDto(Long orderId, String userEmail, BigDecimal totalAmount) {}
```

**2. Constructor Injection**
```java
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
}
```

**3. Virtual Threads (Concurrency)**
```java
@Async // Relies on application.yml: spring.threads.virtual.enabled=true
public void sendOrderConfirmation(Order order) { emailClient.send(order); }
```

**4. RestClient & Resilience4j**
```java
@Service
@RequiredArgsConstructor
public class PaymentGatewayClient {
    private final RestClient restClient;

    @CircuitBreaker(name = "paymentService", fallbackMethod = "fallbackPayment")
    public PaymentResponse processPayment(PaymentRequest request) {
        return restClient.post().uri("https://api.payment.com/v1/charge")
                .body(request).retrieve().body(PaymentResponse.class);
    }
    
    private PaymentResponse fallbackPayment(PaymentRequest request, Throwable t) { 
        return new PaymentResponse("FAILED", "Delay"); 
    }
}
```

**5. Modern Testing**
```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private OrderService orderService; // Use @MockitoBean in Spring Boot 4
}
```

**6. JPA Optimization**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
```

**7. API Response & Global Exception Handling (RFC 9457 ProblemDetail)**
```java
// CommonResponse.java (공통 메타-데이터 제네릭 래퍼)
public class CommonResponse<T> {
    private final Meta meta;
    private final T data;

    public CommonResponse(boolean success, String code, String message, T data) {
        this.meta = new Meta(success, code, message);
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data, String message) {
        return new CommonResponse<>(true, "SUCCESS", message, data);
    }

    public static CommonResponse<Object> error(String errorCode, String errorMessage) {
        return new CommonResponse<>(false, errorCode, errorMessage, null);
    }
    // ... Getter 및 Meta 스태틱 내부 클래스 구현
}

// GlobalExceptionHandler.java (스프링 3.0+ RFC 9457 표준 에러 처리기)
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setType(URI.create("https://api.gurm.com/errors/not-found"));
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setInstance(URI.create("/api/..."));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
```

**8. Null Safety (JSpecify)**
```java
import org.jspecify.annotations.Nullable;

@Nullable
public Product findProductOrNull(Long id) { 
    return repository.findById(id).orElse(null); 
}
```

**9. OpenAPI**
```java
@Tag(name = "Orders", description = "주문 관리 API")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Operation(summary = "주문 단건 조회")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponseDto> getOrder(@PathVariable Long id) { /* ... */ }
}
```