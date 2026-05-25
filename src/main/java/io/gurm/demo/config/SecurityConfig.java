package io.gurm.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // REST API 중심 환경이므로 CSRF 보호 비활성화
            .formLogin(AbstractHttpConfigurer::disable) // 👈 기본 폼 로그인 화면 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // 👈 HTTP Basic 인증 창 비활성화
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/profile",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
