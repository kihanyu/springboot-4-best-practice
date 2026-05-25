package io.gurm.demo.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("S/W품질팀(MX) 부서 소속의 사용자가 가입 시 자동으로 TESTER 권한과 APPROVED 승인 상태가 부여된다")
    void registerUserFromSWQualityDeptAutoApproves() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "quality_tester",
                "tester@gurm.io",
                "E12345",
                "S/W품질팀(MX)", // 자동 승인 대상 부서
                "sso_tester",
                "knox_tester",
                true,
                "v1.0",
                null
        );

        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);

        User mockSavedUser = User.builder()
                .userId(1L)
                .username(request.username())
                .email(request.email())
                .employeeNum(request.employeeNum())
                .deptName(request.deptName())
                .ssoId(request.ssoId())
                .knoxUserId(request.knoxUserId())
                .privacyAgreed(true)
                .privacyPolicyVersion(request.privacyPolicyVersion())
                .permissions(UserPermissions.from(UserRole.TESTER.getDefaultPermissions()))
                .role(UserRole.TESTER)
                .approvalStatus(ApprovalStatus.APPROVED)
                .isAutoApproved(true)
                .isActive(true)
                .build();

        given(userRepository.save(any(User.class))).willReturn(mockSavedUser);

        // when
        UserResponse response = userService.registerUser(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.deptName()).isEqualTo("S/W품질팀(MX)");
        assertThat(response.role()).isEqualTo(UserRole.TESTER);
        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.isAutoApproved()).isTrue();
        assertThat(response.permissions()).containsExactlyInAnyOrder("tc.read", "execution.read", "execution.write", "dashboard.read");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getDeptName()).isEqualTo("S/W품질팀(MX)");
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.TESTER);
        assertThat(capturedUser.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(capturedUser.isAutoApproved()).isTrue();
    }

    @Test
    @DisplayName("그 외 부서의 사용자가 가입 시 대기(PENDING) 및 GUEST 상태로 등록된다")
    void registerUserFromOtherDeptStaysPending() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "developer",
                "dev@gurm.io",
                "E54321",
                "S/W개발본부", // 일반 부서
                "sso_dev",
                "knox_dev",
                true,
                "v1.0",
                null
        );

        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);

        User mockSavedUser = User.builder()
                .userId(2L)
                .username(request.username())
                .email(request.email())
                .employeeNum(request.employeeNum())
                .deptName(request.deptName())
                .ssoId(request.ssoId())
                .knoxUserId(request.knoxUserId())
                .privacyAgreed(true)
                .privacyPolicyVersion(request.privacyPolicyVersion())
                .permissions(UserPermissions.empty())
                .role(UserRole.GUEST)
                .approvalStatus(ApprovalStatus.PENDING)
                .isAutoApproved(false)
                .isActive(true)
                .build();

        given(userRepository.save(any(User.class))).willReturn(mockSavedUser);

        // when
        UserResponse response = userService.registerUser(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo(UserRole.GUEST);
        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(response.isAutoApproved()).isFalse();
        assertThat(response.permissions()).isEmpty();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getRole()).isEqualTo(UserRole.GUEST);
        assertThat(capturedUser.getApprovalStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(capturedUser.isAutoApproved()).isFalse();
    }

    @Test
    @DisplayName("개인정보 수집 동의를 하지 않고 가입 요청 시 예외를 던진다")
    void registerUserWithoutPrivacyAgreementThrowsException() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "john_doe", "john@example.com", "E123", "기획본부", null, null,
                false, "v1.0", null
        );

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("개인정보 수집 및 이용 동의가 완료되어야 회원가입이 가능합니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 사용자명(username)이 있을 경우 가입 요청 시 예외를 던진다")
    void registerUserWithDuplicateUsernameThrowsException() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "duplicate_user", "john@example.com", "E123", "기획본부", null, null,
                true, "v1.0", null
        );
        given(userRepository.existsByUsername(request.username())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 사용자명(username)입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("중복된 이메일(email)이 있을 경우 가입 요청 시 예외를 던진다")
    void registerUserWithDuplicateEmailThrowsException() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "john_doe", "duplicate@example.com", "E123", "기획본부", null, null,
                true, "v1.0", null
        );
        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 존재하는 이메일(email)입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시 예외를 던진다")
    void getUserByIdNotFoundThrowsException() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 사용자입니다. ID: 999");
    }

    @Test
    @DisplayName("ADMIN 권한이 있는 승인자가 승인 처리 요청 시 정상적으로 사용자를 APPROVED 상태로 승인하고 기본 퍼미션을 채워준다")
    void approveUserSuccess() {
        // given
        Long targetUserId = 1L;
        Long adminId = 99L;
        UserApprovalRequest request = new UserApprovalRequest(ApprovalStatus.APPROVED, adminId);

        User targetUser = User.builder()
                .userId(targetUserId)
                .username("jane")
                .role(UserRole.VIEWER) // VIEWER 권한
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        User adminApprover = User.builder()
                .userId(adminId)
                .username("admin_user")
                .role(UserRole.ADMIN)
                .build();

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.findById(adminId)).willReturn(Optional.of(adminApprover));

        // when
        UserResponse response = userService.approveUser(targetUserId, request);

        // then
        assertThat(response.approvalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        assertThat(response.approvedById()).isEqualTo(adminId);
        assertThat(response.approvedByName()).isEqualTo("admin_user");
        assertThat(response.permissions()).containsExactlyInAnyOrder("tc.read", "dashboard.read");
    }

    @Test
    @DisplayName("승인 처리자가 ADMIN 권한이 없는 일반 사용자일 경우 예외를 던진다")
    void approveUserByNonAdminThrowsException() {
        // given
        Long targetUserId = 1L;
        Long nonAdminId = 50L;
        UserApprovalRequest request = new UserApprovalRequest(ApprovalStatus.APPROVED, nonAdminId);

        User targetUser = User.builder()
                .userId(targetUserId)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        User normalUser = User.builder()
                .userId(nonAdminId)
                .username("normal_user")
                .role(UserRole.VIEWER) // 일반 뷰어
                .build();

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.findById(nonAdminId)).willReturn(Optional.of(normalUser));

        // when & then
        assertThatThrownBy(() -> userService.approveUser(targetUserId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("승인 권한이 없습니다.");
    }
}
