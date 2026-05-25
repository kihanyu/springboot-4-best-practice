package io.gurm.demo.users;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    @DisplayName("신규 가입 요청된 사용자를 정상적으로 승인 처리하고 역할별 기본 permissions를 바인딩한다")
    void approveUserSuccess() {
        // given
        User target = User.builder()
                .userId(1L)
                .username("jane")
                .role(UserRole.VIEWER) // VIEWER 권한
                .approvalStatus(ApprovalStatus.PENDING)
                .permissions(UserPermissions.empty()) // 대기 중엔 비어있음
                .isActive(true)
                .build();

        User admin = User.builder()
                .userId(99L)
                .username("admin")
                .role(UserRole.ADMIN)
                .build();

        // when
        target.approve(admin);

        // then
        assertThat(target.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(target.getApprovedBy()).isEqualTo(admin);
        assertThat(target.getApprovedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(target.isActive()).isTrue();
        
        // VIEWER의 기본 permissions인 tc.read, dashboard.read 가 할당되었는지 검증
        assertThat(target.getPermissions()).isNotNull();
        assertThat(target.getPermissions().getPermissions()).containsExactlyInAnyOrder("tc.read", "dashboard.read");
    }

    @Test
    @DisplayName("이미 승인 완료된 사용자를 다시 승인 시도하면 예외를 던진다")
    void approveAlreadyApprovedUserThrowsException() {
        // given
        User admin = User.builder().userId(99L).role(UserRole.ADMIN).build();
        User target = User.builder()
                .userId(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .approvedBy(admin)
                .build();

        // when & then
        assertThatThrownBy(() -> target.approve(admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 승인 완료된 사용자입니다.");
    }

    @Test
    @DisplayName("탈퇴한 사용자를 승인 시도하면 예외를 던진다")
    void approveWithdrawnUserThrowsException() {
        // given
        User admin = User.builder().userId(99L).role(UserRole.ADMIN).build();
        User target = User.builder()
                .userId(1L)
                .isWithdrawn(true)
                .build();

        // when & then
        assertThatThrownBy(() -> target.approve(admin))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("탈퇴한 사용자는 승인 처리할 수 없습니다.");
    }

    @Test
    @DisplayName("대기 상태의 사용자를 정상적으로 반려 처리하고 권한을 비운다")
    void rejectUserSuccess() {
        // given
        User target = User.builder()
                .userId(1L)
                .approvalStatus(ApprovalStatus.PENDING)
                .permissions(UserPermissions.from(List.of("tc.read")))
                .isActive(true)
                .build();

        // when
        target.reject();

        // then
        assertThat(target.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(target.isActive()).isFalse();
        assertThat(target.getPermissions().getPermissions()).isEmpty(); // 비워짐 검증
    }

    @Test
    @DisplayName("대기 상태가 아닌 사용자를 반려 시도하면 예외를 던진다")
    void rejectNonPendingUserThrowsException() {
        // given
        User target = User.builder()
                .userId(1L)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build();

        // when & then
        assertThatThrownBy(target::reject)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기 상태(PENDING)의 사용자만 반려 처리할 수 있습니다.");
    }

    @Test
    @DisplayName("사용자 탈퇴 처리를 수행하면 withdrawn 플래그가 세팅되고 권한이 회수된다")
    void withdrawUserSuccess() {
        // given
        User user = User.builder()
                .userId(1L)
                .isActive(true)
                .isWithdrawn(false)
                .permissions(UserPermissions.from(List.of("tc.read")))
                .build();

        // when
        user.withdraw("개인정보 보호 차원");

        // then
        assertThat(user.isWithdrawn()).isTrue();
        assertThat(user.isActive()).isFalse();
        assertThat(user.getWithdrawnAt()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(user.getWithdrawalReason()).isEqualTo("개인정보 보호 차원");
        assertThat(user.getPermissions().getPermissions()).isEmpty(); // 권한 무효화 검증
    }

    @Test
    @DisplayName("이미 탈퇴한 사용자를 다시 탈퇴 시도하면 예외를 던진다")
    void withdrawAlreadyWithdrawnUserThrowsException() {
        // given
        User user = User.builder()
                .userId(1L)
                .isWithdrawn(true)
                .build();

        // when & then
        assertThatThrownBy(() -> user.withdraw("중복 탈퇴"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("이미 탈퇴 처리된 사용자입니다.");
    }

    @Test
    @DisplayName("정상 활성 사용자의 로그인 일시를 업데이트한다")
    void updateLastLoginSuccess() {
        // given
        User user = User.builder()
                .userId(1L)
                .isActive(true)
                .isWithdrawn(false)
                .build();

        // when
        user.updateLastLogin();

        // then
        assertThat(user.getLastLoginAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("비활성 사용자 혹은 탈퇴한 사용자가 로그인 시도 시 예외를 던진다")
    void updateLastLoginInactiveOrWithdrawnThrowsException() {
        // given
        User inactiveUser = User.builder().userId(1L).isActive(false).build();
        User withdrawnUser = User.builder().userId(2L).isActive(true).isWithdrawn(true).build();

        // when & then
        assertThatThrownBy(inactiveUser::updateLastLogin)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("활성 상태의 사용자만 로그인 처리가 가능합니다.");

        assertThatThrownBy(withdrawnUser::updateLastLogin)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("활성 상태의 사용자만 로그인 처리가 가능합니다.");
    }

    @Test
    @DisplayName("사용자 역할을 업데이트하고 승인된 사용자인 경우 기본 권한을 동기화한다")
    void updateRoleSuccess() {
        // given
        User user = User.builder()
                .userId(1L)
                .role(UserRole.VIEWER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .permissions(UserPermissions.from(UserRole.VIEWER.getDefaultPermissions()))
                .build();

        // when
        user.updateRole(UserRole.EDITOR);

        // then
        assertThat(user.getRole()).isEqualTo(UserRole.EDITOR);
        assertThat(user.getPermissions().getPermissions()).containsExactlyInAnyOrder(
                "tc.read", "tc.write", "tc.delete", 
                "plan.read", "plan.write", 
                "execution.read", "execution.write", 
                "dashboard.read"
        );
    }
}
