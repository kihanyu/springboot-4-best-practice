package io.gurm.demo.users;

import java.util.List;

/**
 * 사용자의 시스템 권한을 정의하는 Enum 클래스입니다.
 * 각 권한 수준에 부합하는 기본 세부 퍼미션(permissions) 목록을 내장합니다.
 */
public enum UserRole {
    /**
     * 게스트 (기본값) - TMS 접근 불가, 접근 권한 요청만 가능
     */
    GUEST(List.of()),

    /**
     * 뷰어 - TC 조회만 가능
     */
    VIEWER(List.of("tc.read", "dashboard.read")),

    /**
     * 테스터 - TC 조회 및 Execution 가능
     */
    TESTER(List.of("tc.read", "execution.read", "execution.write", "dashboard.read")),

    /**
     * 에디터 - TC 생성/수정/삭제 및 Execution 가능
     */
    EDITOR(List.of(
            "tc.read", "tc.write", "tc.delete", 
            "plan.read", "plan.write", 
            "execution.read", "execution.write", 
            "dashboard.read"
    )),

    /**
     * 관리자 - 모든 기능 사용 가능
     */
    ADMIN(List.of(
            "tc.read", "tc.write", "tc.delete", 
            "plan.read", "plan.write", 
            "execution.read", "execution.write", 
            "option.manage", "user.manage", 
            "dashboard.read"
    ));

    private final List<String> defaultPermissions;

    UserRole(List<String> defaultPermissions) {
        this.defaultPermissions = defaultPermissions;
    }

    /**
     * 해당 권한에 부여되는 기본 세부 퍼미션 목록을 반환합니다.
     *
     * @return 퍼미션 문자열 리스트
     */
    public List<String> getDefaultPermissions() {
        return this.defaultPermissions;
    }
}
