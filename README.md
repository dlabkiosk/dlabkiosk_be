# Template Backend API

Spring Boot 기반 백엔드 API 템플릿 프로젝트

---

## 📚 Convention 문서

프로젝트 개발 시 아래 문서를 참고하세요:

- **[Code Convention](./CODE_CONVENTION_README.md)** - 코드 스타일 및 네이밍 규칙
- **[API Convention](API_CONVENTION_README.md)** - REST API 설계 및 응답 포맷
- **[Git Convention](./GIT_CONVETION_README.md)** - 커밋 메시지, 브랜치 전략, PR 프로세스
- **[Libraries Guide](LIBRARIES_CONVENTION_README.md)** - P6Spy, MapStruct 설정 가이드

---

## 🚀 빠른 시작

### 데이터베이스 설정

#### Docker Compose 사용 (권장)

```bash
# .env 파일에서 포트 설정 (기본값: 3310, 3311, 3312, ...)
docker compose --env-file .env -f docker/compose.dev.yml up -d
```

#### Docker 직접 실행

```bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=app \
  -e MYSQL_USER=app \
  -e MYSQL_PASSWORD=app \
  -p 3310:3306 \
  mysql:8.0
```

> **참고:** application-dev.yml의 DB 포트와 Docker 포트 매핑이 일치해야 합니다.
> Docker 포트 형식: `호스트포트:컨테이너포트` (예: `3310:3306`은 호스트 3310 → 컨테이너 3306)

### 실행

```bash
./gradlew bootRun
```

### API 문서

- Swagger UI: http://localhost:8080/swagger-ui.html
