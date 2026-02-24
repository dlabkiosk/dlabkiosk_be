# 대성 키오스크 백엔드 구현 계획

## 기술 스택

| 영역 | 기술                                   |
|---|--------------------------------------|
| 백엔드 | Spring Boot 3.5.9 / Java 17 / Gradle |
| DB | MySQL 8.0                            |
| 캐시 | Redis 7 (Docker)                     |
| 인증 | Spring Security + JWT (jjwt) — 관리자(ADMIN) + 키오스크(KIOSK) 이중 인증, Redis 토큰 저장 |
| 파일 저장 | AWS S3 + CloudFront                  |
| 좌석 상태 캐싱 | Redis Hash (진입 시 조회 + 실패 시 재조회) |
| DB 마이그레이션 | Flyway                               |
| SQL 로깅 | P6Spy (local 프로필만)                   |
| 엑셀 | Apache POI (SXSSFWorkbook)           |
| QR 생성 | ZXing                                |
| 학생 인식 | QR (UUID) + RFID (HID 방식 카드 리더기)   |
| 기타 | Lombok, Jakarta Validation           |

---

## 패키지 구조 (package-by-feature)

```
com.moduletest.deasungkioskbackend
├── common/
│   ├── config/          # SecurityConfig, RedisConfig, SwaggerConfig, WebConfig
│   ├── entity/          # BaseTimeEntity (createdAt, updatedAt)
│   ├── exception/       # ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── dto/             # CommonResponse (통일 응답 포맷)
│   ├── security/        # JwtTokenProvider, JwtAuthenticationFilter, TokenRedisService
│   └── util/            # CookieUtil
├── domain/
│   ├── admin/           # 관리자 인증 (로그인, JWT)
│   ├── kiosk/           # 키오스크 인증 (지점 로그인, JWT)
│   ├── store/           # 지점 CRUD + kioskPin
│   ├── student/         # 학생 관리 (QR UUID + RFID UID)
│   ├── attendance/      # QR/RFID 출석 (등/하원)
│   ├── outing/          # 학생 외출/복귀
│   ├── seat/            # 좌석 관리 + Redis 캐싱
│   ├── meal/            # 식단표 (S3 이미지 + Redis 캐시)
│   └── report/          # 엑셀 리포트
```

각 domain 모듈: `controller / service / repository / entity / dto / exception`

---

## DB 마이그레이션 (Flyway)

- 경로: `src/main/resources/db/migration/`
- 네이밍: `V{버전}__{설명}.sql`
- `ddl-auto: validate` — Flyway가 스키마 관리, Hibernate는 검증만

| 파일 | 내용 |
|---|---|
| `V1__create_stores.sql` | stores 테이블 + 테스트 데이터 5개 |
| `V2__create_admin_users.sql` | admin_users 테이블 |
| `V3` | (보류 — Phase 3 배리어프리용) |
| `V4__create_students.sql` | students 테이블 + 테스트 데이터 10명 |
| `V5__create_attendances.sql` | attendances 테이블 |
| `V6__create_seats.sql` | seats 테이블 + 테스트 데이터 8개 |
| `V7__create_seat_usages.sql` | seat_usages 테이블 |
| `V8__add_kiosk_pin_to_stores.sql` | stores에 kiosk_pin 컬럼 추가 |
| `V9__add_rfid_uid_to_students.sql` | students에 rfid_uid 컬럼 추가 |
| `V10__create_meal_plans.sql` | meal_plans 테이블 (Phase 6) |
| `V11__create_outings.sql` | outings 테이블 (Phase 4.6) |

---

## SQL 로깅 (P6Spy)

- 실행 SQL을 파라미터 바인딩까지 포함해서 출력
- Hibernate `show-sql` 대신 사용 (`?` 대신 실제 값 표시)
- **local 프로필에서만 활성화**, prod에서는 비활성화

---

## 구현 단계

### Phase 0: 프로젝트 기반 인프라

- [x] `build.gradle` — 전체 의존성 추가
- [x] `application.yml` + 프로필별 설정 (`local`, `prod`, `test`)
- [x] `docker-compose.yml` (MySQL 8.0 + Redis 7)
- [x] `BaseTimeEntity` — JPA Auditing (createdAt, updatedAt)
- [x] `CommonResponse<T>` — 통일 응답 포맷
- [x] `ErrorCode` / `BusinessException` / `GlobalExceptionHandler`
- [x] CORS 설정
- [x] Flyway 설정
- [x] P6Spy 설정

### Phase 1: 관리자 인증 (JWT + Redis 토큰 저장)

- [x] `V2__create_admin_users.sql` — login_id, password(BCrypt), name, role
- [x] `AdminUser` 엔티티 + Repository
- [x] `JwtTokenProvider` — 토큰 생성/검증/클레임 추출 + `createKioskToken()` + 역할별 TTL 분리
- [x] `JwtAuthenticationFilter` — OncePerRequestFilter + Redis 토큰 유효성 검증
- [x] `TokenRedisService` — 어드민/키오스크 토큰 Redis CRUD (저장/검증/삭제)
- [x] `SecurityConfig` — stateless, ADMIN/KIOSK 역할 분리, 커스텀 401/403 응답
- [x] `POST /api/admin/auth/login` → JWT 발급 + Redis 저장
- [x] `POST /api/admin/auth/signup` → 관리자 등록
- [x] `POST /api/admin/auth/logout` → Redis 토큰 삭제 + 쿠키 초기화
- [x] `CookieUtil` — 쿠키 생성 + 쿠키 삭제(clearAccessToken, clearRefreshToken)

**토큰 TTL 구분 (.env):**
- `JWT_ACCESS_EXPIRATION` — 어드민 access token (기본 1시간)
- `JWT_REFRESH_EXPIRATION` — 어드민 refresh token (기본 7일)
- `KIOSK_TOKEN_EXPIRATION` — 키오스크 단일 토큰 (기본 24시간, refresh 없음)

**Redis 키 구조:**
- `auth:admin:{userId}` — 어드민 access token
- `auth:refresh:{userId}` — 어드민 refresh token
- `auth:kiosk:{storeId}` — 키오스크 token

### Phase 2: 지점 관리

- [x] `V1__create_stores.sql` — store_name, store_code, address, phone, is_active
- [x] `V8__add_kiosk_pin_to_stores.sql` — kiosk_pin 컬럼 추가
- [x] `Store` 엔티티 (kioskPin 포함) + Repository
- [x] 어드민 CRUD: `POST/GET/PUT/DELETE /api/v1/admin/stores` (kioskPin 설정 가능)
- [x] 키오스크 공개 조회: `GET /api/v1/stores/{storeCode}`

### Phase 3: 키오스크 설정 (배리어프리) ⏭️ 보류

- 배리어프리/TTS/고대비/글자크기는 클라이언트 UI 설정이므로 나중으로 미룸

### Phase 4: 학생 관리 + QR/RFID 출석

- [x] `V4__create_students.sql` — name, phone, qr_uuid(UNIQUE), grade, store_id
- [x] `V5__create_attendances.sql` — student_id, store_id, check_in_at, check_out_at, status
- [x] `V9__add_rfid_uid_to_students.sql` — rfid_uid(UNIQUE, nullable) 컬럼 추가
- [x] `Student` 엔티티 (qrUuid + rfidUid) + CRUD (어드민)
- [x] `QrCodeService` — ZXing으로 UUID → QR PNG 생성
- [x] `GET /api/v1/admin/students/{studentId}/qr` → QR 이미지 다운로드
- [x] `POST /api/v1/kiosk/attendance/check-in` — QR 또는 RFID → 등원 (KIOSK 인증 필요)
- [x] `POST /api/v1/kiosk/attendance/check-out` — 하원 (KIOSK 인증 필요)
- [x] 출석 시 학생 지점 소속 검증 (다른 지점 학생 거부)

### Phase 4.5: 키오스크 인증 (지점별 로그인)

- [x] `KioskLoginRequest` (storeCode + kioskPin)
- [x] `KioskLoginResponse` (storeId, storeName, storeCode)
- [x] `KioskAuthService` — 지점 코드 + PIN 검증 → JWT 발급 (role=KIOSK, subject=storeId) + Redis 저장 + logout
- [x] `KioskAuthController`: `POST /api/v1/kiosk/auth/login` (공개), `POST /api/v1/kiosk/auth/logout` (공개)
- [x] `SecurityConfig` — `/api/v1/kiosk/auth/**` 공개, `/api/v1/kiosk/**` KIOSK 역할 필요
- [x] 키오스크 API에서 storeId를 토큰에서 자동 추출

### Phase 4.6: 학생 외출/복귀

- [x] `V11__create_outings.sql` — student_id, store_id, reason, started_at, ended_at
- [x] `Outing` 엔티티 + `OutingRepository` (당일 진행 중 외출 조회 JPQL)
- [x] `OutingException` (도메인별 예외 분리)
- [x] `OutingStartRequest` (qrUuid + rfidUid + reason 선택)
- [x] `OutingEndRequest` (qrUuid + rfidUid)
- [x] `OutingResponse` (fromEntity)
- [x] `OutingService` — 외출 시작/종료 + 좌석 Redis 상태 연동 (OUTING ↔ IN_USE)
- [x] `OutingController`: `POST /api/v1/kiosk/outings/start`, `POST /api/v1/kiosk/outings/end` (KIOSK 인증 필요)
- [x] `SeatRedisService` — `markSeatOuting()`, `markSeatInUse()` 추가
- [x] `SeatStatusResponse` — `outing` 필드 추가 (좌석 배치도에서 외출 중 표시)
- [x] `SeatService` — OUTING: 접두사 파싱 추가
- [x] ErrorCode 추가: OT001(미등원), OT002(이미 외출 중), OT003(외출 기록 없음)

### Phase 5: 좌석 관리 + Redis 캐싱

좌석 현황 조회 방식: **진입 시 조회 + 실패 시 재조회** (폴링/SSE 없음)
- 좌석 화면 진입 시 Redis에서 현황 1회 조회
- 좌석 입실 시도 시 Redis HSETNX로 선점 → 실패하면 에러 응답 + 최신 현황 재조회
- 지점당 키오스크 2~3대로 동시 충돌 확률 극히 낮음, HSETNX가 원자적으로 해결
- 폴링 불필요: 화면 갱신으로 인한 UX 저하(깜빡임, 터치 오입력) 방지
- Redis는 좌석 현재 상태 캐싱용. DB는 기록(히스토리)용

좌석 배치도:
- x_pos, y_pos는 캔버스 상의 픽셀 좌표
- 관리자 웹에서 드래그로 좌석 배치 → 좌표 저장 (프론트: react-draggable 등)
- 키오스크에서 저장된 좌표 그대로 렌더링하면 배치도 완성
- 백엔드는 좌표를 저장/반환만 담당

동시성 제어 (보상 트랜잭션):
- Redis HSETNX로 좌석 원자적 선점 → DB 저장 → DB 실패 시 Redis 롤백

Redis 좌석 상태 값 형식:
- `IN_USE:{studentId}:{studentName}` — 사용 중
- `OUTING:{studentId}:{studentName}` — 외출 중 (좌석 유지)

- [x] `V6__create_seats.sql` — seat_label, seat_type, x_pos(픽셀), y_pos(픽셀), is_active
- [x] `V7__create_seat_usages.sql` — seat_id, student_id, store_id, status, started_at, ended_at
- [x] `RedisConfig` — RedisTemplate<String, String>
- [x] Redis Hash `seat:status:{storeId}` — 현재 좌석 상태
- [x] `SeatRedisWarmUpRunner` — 서버 기동 시 DB → Redis 초기화 (연결 실패해도 서버 정상 기동)
- [x] `GET /api/v1/kiosk/seats` — 좌석 현황 (Redis 조회, 화면 진입 시 호출, storeId 토큰에서)
- [x] `POST /api/v1/kiosk/seats/{seatId}/check-in` — 좌석 입실 (QR/RFID + Redis 선점 + DB 저장)
- [x] `POST /api/v1/kiosk/seats/{seatId}/check-out` — 좌석 퇴실 (DB 종료 + Redis 갱신)
- [x] 어드민 CRUD: `POST/GET/PUT/DELETE /api/v1/admin/seats`

### Phase 6: 식단표 (S3 + Redis 캐시)

- [ ] `V10__create_meal_plans.sql` — store_id, meal_date, meal_type, image_url, description
- [ ] `S3Config` — S3Client bean
- [ ] `FileStorageService` — S3 업로드 + CloudFront URL 반환
- [ ] `@Cacheable`/`@CacheEvict` — Redis 캐시 (TTL 1시간)
- [ ] 어드민 CRUD + 이미지 업로드
- [ ] 키오스크 조회: `GET /api/v1/kiosk/meal-plans` (storeId 토큰에서)

### Phase 5.5: 공부 시간 랭킹 (Redis Sorted Set)

지점별/전체 공부 시간 랭킹. Redis ZSET으로 실시간 순위 제공.

**점수 갱신 시점:**
- 좌석 퇴실 시: `ended_at - started_at` (초 단위) 누적
- 외출 시작 시: `외출시작 - 마지막입실/복귀` (초 단위) 누적
- 외출 시간은 제외 (외출 중에는 점수 안 쌓임)

**Redis 키 구조:**
- `ranking:study:{storeId}:{날짜}` — 지점별 일일 랭킹
- `ranking:study:all:{날짜}` — 전체 일일 랭킹
- TTL 30일 (자동 만료)

**명령어:**
- `ZINCRBY` — 퇴실/외출 시작 시 공부 시간(초) 누적
- `ZREVRANGE` — 점수 높은 순 Top N 조회

**구현 목록:**
- [ ] `StudyRankingRedisService` — ZINCRBY(점수 누적), ZREVRANGE(랭킹 조회)
- [ ] `SeatService` 퇴실 로직에 랭킹 점수 갱신 추가
- [ ] `OutingService` 외출 시작 로직에 랭킹 점수 갱신 추가
- [ ] `RankingResponse` (rank, studentName, studyTime)
- [ ] `RankingController`: `GET /api/v1/kiosk/rankings` — 지점 랭킹 (storeId 토큰에서)
- [ ] `RankingController`: `GET /api/v1/kiosk/rankings/all` — 전체 랭킹
- [ ] `GET /api/v1/admin/rankings?storeId=` — 어드민 랭킹 조회

**향후 확장 (필요 시):**
- 주간/월간 랭킹: 일별 ZSET을 `ZUNIONSTORE`로 합산
- DB 백업: `daily_study_summary` 테이블에 일별 집계 저장 (장기 통계/엑셀 리포트용)

### Phase 7: 엑셀 리포트

- [ ] `ExcelReportService` — SXSSFWorkbook (스트리밍)
- [ ] `GET /api/v1/admin/reports/attendance?storeId=&from=&to=` → .xlsx
- [ ] `GET /api/v1/admin/reports/seat-usage?storeId=&from=&to=` → .xlsx

---

## 단계 의존 관계

```
Phase 0 (기반)
  → Phase 1 (관리자 인증)
  → Phase 2 (지점)
    → Phase 4 (학생 + QR/RFID)
    → Phase 4.5 (키오스크 인증)
    → Phase 4.6 (외출/복귀)
    → Phase 5 (좌석 + Redis)
    → Phase 5.5 (공부 시간 랭킹) ← Phase 5 좌석 퇴실 + Phase 4.6 외출에서 점수 갱신
    → Phase 6 (식단표)
      → Phase 7 (엑셀) ← Phase 4, 5, 5.5 데이터 필요
  Phase 3 (배리어프리) — 보류, 필요 시 복귀
```

---

## 보안 엔드포인트 정리

| 경로 | 인증 | 용도 |
|---|---|---|
| `POST /api/admin/auth/login` | X | 관리자 로그인 |
| `POST /api/admin/auth/signup` | X | 관리자 회원가입 |
| `POST /api/admin/auth/logout` | X | 관리자 로그아웃 (Redis 토큰 삭제 + 쿠키 초기화) |
| `GET /api/v1/stores/{storeCode}` | X | 키오스크 지점 조회 (로그인 전) |
| `POST /api/v1/kiosk/auth/login` | X | 키오스크 지점 로그인 |
| `POST /api/v1/kiosk/auth/logout` | X | 키오스크 로그아웃 (Redis 토큰 삭제 + 쿠키 초기화) |
| `POST /api/v1/kiosk/attendance/**` | KIOSK JWT | 출석 (등/하원) |
| `POST /api/v1/kiosk/outings/start` | KIOSK JWT | 외출 시작 |
| `POST /api/v1/kiosk/outings/end` | KIOSK JWT | 외출 복귀 |
| `GET /api/v1/kiosk/seats` | KIOSK JWT | 좌석 현황 (진입 시 조회) |
| `POST /api/v1/kiosk/seats/{id}/check-in` | KIOSK JWT | 좌석 입실 |
| `POST /api/v1/kiosk/seats/{id}/check-out` | KIOSK JWT | 좌석 퇴실 |
| `GET /api/v1/kiosk/rankings` | KIOSK JWT | 지점 공부 시간 랭킹 (Phase 5.5) |
| `GET /api/v1/kiosk/rankings/all` | KIOSK JWT | 전체 공부 시간 랭킹 (Phase 5.5) |
| `GET /api/v1/kiosk/meal-plans` | KIOSK JWT | 식단 조회 (Phase 6) |
| `/api/v1/admin/**` | ADMIN JWT | 모든 어드민 기능 |

---

## Swagger 그룹

| 그룹 | 경로 |
|------|------|
| 1. 관리자 API | `/api/v1/admin/**`, `/api/admin/**` |
| 2. 키오스크 API | `/api/v1/kiosk/**`, `/api/v1/stores/**` |

---

## 검증 방법

각 Phase 완료 후:
1. `./gradlew classes checkstyleMain` — 빌드 + 스타일 검사
2. `docker-compose up -d` → `./gradlew bootRun` — Flyway 자동 마이그레이션 + 앱 실행
3. P6Spy 로그로 SQL 확인 (local)
4. Swagger UI 또는 Postman/curl로 API 테스트
