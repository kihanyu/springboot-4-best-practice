package io.gurm.demo.users;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 정보 조회를 위한 응답 DTO 레코드입니다.
 * 부서명을 표출하고, 엔티티의 JSONPermissions 객체를 플랫한 List 형태로 매핑하여 프론트엔드 편의성을 극대화합니다.
 */
public record UserResponse(
        Long userId,
        String username,
        String email,
        String employeeNum,
        String deptName, // 부서명 추가
        UserRole role,
        List<String> permissions, // 플랫 리스트로 변환 반환
        String ssoId,
        String knoxUserId,
        ApprovalStatus approvalStatus,
        boolean isAutoApproved,
        Long approvedById,
        String approvedByName,
        LocalDateTime approvedAt,
        boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean privacyAgreed,
        LocalDateTime privacyAgreedAt,
        String privacyPolicyVersion,
        boolean isWithdrawn,
        LocalDateTime withdrawnAt,
        String withdrawalReason
) {
    /**
     * User 엔티티를 UserResponse 레코드로 매핑하는 정적 팩토리 메서드입니다.
     *
     * @param user 매핑할 User 엔티티
     * @return 변환된 UserResponse 객체
     */
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }

        Long approvedById = user.getApprovedBy() != null ? user.getApprovedBy().getUserId() : null;
        String approvedByName = user.getApprovedBy() != null ? user.getApprovedBy().getUsername() : null;
        
        // 엔티티 내부의 UserPermissions 객체를 플랫한 리스트로 변환
        List<String> flatPermissions = user.getPermissions() != null 
                ? new ArrayList<>(user.getPermissions().getPermissions()) 
                : new ArrayList<>();

        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getEmployeeNum(),
                user.getDeptName(),
                user.getRole(),
                flatPermissions,
                user.getSsoId(),
                user.getKnoxUserId(),
                user.getApprovalStatus(),
                user.isAutoApproved(),
                approvedById,
                approvedByName,
                user.getApprovedAt(),
                user.isActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isPrivacyAgreed(),
                user.getPrivacyAgreedAt(),
                user.getPrivacyPolicyVersion(),
                user.isWithdrawn(),
                user.getWithdrawnAt(),
                user.getWithdrawalReason()
        );
    }
}
