# 대성 키오스크 백엔드 구현 계획

## 기술 스택

| 영역 | 기술                                   |
|---|--------------------------------------|
| 백엔드 | Spring Boot 3.5.9 / Java 17 / Gradle |
| DB | MySQL 8.0                            |
| 캐시 | Redis 7 (Docker)                     |
| 인증 | Spring Security + JWT (jjwt)         |
| 파일 저장 | AWS S3 + CloudFront                  |
| 좌석 상태 캐싱 | Redis Hash + 폴링                   |
| DB 마이그레이션 | Flyway                               |
| SQL 로깅 | P6Spy (local 프로필만)                   |
| 엑셀 | Apache POI (SXSSFWorkbook)           |
| QR 생성 | ZXing                                |
| 기타 | Lombok, Jakarta Validation           |

---

## 패키지 구조 (package-by-feature)

```
com.moduletest.deasungkioskbackend
├── global/
│   ├── config/          # SecurityConfig, RedisConfig, S3Config, WebConfig
│   ├── entity/          # BaseTimeEntity (createdAt, updatedAt)
│   ├── exception/       # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── dto/             # ApiResponse (통일 응답 포맷)
│   └── security/        # JwtTokenProvider, JwtAuthenticationFilter
├── domain/
│   ├── admin/           # 관리자 인증 (로그인, JWT)
│   ├── store/           # 지점 CRUD
│   ├── kiosksetting/    # 배리어프리 설정
│   ├── student/         # 학생 관리
│   ├── attendance/      # QR 출석 (등/하원)
│   ├── seat/            # 좌석 관리 + Redis 캐싱 (폴링)
│   ├── meal/            # 식단표 (S3 이미지 + Redis 캐시)
│   └── report/          # 엑셀 리포트
```

각 domain 모듈: `controller / service / repository / entity / dto`

---

## DB 마이그레이션 (Flyway)

- 경로: `src/main/resources/db/migration/`
- 네이밍: `V{버전}__{설명}.sql`
- `ddl-auto: validate` — Flyway가 스키마 관리, Hibernate는 검증만

| 파일 | 테이블 |
|---|---|
| `V1__create_stores.sql` | stores |
| `V2__create_admin_users.sql` | admin_users |
| `V3__create_kiosk_settings.sql` | kiosk_settings |
| `V4__create_students.sql` | students |
| `V5__create_attendances.sql` | attendances |
| `V6__create_seats.sql` | seats |
| `V7__create_seat_usages.sql` | seat_usages |
| `V8__create_meal_plans.sql` | meal_plans |

---

## SQL 로깅 (P6Spy)

- 실행 SQL을 파라미터 바인딩까지 포함해서 출력
- Hibernate `show-sql` 대신 사용 (`?` 대신 실제 값 표시)
- **local 프로필에서만 활성화**, prod에서는 비활성화

---

## 구현 단계

### Phase 0: 프로젝트 기반 인프라

- [ ] `build.gradle` — 전체 의존성 추가
- [ ] `application.yml` + 프로필별 설정 (`local`, `prod`, `test`)
- [ ] `docker-compose.yml` (MySQL 8.0 + Redis 7)
- [ ] `BaseTimeEntity` — JPA Auditing (createdAt, updatedAt)
- [ ] `ApiResponse<T>` — 통일 응답 포맷 `{success, data, error, timestamp}`
- [ ] `ErrorCode` / `BusinessException` / `GlobalExceptionHandler`
- [ ] CORS 설정
- [ ] Flyway 설정
- [ ] P6Spy 설정 (`spy.properties`)

### Phase 1: 관리자 인증 (JWT)

- [ ] `V2__create_admin_users.sql` — email, password(BCrypt), name, role
- [ ] `AdminUser` 엔티티 + Repository
- [ ] `JwtTokenProvider` — 토큰 생성/검증/클레임 추출
- [ ] `JwtAuthenticationFilter` — OncePerRequestFilter
- [ ] `SecurityConfig` — stateless 세션, 공개/보호 엔드포인트 분리
- [ ] `POST /api/v1/auth/login` → JWT 발급

### Phase 2: 지점 관리

- [ ] `V1__create_stores.sql` — store_name, address, phone, is_active
- [ ] `Store` 엔티티 + Repository
- [ ] 어드민 CRUD: `POST/GET/PUT/DELETE /api/v1/admin/stores`
- [ ] 키오스크 공개 조회: `GET /api/v1/stores/{storeId}`

### Phase 3: 키오스크 설정 (배리어프리)

- [ ] `V3__create_kiosk_settings.sql` — barrier_free, tts_enabled, high_contrast, default_font_size
- [ ] `KioskSetting` 엔티티 (Store 1:1)
- [ ] 키오스크 공개 조회: `GET /api/v1/stores/{storeId}/kiosk-settings`
- [ ] 어드민 수정: `PUT /api/v1/admin/stores/{storeId}/kiosk-settings`

### Phase 4: 학생 관리 + QR 출석

- [ ] `V4__create_students.sql` — name, phone, qr_uuid(UNIQUE), grade, store_id
- [ ] `V5__create_attendances.sql` — student_id, store_id, check_in_at, check_out_at, status
- [ ] `Student` 엔티티 + CRUD (어드민)
- [ ] `QrCodeService` — ZXing으로 UUID → QR PNG 생성
- [ ] `GET /api/v1/admin/students/{studentId}/qr` → QR 이미지 다운로드
- [ ] `POST /api/v1/attendance/check-in` — QR 스캔 → 등원
- [ ] `POST /api/v1/attendance/check-out` — 하원

### Phase 5: 좌석 관리 + Redis 캐싱 (폴링)

SSE 대신 폴링 방식 채택. 키오스크가 5~10초마다 조회 API 호출.
- 소규모(지점당 3~4대)에서는 폴링이 단순하고 충분
- 대규모에서도 폴링이 유리 (stateless, 스케일아웃 자유, 배포 시 영향 없음)
- Redis는 좌석 현재 상태 캐싱용. DB는 기록(히스토리)용

좌석 배치도:
- x_pos, y_pos는 캔버스 상의 픽셀 좌표
- 관리자 웹에서 드래그로 좌석 배치 → 좌표 저장 (프론트: react-draggable 등)
- 키오스크에서 저장된 좌표 그대로 렌더링하면 배치도 완성
- 백엔드는 좌표를 저장/반환만 담당

동시성 제어 (보상 트랜잭션):
- Redis HSETNX로 좌석 원자적 선점 → DB 저장 → DB 실패 시 Redis 롤백

- [ ] `V6__create_seats.sql` — seat_label, seat_type, x_pos(픽셀), y_pos(픽셀), is_active
- [ ] `V7__create_seat_usages.sql` — seat_id, student_id, store_id, status, started_at, ended_at
- [ ] `RedisConfig` — RedisTemplate<String, String>
- [ ] Redis Hash `seat:status:{storeId}` — 현재 좌석 상태 (AVAILABLE / IN_USE:{studentId}:{studentName})
- [ ] `SeatRedisWarmUpRunner` — 서버 기동 시 DB → Redis 초기화 (연결 실패해도 서버 정상 기동)
- [ ] `GET /api/v1/stores/{storeId}/seats` — 좌석 현황 (Redis 조회, 폴링용)
- [ ] `POST /api/v1/seats/{seatId}/check-in` — 좌석 입실 (QR + Redis 선점 + DB 저장)
- [ ] `POST /api/v1/seats/{seatId}/check-out` — 좌석 퇴실 (DB 종료 + Redis 갱신)
- [ ] 어드민 CRUD: `POST/GET/PUT/DELETE /api/v1/admin/seats`

### Phase 6: 식단표 (S3 + Redis 캐시)

- [ ] `V8__create_meal_plans.sql` — store_id, meal_date, meal_type, image_url, description
- [ ] `S3Config` — S3Client bean
- [ ] `FileStorageService` — S3 업로드 + CloudFront URL 반환
- [ ] `@Cacheable`/`@CacheEvict` — Redis 캐시 (TTL 1시간)
- [ ] 어드민 CRUD + 이미지 업로드
- [ ] 키오스크 조회: `GET /api/v1/stores/{storeId}/meal-plans?date=`

### Phase 7: 엑셀 리포트

- [ ] `ExcelReportService` — SXSSFWorkbook (스트리밍)
- [ ] `GET /api/v1/admin/reports/attendance?storeId=&from=&to=` → .xlsx
- [ ] `GET /api/v1/admin/reports/seat-usage?storeId=&from=&to=` → .xlsx

---

## 단계 의존 관계

```
Phase 0 (기반)
  → Phase 1 (인증)
  → Phase 2 (지점)
    → Phase 3 (키오스크 설정)  ← 독립
    → Phase 4 (학생 + QR)     ← 독립
    → Phase 5 (좌석 + Redis)   ← 독립
    → Phase 6 (식단표)        ← 독립
      → Phase 7 (엑셀) ← Phase 4, 5 데이터 필요
```

Phase 3~6은 서로 독립적 — 순서 변경 가능

---

## 보안 엔드포인트 정리

| 경로 | 인증 | 용도 |
|---|---|---|
| `POST /api/v1/auth/login` | X | 로그인 |
| `GET /api/v1/stores/{id}` | X | 키오스크 지점 정보 |
| `GET /api/v1/stores/{id}/kiosk-settings` | X | 키오스크 접근성 설정 |
| `GET /api/v1/stores/{id}/seats` | X | 키오스크 좌석 현황 (폴링) |
| `POST /api/v1/seats/{id}/check-in` | X | 키오스크 좌석 입실 |
| `POST /api/v1/seats/{id}/check-out` | X | 키오스크 좌석 퇴실 |
| `GET /api/v1/stores/{id}/meal-plans` | X | 키오스크 식단 조회 |
| `POST /api/v1/attendance/**` | X | 키오스크 QR 체크인/아웃 |
| `/api/v1/admin/**` | JWT 필수 | 모든 어드민 기능 |

---

## 검증 방법

각 Phase 완료 후:
1. `./gradlew test` — 테스트 통과 확인
2. `docker-compose up -d` → `./gradlew bootRun` — Flyway 자동 마이그레이션 + 앱 실행
3. P6Spy 로그로 SQL 확인 (local)
4. Postman/curl로 API 테스트
