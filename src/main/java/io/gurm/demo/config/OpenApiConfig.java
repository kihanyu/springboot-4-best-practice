package io.gurm.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI spring4BestPracticeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring Boot 4 Best Practice API Specification")
                        .description("스프링 부트 4 베스트 프랙티스 프로젝트의 API 명세서입니다.")
                        .version("v0.0.1"));
    }
}
