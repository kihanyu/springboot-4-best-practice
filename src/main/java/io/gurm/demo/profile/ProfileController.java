package io.gurm.demo.profile;

import io.gurm.demo.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.time.LocalDateTime;

@RestController
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(
        summary = "애플리케이션 및 프로파일 정보 조회",
        description = "현재 구동 중인 스프링 부트 애플리케이션의 이름과 활성화된 프로파일(Profiles) 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공 (meta 및 data 완벽 분리)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                      "meta": {
                        "success": true,
                        "code": "SUCCESS",
                        "message": "조회 성공",
                        "timestamp": "2026-05-25T11:58:00.000000"
                      },
                      "data": {
                        "app-name": "spring4-bestpractice",
                        "profiles": ["local"]
                      }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "설정 에러 (RFC 9457 Problem Details 스펙 적용)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "에러 응답 예시 (RFC 9457)",
                    value = """
                    {
                      "type": "https://api.gurm.com/errors/config-error",
                      "title": "Configuration Error",
                      "status": 500,
                      "detail": "Active profiles config is missing.",
                      "instance": "/profile",
                      "timestamp": "2026-05-25T12:06:00.000000"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<ProfileResponse>> getProfileInfo() {
        ProfileResponse profileInfo = profileService.getProfileInfo();
        return ResponseEntity.ok(CommonResponse.success(profileInfo, "조회 성공"));
    }

    // 컨트롤러 레벨 예외 처리: 환경변수 누락 시 스프링 공식 지원 RFC 9457 ProblemDetail 규격으로 반환
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleConfigException(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()
        );
        problemDetail.setType(URI.create("https://api.gurm.com/errors/config-error"));
        problemDetail.setTitle("Configuration Error");
        problemDetail.setInstance(URI.create("/profile"));
        problemDetail.setProperty("timestamp", LocalDateTime.now()); // 추가적인 메타데이터 확장

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problemDetail);
    }
}
