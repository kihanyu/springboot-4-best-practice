package io.gurm.demo.profile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;

@Service
public class ProfileService {

    private final String applicationName;
    private final Environment environment;

    public ProfileService(
            @Value("${spring.application.name:}") String applicationName,
            Environment environment) {
        this.applicationName = applicationName;
        this.environment = environment;
    }

    public ProfileResponse getProfileInfo() {
        String[] activeProfiles = environment.getActiveProfiles();

        // 비즈니스 검증 로직: 환경변수를 읽지 못한 경우 예외 발생
        if ( ObjectUtils.isEmpty(applicationName)) {
            throw new IllegalStateException("Application name config is missing.");
        }
        if (ObjectUtils.isEmpty(activeProfiles)) {
            throw new IllegalStateException("Active profiles config is missing.");
        }

        return new ProfileResponse(applicationName, Arrays.asList(activeProfiles));
    }
}
