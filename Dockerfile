# Build Stage
FROM ghcr.io/graalvm/jdk-community:25 AS builder
WORKDIR /app

# Gradle 설정 및 래퍼 복사 (의존성 캐싱 극대화)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle gradle.properties ./

# gradlew 실행 권한 부여 및 의존성 사전 다운로드
# gradle.properties에 정의된 로컬 java.home 설정을 컨테이너의 JAVA_HOME으로 오버라이드합니다.
RUN chmod +x gradlew
RUN ./gradlew dependencies -Dorg.gradle.java.home=$JAVA_HOME --no-daemon || true

# 소스 코드 복사 및 빌드 수행 (테스트는 CI 파이프라인에서 수행하므로 빌드 단계에서는 제외)
COPY src/ src/
RUN ./gradlew bootJar -Dorg.gradle.java.home=$JAVA_HOME -x test --no-daemon

# Run Stage
FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app

# 보안 강화를 위한 Non-root 실행 사용자 정의
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# 빌드 산출물 중 plain jar를 제외한 실행용 jar 복사
COPY --from=builder --chown=spring:spring /app/build/libs/*[!plain].jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
