package io.gurm.demo.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터 액세스 처리를 담당하는 JPA 리포지토리 인터페이스입니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명(로그인 ID)으로 사용자를 조회합니다.
     *
     * @param username 조회할 사용자명
     * @return 조회된 사용자의 Optional 객체
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 조회할 이메일 주소
     * @return 조회된 사용자의 Optional 객체
     */
    Optional<User> findByEmail(String email);

    /**
     * SSO 식별자(ssoId)로 사용자를 조회합니다.
     *
     * @param ssoId SSO ID
     * @return 조회된 사용자의 Optional 객체
     */
    Optional<User> findBySsoId(String ssoId);

    /**
     * Knox 사용자 ID로 사용자를 조회합니다.
     *
     * @param knoxUserId Knox ID
     * @return 조회된 사용자의 Optional 객체
     */
    Optional<User> findByKnoxUserId(String knoxUserId);

    /**
     * 사용자명이 존재하는지 확인합니다.
     *
     * @param username 중복 검증할 사용자명
     * @return 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * 이메일이 존재하는지 확인합니다.
     *
     * @param email 중복 검증할 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
}
