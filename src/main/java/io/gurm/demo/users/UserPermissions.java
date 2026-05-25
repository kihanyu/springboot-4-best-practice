package io.gurm.demo.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DB의 JSON 타입 컬럼 permissions 구조를 감싸는 Wrapper 객체 클래스입니다.
 * 명세에 따라 { "permissions": [ "tc.read", ... ] } 구조로 데이터베이스와 직렬화/역직렬화됩니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissions implements Serializable {

    private List<String> permissions = new ArrayList<>();

    /**
     * 문자열 리스트로부터 UserPermissions 인스턴스를 손쉽게 생성하는 정적 팩토리 메서드입니다.
     *
     * @param permissions 퍼미션 문자열 리스트
     * @return UserPermissions 인스턴스
     */
    public static UserPermissions from(List<String> permissions) {
        return new UserPermissions(permissions != null ? new ArrayList<>(permissions) : new ArrayList<>());
    }

    /**
     * 빈 퍼미션 목록을 갖는 빈 객체를 생성합니다.
     *
     * @return 빈 UserPermissions 인스턴스
     */
    public static UserPermissions empty() {
        return new UserPermissions(new ArrayList<>());
    }
}
