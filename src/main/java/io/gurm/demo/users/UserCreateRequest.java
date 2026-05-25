package io.gurm.demo.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 신규 사용자 등록(가입) 요청을 위한 DTO 레코드입니다.
 * 부서명(deptName) 필드가 추가되어 자동 승인 규칙의 유스케이스를 지원합니다.
 */
public record UserCreateRequest(
        @NotBlank(message = "사용자명(로그인 ID)은 필수 항목입니다.")
        @Size(max = 50, message = "사용자명은 최대 50자까지 입력 가능합니다.")
        String username,

        @NotBlank(message = "이메일은 필수 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
        String email,

        @Size(max = 20, message = "사번은 최대 20자까지 입력 가능합니다.")
        String employeeNum,

        @Size(max = 100, message = "부서명은 최대 100자까지 입력 가능합니다.")
        String deptName, // 부서명 추가

        @Size(max = 100, message = "SSO 식별자는 최대 100자까지 입력 가능합니다.")
        String ssoId,

        @Size(max = 100, message = "Knox ID는 최대 100자까지 입력 가능합니다.")
        String knoxUserId,

        @NotNull(message = "개인정보 수집 이용 동의 여부는 필수 항목입니다.")
        Boolean privacyAgreed,

        @Size(max = 20, message = "개인정보 처리방침 버전은 최대 20자까지 입력 가능합니다.")
        String privacyPolicyVersion,

        List<String> permissions
) {
}
