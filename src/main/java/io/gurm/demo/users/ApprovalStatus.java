package io.gurm.demo.users;

/**
 * 사용자의 가입 승인 상태를 정의하는 Enum 클래스입니다.
 * 명세서에 정의된 3가지 승인 상태를 포함합니다.
 */
public enum ApprovalStatus {
    /**
     * 승인 대기 중 (기본값)
     */
    PENDING,

    /**
     * 승인 완료
     */
    APPROVED,

    /**
     * 반려됨
     */
    REJECTED
}
