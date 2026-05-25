package io.gurm.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 메소드 수준 인가 제어 활성화 (@PreAuthorize 등)
public class SecurityConfig {

    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    public SecurityConfig(GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter) {
        this.gatewayHeaderAuthenticationFilter = gatewayHeaderAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // REST API 중심 환경이므로 CSRF 보호 비활성화
            .formLogin(AbstractHttpConfigurer::disable) // 기본 폼 로그인 화면 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 창 비활성화
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/profile",
                    "/swagger",
                    "/swagger/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll() // Swagger 및 프로필 경로만 공개적으로 비인증 허용
                .anyRequest().authenticated() // /users/**를 포함한 모든 일반 비즈니스 API 경로는 API 게이트웨이 인증 헤더 필수!
            )
            // API 게이트웨이 인증 보장 헤더 파싱 필터를 필터체인에 연동
            .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
