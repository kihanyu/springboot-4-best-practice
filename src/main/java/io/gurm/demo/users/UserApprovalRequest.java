package io.gurm.demo.users;

import jakarta.validation.constraints.NotNull;

/**
 * 관리자가 사용자의 가입 요청을 승인 또는 반려하기 위한 DTO 레코드입니다.
 */
public record UserApprovalRequest(
        @NotNull(message = "승인 상태(status)는 필수 항목입니다.")
        ApprovalStatus status,

        @NotNull(message = "승인 처리자 ID(approvedById)는 필수 항목입니다.")
        Long approvedById
) {
}
