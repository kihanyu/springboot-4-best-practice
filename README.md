# springboot-4-best-practice

## 프로젝트 개요

Springboot 4.x 백엔드 애플리케이션을 best practice에 맞춰 구현하는 프로젝트

## 사전 요구사항 (Prerequisites)

- Java 25 (또는 호환되는 버전)
- Docker 및 Docker Compose (데이터베이스 및 모니터링 환경용)

## 빌드 방법 (Build)

Gradle Wrapper를 사용하여 프로젝트를 빌드할 수 있습니다. 테스트를 포함하여 전체 빌드를 진행합니다.

```bash
# Mac / Linux
./gradlew clean build

# Windows
gradlew.bat clean build
```
*(테스트를 생략하고 빌드하려면 `./gradlew clean build -x test` 를 사용하세요.)*

## 실행 방법 (Run)

### 1. 인프라 서비스 실행 (PostgreSQL & Grafana LGTM)

애플리케이션이 필요로 하는 데이터베이스와 모니터링 환경을 Docker Compose로 먼저 실행합니다.
*(Spring Boot 3.1+의 Docker Compose 통합 기능이 활성화되어 있다면 `bootRun` 시 자동 실행될 수 있습니다.)*

```bash
docker compose up -d
```

### 2. 애플리케이션 실행

다음 명령어를 통해 Spring Boot 애플리케이션을 실행합니다.

```bash
# Mac / Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

### 3. 주요 접속 정보
- **Application API**: `http://localhost:8080` (기본 포트)
- **Grafana (모니터링)**: `http://localhost:3000`
- **PostgreSQL (DB)**: `localhost:5432` (User: `myuser`, DB: `mydatabase`)
