package io.gurm.demo.users;

import io.gurm.demo.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * 사용자 정보 관리와 가입 승인 등을 담당하는 REST 컨트롤러입니다.
 */
@Tag(name = "사용자 관리 API (Users)", description = "신규 가입, 상세 조회, 가입 승인/반려 및 탈퇴 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.domain:api.gurm.io}")
    private String domain;

    @Operation(
            summary = "신규 사용자 회원가입 요청",
            description = "사용자 정보 및 개인정보 수집 이용 동의를 입력받아 대기 상태(PENDING)의 신규 사용자를 등록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "회원 가입 성공",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 파라미터 또는 고유값 중복",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "비즈니스 예외 예시",
                                    value = """
                                    {
                                      "type": "https://api.gurm.io/errors/business-error",
                                      "title": "Business Logic Violation",
                                      "status": 400,
                                      "detail": "이미 존재하는 사용자명(username)입니다: tommy",
                                      "instance": "/users",
                                      "timestamp": "2026-05-25T13:30:00.000000"
                                    }
                                    """
                            )
                    )
            )
    })
    @PostMapping
    public ResponseEntity<BaseResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.registerUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(response, "회원 가입이 요청되었습니다. 관리자 승인을 대기합니다."));
    }

    @Operation(
            summary = "사용자 ID 기준 상세 조회",
            description = "고유 식별자 ID를 기준으로 사용자 정보를 상세히 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserResponse>> getUserById(
            @Parameter(description = "조회하고자 하는 사용자 ID", required = true)
            @PathVariable("id") Long id
    ) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(BaseResponse.success(response, "사용자 정보를 정상적으로 조회했습니다."));
    }

    @Operation(
            summary = "사용자명 기준 상세 조회",
            description = "사용자명(username)을 기준으로 사용자 정보를 상세히 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 조회 성공",
                    useReturnTypeSchema = true
            )
    })
    @GetMapping(params = "username")
    public ResponseEntity<BaseResponse<UserResponse>> getUserByUsername(
            @Parameter(description = "조회하고자 하는 사용자명", required = true)
            @RequestParam("username") String username
    ) {
        UserResponse response = userService.getUserByUsername(username);
        return ResponseEntity.ok(BaseResponse.success(response, "사용자 정보를 정상적으로 조회했습니다."));
    }

    @Operation(
            summary = "사용자 가입 요청 승인 및 반려",
            description = "관리자가 대기 상태의 사용자 가입 요청을 승인(APPROVED)하거나 반려(REJECTED) 처리합니다. ADMIN 권한을 보유한 사용자만 수행할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "승인/반려 상태 변경 성공",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파라미터 또는 권한 부족",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PutMapping("/{id}/approval")
    public ResponseEntity<BaseResponse<UserResponse>> approveUser(
            @Parameter(description = "승인 처리 대상 사용자 ID", required = true)
            @PathVariable("id") Long id,
            @Valid @RequestBody UserApprovalRequest request
    ) {
        UserResponse response = userService.approveUser(id, request);
        String actionMessage = request.status() == ApprovalStatus.APPROVED ? "가입을 최종 승인했습니다." : "가입 요청을 반려했습니다.";
        return ResponseEntity.ok(BaseResponse.success(response, actionMessage));
    }

    @Operation(
            summary = "사용자 탈퇴 처리",
            description = "사용자가 회원 탈퇴를 요청합니다. 정상(is_withdrawn = 0)이었던 사용자가 탈퇴(is_withdrawn = 1)로 변경되며 비활성화됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 탈퇴 처리 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "탈퇴 응답 예시",
                                    value = """
                                    {
                                      "meta": {
                                        "success": true,
                                        "code": "SUCCESS",
                                        "message": "사용자 탈퇴 처리가 완료되었습니다.",
                                        "timestamp": "2026-05-25T13:35:00.000000"
                                      },
                                      "data": null
                                    }
                                    """
                            )
                    )
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> withdrawUser(
            @Parameter(description = "탈퇴 처리 대상 사용자 ID", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "탈퇴 사유")
            @RequestParam(value = "reason", required = false, defaultValue = "사용자 직접 탈퇴") String reason
    ) {
        userService.withdrawUser(id, reason);
        return ResponseEntity.ok(BaseResponse.success(null, "사용자 탈퇴 처리가 완료되었습니다."));
    }

    // ==================== 예외 처리 핸들러 (ProblemDetail 규격) ====================

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ProblemDetail> handleBusinessException(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setType(URI.create("https://" + domain + "/errors/business-error"));
        problemDetail.setTitle("Business Logic Violation");
        problemDetail.setInstance(URI.create("/users"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }
}
