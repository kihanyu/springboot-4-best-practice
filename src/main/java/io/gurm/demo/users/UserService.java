package io.gurm.demo.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 사용자 도메인 관련 비즈니스 요구사항 및 트랜잭션을 관리하는 서비스 클래스입니다.
 * 추가 요구사항인 부서별 자동 승인 기능("S/W품질팀(MX)")이 정밀히 구현되어 있습니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final String AUTO_APPROVAL_DEPT = "S/W품질팀(MX)";
    private final UserRepository userRepository;

    /**
     * 신규 사용자를 시스템에 등록(가입 요청)합니다.
     * 부서명이 "S/W품질팀(MX)"인 경우 자동으로 승인(APPROVED) 및 TESTER 권한이 부여됩니다.
     *
     * @param request 가입할 사용자 정보
     * @return 등록된 사용자 상세 정보
     */
    @Transactional
    public UserResponse registerUser(UserCreateRequest request) {
        log.info("신규 사용자 등록 요청 - Username: {}, Email: {}, Dept: {}", 
                request.username(), request.email(), request.deptName());

        // 1. 필수 유효성 및 약관 동의 검증
        if (Boolean.FALSE.equals(request.privacyAgreed())) {
            throw new IllegalArgumentException("개인정보 수집 및 이용 동의가 완료되어야 회원가입이 가능합니다.");
        }

        // 2. 고유 식별자 중복 검증 (Username, Email)
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자명(username)입니다: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일(email)입니다: " + request.email());
        }

        // 3. SSO ID & Knox ID 중복 사전 검증
        if (StringUtils.hasText(request.ssoId()) && userRepository.findBySsoId(request.ssoId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 SSO ID입니다: " + request.ssoId());
        }
        if (StringUtils.hasText(request.knoxUserId()) && userRepository.findByKnoxUserId(request.knoxUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 Knox ID입니다: " + request.knoxUserId());
        }

        // 4. 부서 조건에 따른 자동 승인 여부 및 초기 권한 매핑
        boolean isSWQualityDept = AUTO_APPROVAL_DEPT.equals(request.deptName());
        
        UserRole targetRole = isSWQualityDept ? UserRole.TESTER : UserRole.GUEST;
        ApprovalStatus targetStatus = isSWQualityDept ? ApprovalStatus.APPROVED : ApprovalStatus.PENDING;
        LocalDateTime approvedAtTime = isSWQualityDept ? LocalDateTime.now() : null;
        
        // 권한별 명세서 기본 퍼미션 조회 바인딩
        UserPermissions initialPermissions = UserPermissions.from(targetRole.getDefaultPermissions());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .employeeNum(request.employeeNum())
                .deptName(request.deptName())
                .ssoId(request.ssoId())
                .knoxUserId(request.knoxUserId())
                .privacyAgreed(true)
                .privacyPolicyVersion(request.privacyPolicyVersion())
                .permissions(initialPermissions)
                .role(targetRole)
                .approvalStatus(targetStatus)
                .isAutoApproved(isSWQualityDept)
                .approvedAt(approvedAtTime)
                .isActive(true)
                .isWithdrawn(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("사용자 등록 완료 (자동 승인 여부: {}) - User ID: {}", isSWQualityDept, savedUser.getUserId());
        return UserResponse.from(savedUser);
    }

    /**
     * 식별자 ID를 기준으로 사용자 상세 정보를 조회합니다.
     *
     * @param userId 조회 대상 사용자 식별자 ID
     * @return 사용자 응답 정보
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));
        return UserResponse.from(user);
    }

    /**
     * 사용자명을 기준으로 사용자 상세 정보를 조회합니다.
     *
     * @param username 조회 대상 사용자명
     * @return 사용자 응답 정보
     */
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. Username: " + username));
        return UserResponse.from(user);
    }

    /**
     * 가입 신청한 사용자를 승인하거나 반려(거절) 처리합니다.
     * 승인 처리 시 해당 역할(Role)에 매핑되는 기본 permissions 세트가 자동 부여됩니다.
     *
     * @param userId  가입 대상 사용자 ID
     * @param request 승인 결과 및 처리 관리자 정보
     * @return 변경된 사용자 상세 정보
     */
    @Transactional
    public UserResponse approveUser(Long userId, UserApprovalRequest request) {
        log.info("사용자 승인 처리 시작 - Target User ID: {}, Approver User ID: {}, Action: {}", 
                userId, request.approvedById(), request.status());

        // 1. 가입 승인을 받을 대상 사용자 조회
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("가입 대기 중인 사용자가 존재하지 않습니다. ID: " + userId));

        // 2. 승인 처리를 수행하는 관리자 정보 조회 및 검증
        User approver = userRepository.findById(request.approvedById())
                .orElseThrow(() -> new IllegalArgumentException("승인 권한이 있는 관리자가 존재하지 않습니다. ID: " + request.approvedById()));

        if (approver.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("승인 권한이 없습니다. 승인 처리는 ADMIN 권한을 가진 사용자만 가능합니다.");
        }

        // 3. 상태 변경 로직 실행
        if (request.status() == ApprovalStatus.APPROVED) {
            targetUser.approve(approver);
            log.info("사용자 가입 승인 완료 - Target User ID: {}", userId);
        } else if (request.status() == ApprovalStatus.REJECTED) {
            targetUser.reject();
            log.info("사용자 가입 반려 완료 - Target User ID: {}", userId);
        } else {
            throw new IllegalArgumentException("올바르지 않은 승인 상태 요청입니다. APPROVED 혹은 REJECTED 상태만 입력 가능합니다.");
        }

        return UserResponse.from(targetUser);
    }

    /**
     * 사용자의 탈퇴를 신청하고 활성 상태를 중단 처리합니다.
     * 탈퇴 완료 시 보안을 위해 보유 권한(permissions)은 무효화(empty)됩니다.
     *
     * @param userId 탈퇴 대상 사용자 ID
     * @param reason 탈퇴 사유
     */
    @Transactional
    public void withdrawUser(Long userId, String reason) {
        log.info("사용자 탈퇴 요청 - User ID: {}, Reason: {}", userId, reason);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. ID: " + userId));

        user.withdraw(reason);
        log.info("사용자 탈퇴 완료 - User ID: {}", userId);
    }
}
