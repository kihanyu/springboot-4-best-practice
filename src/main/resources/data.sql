-- 100개의 사용자 더미 데이터 적재 SQL 스크립트
-- JPA Auto-DDL이 만드는 MappedSuperclass/boolean 제약(NOT NULL)에 완전히 부합하도록
-- 모든 INSERT 구문에 is_withdrawn을 명시적으로 추가하여 적재 시 충돌 에러를 원천 차단합니다.

-- 1. ADMIN 사용자 등록 (승인자 역할 수행)
TRUNCATE TABLE users RESTART IDENTITY CASCADE;

INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES ('admin', 'admin@gurm.io', 'EMP001', 'S/W품질팀(MX)', 'ADMIN', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "option.manage", "user.manage", "dashboard.read"]}', 'sso_admin', 'knox_admin', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 2. VIEWER 사용자 등록 (어드민에 의해 승인된 사용자)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES ('viewer_user', 'viewer@gurm.io', 'EMP002', 'S/W개발본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_viewer', 'knox_viewer', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 3. TESTER 사용자 등록 (S/W품질팀(MX) 소속 자동 승인 대상 시뮬레이션)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES ('tester_user', 'tester@gurm.io', 'EMP003', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_tester', 'knox_tester', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 4 ~ 30. 승인 완료(APPROVED)된 일반 및 에디터 사용자들 (27개)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES 
('user4', 'user4@gurm.io', 'EMP004', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_4', 'knox_4', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user5', 'user5@gurm.io', 'EMP005', '기획본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_5', 'knox_5', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user6', 'user6@gurm.io', 'EMP006', '마케팅부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_6', 'knox_6', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user7', 'user7@gurm.io', 'EMP007', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_7', 'knox_7', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user8', 'user8@gurm.io', 'EMP008', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_8', 'knox_8', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user9', 'user9@gurm.io', 'EMP009', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_9', 'knox_9', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user10', 'user10@gurm.io', 'EMP010', '기획본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_10', 'knox_10', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user11', 'user11@gurm.io', 'EMP011', '영업부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_11', 'knox_11', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user12', 'user12@gurm.io', 'EMP012', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_12', 'knox_12', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user13', 'user13@gurm.io', 'EMP013', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_13', 'knox_13', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user14', 'user14@gurm.io', 'EMP014', '인사총무팀', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_14', 'knox_14', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user15', 'user15@gurm.io', 'EMP015', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_15', 'knox_15', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user16', 'user16@gurm.io', 'EMP016', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_16', 'knox_16', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user17', 'user17@gurm.io', 'EMP017', '기획본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_17', 'knox_17', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user18', 'user18@gurm.io', 'EMP018', '기획본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_18', 'knox_18', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user19', 'user19@gurm.io', 'EMP019', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_19', 'knox_19', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user20', 'user20@gurm.io', 'EMP020', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_20', 'knox_20', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user21', 'user21@gurm.io', 'EMP021', '인사총무팀', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_21', 'knox_21', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user22', 'user22@gurm.io', 'EMP022', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_22', 'knox_22', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user23', 'user23@gurm.io', 'EMP023', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_23', 'knox_23', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user24', 'user24@gurm.io', 'EMP024', '영업부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_24', 'knox_24', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user25', 'user25@gurm.io', 'EMP025', '영업부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_25', 'knox_25', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user26', 'user26@gurm.io', 'EMP026', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_26', 'knox_26', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user27', 'user27@gurm.io', 'EMP027', 'S/W개발본부', 'EDITOR', '{"permissions": ["tc.read", "tc.write", "tc.delete", "plan.read", "plan.write", "execution.read", "execution.write", "dashboard.read"]}', 'sso_27', 'knox_27', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user28', 'user28@gurm.io', 'EMP028', '기획본부', 'VIEWER', '{"permissions": ["tc.read", "dashboard.read"]}', 'sso_28', 'knox_28', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user29', 'user29@gurm.io', 'EMP029', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_29', 'knox_29', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user30', 'user30@gurm.io', 'EMP030', 'S/W품질팀(MX)', 'TESTER', '{"permissions": ["tc.read", "execution.read", "execution.write", "dashboard.read"]}', 'sso_30', 'knox_30', 'APPROVED', TRUE, null, CURRENT_TIMESTAMP, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 31 ~ 65. 가입 승인 대기 중(PENDING)인 사용자들 (35개)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES 
('user31', 'user31@gurm.io', 'EMP031', '기획본부', 'GUEST', '{"permissions": []}', 'sso_31', 'knox_31', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user32', 'user32@gurm.io', 'EMP032', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_32', 'knox_32', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user33', 'user33@gurm.io', 'EMP033', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_33', 'knox_33', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user34', 'user34@gurm.io', 'EMP034', '기획본부', 'GUEST', '{"permissions": []}', 'sso_34', 'knox_34', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user35', 'user35@gurm.io', 'EMP035', '영업부', 'GUEST', '{"permissions": []}', 'sso_35', 'knox_35', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user36', 'user36@gurm.io', 'EMP036', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_36', 'knox_36', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user37', 'user37@gurm.io', 'EMP037', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_37', 'knox_37', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user38', 'user38@gurm.io', 'EMP038', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_38', 'knox_38', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user39', 'user39@gurm.io', 'EMP039', '영업부', 'GUEST', '{"permissions": []}', 'sso_39', 'knox_39', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user40', 'user40@gurm.io', 'EMP040', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_40', 'knox_40', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user41', 'user41@gurm.io', 'EMP041', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_41', 'knox_41', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user42', 'user42@gurm.io', 'EMP042', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_42', 'knox_42', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user43', 'user43@gurm.io', 'EMP043', '기획본부', 'GUEST', '{"permissions": []}', 'sso_43', 'knox_43', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user44', 'user44@gurm.io', 'EMP044', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_44', 'knox_44', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user45', 'user45@gurm.io', 'EMP045', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_45', 'knox_45', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user46', 'user46@gurm.io', 'EMP046', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_46', 'knox_46', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user47', 'user47@gurm.io', 'EMP047', '영업부', 'GUEST', '{"permissions": []}', 'sso_47', 'knox_47', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user48', 'user48@gurm.io', 'EMP048', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_48', 'knox_48', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user49', 'user49@gurm.io', 'EMP049', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_49', 'knox_49', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user50', 'user50@gurm.io', 'EMP050', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_50', 'knox_50', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user51', 'user51@gurm.io', 'EMP051', '영업부', 'GUEST', '{"permissions": []}', 'sso_51', 'knox_51', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user52', 'user52@gurm.io', 'EMP052', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_52', 'knox_52', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user53', 'user53@gurm.io', 'EMP053', '기획본부', 'GUEST', '{"permissions": []}', 'sso_53', 'knox_53', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user54', 'user54@gurm.io', 'EMP054', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_54', 'knox_54', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user55', 'user55@gurm.io', 'EMP055', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_55', 'knox_55', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user56', 'user56@gurm.io', 'EMP056', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_56', 'knox_56', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user57', 'user57@gurm.io', 'EMP057', '기획본부', 'GUEST', '{"permissions": []}', 'sso_57', 'knox_57', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user58', 'user58@gurm.io', 'EMP058', '영업부', 'GUEST', '{"permissions": []}', 'sso_58', 'knox_58', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user59', 'user59@gurm.io', 'EMP059', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_59', 'knox_59', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user60', 'user60@gurm.io', 'EMP060', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_60', 'knox_60', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user61', 'user61@gurm.io', 'EMP061', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_61', 'knox_61', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user62', 'user62@gurm.io', 'EMP062', '기획본부', 'GUEST', '{"permissions": []}', 'sso_62', 'knox_62', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user63', 'user63@gurm.io', 'EMP063', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_63', 'knox_63', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user64', 'user64@gurm.io', 'EMP064', '영업부', 'GUEST', '{"permissions": []}', 'sso_64', 'knox_64', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user65', 'user65@gurm.io', 'EMP065', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_65', 'knox_65', 'PENDING', FALSE, null, null, TRUE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 66 ~ 85. 가입이 거절/반려(REJECTED)된 사용자들 (20개)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, created_at, updated_at)
VALUES 
('user66', 'user66@gurm.io', 'EMP066', '기획본부', 'GUEST', '{"permissions": []}', 'sso_66', 'knox_66', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user67', 'user67@gurm.io', 'EMP067', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_67', 'knox_67', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user68', 'user68@gurm.io', 'EMP068', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_68', 'knox_68', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user69', 'user69@gurm.io', 'EMP069', '기획본부', 'GUEST', '{"permissions": []}', 'sso_69', 'knox_69', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user70', 'user70@gurm.io', 'EMP070', '영업부', 'GUEST', '{"permissions": []}', 'sso_70', 'knox_70', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user71', 'user71@gurm.io', 'EMP071', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_71', 'knox_71', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user72', 'user72@gurm.io', 'EMP072', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_72', 'knox_72', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user73', 'user73@gurm.io', 'EMP073', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_73', 'knox_73', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user74', 'user74@gurm.io', 'EMP074', '영업부', 'GUEST', '{"permissions": []}', 'sso_74', 'knox_74', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user75', 'user75@gurm.io', 'EMP075', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_75', 'knox_75', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user76', 'user76@gurm.io', 'EMP076', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_76', 'knox_76', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user77', 'user77@gurm.io', 'EMP077', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_77', 'knox_77', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user78', 'user78@gurm.io', 'EMP078', '기획본부', 'GUEST', '{"permissions": []}', 'sso_78', 'knox_78', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user79', 'user79@gurm.io', 'EMP079', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_79', 'knox_79', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user80', 'user80@gurm.io', 'EMP080', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_80', 'knox_80', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user81', 'user81@gurm.io', 'EMP081', '마케팅부', 'GUEST', '{"permissions": []}', 'sso_81', 'knox_81', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user82', 'user82@gurm.io', 'EMP082', '영업부', 'GUEST', '{"permissions": []}', 'sso_82', 'knox_82', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user83', 'user83@gurm.io', 'EMP083', '인사총무팀', 'GUEST', '{"permissions": []}', 'sso_83', 'knox_83', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user84', 'user84@gurm.io', 'EMP084', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_84', 'knox_84', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user85', 'user85@gurm.io', 'EMP085', 'S/W개발본부', 'GUEST', '{"permissions": []}', 'sso_85', 'knox_85', 'REJECTED', FALSE, null, null, FALSE, TRUE, 'v1.0', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- 86 ~ 100. 회원 탈퇴(is_withdrawn = 1) 완료된 사용자들 (15개)
INSERT INTO users (username, email, employee_num, dept_name, role, permissions, sso_id, knox_user_id, approval_status, is_auto_approved, approved_by, approved_at, is_active, privacy_agreed, privacy_policy_version, is_withdrawn, withdrawn_at, withdrawal_reason, created_at, updated_at)
VALUES 
('user86', 'user86@gurm.io', 'EMP086', 'S/W개발본부', 'VIEWER', '{"permissions": []}', 'sso_86', 'knox_86', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '개인 사정으로 인한 탈퇴', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user87', 'user87@gurm.io', 'EMP087', 'S/W개발본부', 'VIEWER', '{"permissions": []}', 'sso_87', 'knox_87', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이직으로 인한 계정 삭제', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user88', 'user88@gurm.io', 'EMP088', '기획본부', 'GUEST', '{"permissions": []}', 'sso_88', 'knox_88', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user89', 'user89@gurm.io', 'EMP089', '인사총무팀', 'VIEWER', '{"permissions": []}', 'sso_89', 'knox_89', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '서비스 불만족', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user90', 'user90@gurm.io', 'EMP090', 'S/W개발본부', 'EDITOR', '{"permissions": []}', 'sso_90', 'knox_90', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '개인정보 보호 목적', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user91', 'user91@gurm.io', 'EMP091', '마케팅부', 'VIEWER', '{"permissions": []}', 'sso_91', 'knox_91', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user92', 'user92@gurm.io', 'EMP092', '영업부', 'VIEWER', '{"permissions": []}', 'sso_92', 'knox_92', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user93', 'user93@gurm.io', 'EMP093', '인사총무팀', 'VIEWER', '{"permissions": []}', 'sso_93', 'knox_93', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user94', 'user94@gurm.io', 'EMP094', 'S/W개발본부', 'VIEWER', '{"permissions": []}', 'sso_94', 'knox_94', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user95', 'user95@gurm.io', 'EMP095', 'S/W개발본부', 'VIEWER', '{"permissions": []}', 'sso_95', 'knox_95', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user96', 'user96@gurm.io', 'EMP096', '영업부', 'VIEWER', '{"permissions": []}', 'sso_96', 'knox_96', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user97', 'user97@gurm.io', 'EMP097', '마케팅부', 'VIEWER', '{"permissions": []}', 'sso_97', 'knox_97', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user98', 'user98@gurm.io', 'EMP098', '기획본부', 'VIEWER', '{"permissions": []}', 'sso_98', 'knox_98', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user99', 'user99@gurm.io', 'EMP099', '인사총무팀', 'VIEWER', '{"permissions": []}', 'sso_99', 'knox_99', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user100', 'user100@gurm.io', 'EMP100', 'S/W개발본부', 'VIEWER', '{"permissions": []}', 'sso_100', 'knox_100', 'APPROVED', FALSE, 1, CURRENT_TIMESTAMP, FALSE, TRUE, 'v1.0', TRUE, CURRENT_TIMESTAMP, '이용 빈도 낮음', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;
