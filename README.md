# 대성 키오스크 백엔드

학원 키오스크 시스템을 위한 백엔드 API 서버

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| Framework | Spring Boot 3.5.9 / Java 17 |
| DB | MySQL 8.0 / Spring Data JPA |
| 캐시 | Redis 7 |
| 인증 | Spring Security + JWT |
| 파일 저장 | AWS S3 |
| 실시간 통신 | SSE (Server-Sent Events) |
| DB 마이그레이션 | Flyway |
| 객체 매핑 | MapStruct |
| API 문서 | SpringDoc OpenAPI (Swagger) |
| 코드 품질 | Checkstyle / JaCoCo |

---

## 실행 방법

### 1. 환경 설정

```bash
# .env 파일 생성
cp .env.example .env
```

`.env` 파일에 필요한 값을 설정합니다. `.env.example`을 참고하세요.

```properties
# Docker
MYSQL_PORT=3313
REDIS_PORT=6379

# AWS S3 (Phase 6에서 사용)
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key

# JWT (Phase 1에서 사용)
JWT_SECRET=your-jwt-secret
```

> `.env` 파일은 `.gitignore`에 포함되어 git에 올라가지 않습니다.
> Spring Boot에서 `spring.config.import`로 `.env`를 직접 읽기 때문에 Docker Compose와 애플리케이션이 동일한 `.env` 파일을 공유합니다.

### 2. Docker 인프라 실행

```bash
docker compose --env-file .env -f docker/compose.dev.yml up -d
```

- MySQL: `localhost:3313`
- Redis: `localhost:6379`

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. API 문서

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

---

## 프로젝트 구조

```
com.moduletest.deasungkioskbackend/
├── global/
│   ├── config/          # Security, Swagger, CORS, P6Spy
│   ├── entity/          # BaseTimeEntity
│   ├── exception/       # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── dto/             # ApiResponse
│   └── security/        # JWT
└── domain/
    ├── admin/           # 관리자 인증
    ├── store/           # 지점 관리
    ├── kiosksetting/    # 배리어프리 설정
    ├── student/         # 학생 관리
    ├── attendance/      # QR 출석
    ├── seat/            # 좌석 관리 + SSE
    ├── meal/            # 식단표
    └── report/          # 엑셀 리포트
```

---

## 주요 명령어

```bash
# 빌드
./gradlew clean build

# 테스트
./gradlew test

# 코드 스타일 검사
./gradlew checkstyleAll

# 커버리지 리포트
./gradlew jacocoTestReport
```

---

## Convention 문서

- [Code Convention](./CODE_CONVENTION_README.md) - 코드 스타일 및 네이밍 규칙
- [API Convention](./API_CONVENTION_README.md) - REST API 설계 및 응답 포맷
- [Git Convention](./GIT_CONVETION_README.md) - 커밋 메시지, 브랜치 전략, PR 프로세스
- [Libraries Guide](./LIBRARIES_CONVENTION_README.md) - P6Spy, MapStruct 설정 가이드
