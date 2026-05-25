package io.gurm.demo.users;

import io.gurm.demo.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 사용자 정보와 승인 상태를 관리하는 JPA 엔티티 클래스입니다.
 * 공통 BaseEntity를 상속받으며, 부서별 자동 승인 기능과 최적화된 JSON permissions 구조를 지원합니다.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "employee_num", length = 20)
    private String employeeNum;

    @Column(name = "dept_name", length = 100)
    private String deptName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.GUEST;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions")
    @Builder.Default
    private UserPermissions permissions = UserPermissions.empty();

    @Column(name = "sso_id", length = 100, unique = true)
    private String ssoId;

    @Column(name = "knox_user_id", length = 100, unique = true)
    private String knoxUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20, nullable = false)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(name = "is_auto_approved", nullable = false)
    @Builder.Default
    private boolean isAutoApproved = false;

    /**
     * 가입을 승인한 사용자 (자기 참조 FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "privacy_agreed", nullable = false)
    @Builder.Default
    private boolean privacyAgreed = false;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Column(name = "privacy_policy_version", length = 20)
    private String privacyPolicyVersion;

    @Column(name = "is_withdrawn", nullable = false)
    @Builder.Default
    private boolean isWithdrawn = false;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "withdrawal_reason", length = 500)
    private String withdrawalReason;

    @PrePersist
    protected void onUserCreate() {
        super.onCreate(); // 부모의 생성일시/수정일시 할당 로직 수행
        if (this.privacyAgreed && this.privacyAgreedAt == null) {
            this.privacyAgreedAt = getCreatedAt();
        }
    }

    // ==================== 비즈니스 도메인 로직 (Rich Domain Model) ====================

    /**
     * 사용자의 가입 요청을 승인합니다.
     * 승인이 완료되면 해당 역할(Role)의 기본 권한(permissions) 세트를 자동 부여합니다.
     *
     * @param approver 승인 처리를 수행하는 관리자 정보
     */
    public void approve(User approver) {
        if (this.isWithdrawn) {
            throw new IllegalStateException("탈퇴한 사용자는 승인 처리할 수 없습니다.");
        }
        if (this.approvalStatus == ApprovalStatus.APPROVED) {
            throw new IllegalStateException("이미 승인 완료된 사용자입니다.");
        }
        
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approver;
        this.approvedAt = LocalDateTime.now();
        this.isActive = true;

        // Role 승격에 따라 해당 역할의 디폴트 퍼미션을 동적 바인딩
        if (this.role != null) {
            this.permissions = UserPermissions.from(this.role.getDefaultPermissions());
        }
    }

    /**
     * 사용자의 가입 요청을 반려합니다.
     */
    public void reject() {
        if (this.approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException("대기 상태(PENDING)의 사용자만 반려 처리할 수 있습니다.");
        }
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.isActive = false;
        this.permissions = UserPermissions.empty(); // 권한 무효화
    }

    /**
     * 사용자가 탈퇴 처리를 요청합니다.
     *
     * @param reason 탈퇴 사유
     */
    public void withdraw(String reason) {
        if (this.isWithdrawn) {
            throw new IllegalStateException("이미 탈퇴 처리된 사용자입니다.");
        }
        this.isWithdrawn = true;
        this.isActive = false;
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = reason;
        this.permissions = UserPermissions.empty(); // 권한 회수
    }

    /**
     * 사용자의 마지막 로그인 일시를 업데이트합니다.
     */
    public void updateLastLogin() {
        if (!this.isActive || this.isWithdrawn) {
            throw new IllegalStateException("활성 상태의 사용자만 로그인 처리가 가능합니다.");
        }
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 개인정보 수집 이용에 대한 추가 동의를 처리합니다.
     *
     * @param version 동의하는 개인정보 처리방침 버전
     */
    public void agreePrivacy(String version) {
        this.privacyAgreed = true;
        this.privacyAgreedAt = LocalDateTime.now();
        this.privacyPolicyVersion = version;
    }

    /**
     * 사용자의 역할을 수동으로 변경하고, 해당 역할에 부합하는 기본 퍼미션으로 동기화합니다.
     *
     * @param newRole 새로운 역할
     */
    public void updateRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("역할은 null일 수 없습니다.");
        }
        this.role = newRole;
        if (this.approvalStatus == ApprovalStatus.APPROVED) {
            this.permissions = UserPermissions.from(newRole.getDefaultPermissions());
        }
    }
}
