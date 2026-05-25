package io.gurm.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gurm.demo.users.User;
import io.gurm.demo.users.UserRepository;
import io.gurm.demo.users.UserService;
import io.gurm.demo.users.UserCreateRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * K8s API Gateway 대리 인증 결과 헤더를 기반으로 작동하는 경량 보안 필터입니다.
 * 1. X-User-Sso-Id 헤더를 스캔하여 로컬 DB 유저를 판별합니다.
 * 2. 신규 유저 진입 시 JIT 회원가입을 동적으로 유도 및 완료시킵니다.
 * 3. 로컬 DB 권한 및 세부 Permissions을 실시간으로 획득하여 SecurityContext에 충전합니다.
 */
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GatewayHeaderAuthenticationFilter(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 게이트웨이가 주입한 필수 인증 헤더 파싱
        String ssoId = request.getHeader("X-User-Sso-Id");

        // 공개 경로 및 비인증 허용 리스트는 필터에서 직접 401을 내지 않고 SecurityConfig가 판단하도록 통과시킵니다.
        String requestUri = request.getRequestURI();
        boolean isPublicPath = requestUri.equals("/profile")
                || requestUri.startsWith("/swagger")
                || requestUri.startsWith("/v3/api-docs");

        if (ssoId == null || ssoId.isBlank()) {
            if (isPublicPath) {
                // 비인가 허용 경로일 경우 다음 필터로 통과시킴
                filterChain.doFilter(request, response);
                return;
            }
            // 일반 비즈니스 API인데 게이트웨이 인증 헤더가 없다면 401 Unauthorized 응답 (RFC 9457 표준 규격)
            sendUnauthorizedError(request, response);
            return;
        }

        // 2. 로컬 DB에서 ssoId 기반 유저 조회 및 JIT 가입 처리
        User user;
        try {
            Optional<User> userOptional = userRepository.findBySsoId(ssoId);
            if (userOptional.isEmpty()) {
                // 최초 진입 시 JIT 프로비저닝 수행
                String email = request.getHeader("X-User-Email");
                if (email == null || email.isBlank()) {
                    email = ssoId + "@gurm.io"; // 폴백 이메일
                }

                String username = request.getHeader("X-User-Preferred-Username");
                if (username == null || username.isBlank()) {
                    username = "sso_user_" + (ssoId.length() > 8 ? ssoId.substring(0, 8) : ssoId); // 폴백 사용자명
                }

                // 가입 자동 승인 기준인 부서 헤더 파싱
                String department = request.getHeader("X-User-Department");
                if (department == null || department.isBlank()) {
                    department = "GUEST_DEPT"; // 폴백 부서명
                }

                String employeeNum = request.getHeader("X-User-Employee-Num");
                String knoxUserId = request.getHeader("X-User-Knox-Id");

                UserCreateRequest createRequest = new UserCreateRequest(
                        username,
                        email,
                        employeeNum,
                        department,
                        ssoId,
                        knoxUserId,
                        true, // 개인정보 약관 동의 자동 처리
                        "v1.0",
                        null
                );

                // 회원 가입 (부서가 S/W품질팀(MX)이면 자동으로 APPROVED 및 TESTER 권한 획득)
                userService.registerUser(createRequest);

                user = userRepository.findBySsoId(ssoId)
                        .orElseThrow(() -> new IllegalStateException("JIT 회원 가입 성공 후 DB 조회를 완료할 수 없습니다."));
            } else {
                user = userOptional.get();
            }
        } catch (Exception ex) {
            logger.error("API Gateway 헤더 연동 및 JIT 회원가입 중 예외 발생", ex);
            sendInternalServerError(request, response, ex.getMessage());
            return;
        }

        // 3. 로컬 DB 최신 유저 정보 기반 GrantedAuthority 목록 생성
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        if (user.getPermissions() != null && user.getPermissions().getPermissions() != null) {
            for (String permission : user.getPermissions().getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        // 4. SecurityContext에 Authentication 저장 (인가 롤 및 퍼미션 동기화)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "API Gateway 인증 자격 증명이 누락되었습니다. 요청 헤더에 유효한 X-User-Sso-Id가 필요합니다."
        );
        detail.setType(URI.create("about:blank"));
        detail.setTitle("Unauthorized");
        detail.setInstance(URI.create(request.getRequestURI()));

        objectMapper.writeValue(response.getWriter(), detail);
    }

    private void sendInternalServerError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "인증 처리 도중 서버 내부 오류가 발생했습니다: " + message
        );
        detail.setType(URI.create("about:blank"));
        detail.setTitle("Internal Server Error");
        detail.setInstance(URI.create(request.getRequestURI()));

        objectMapper.writeValue(response.getWriter(), detail);
    }
}
