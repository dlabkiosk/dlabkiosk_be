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
│   ├── notice/          # 공지사항 관리
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
| `V12__add_indexes.sql` | attendances, outings, seat_usages 복합 인덱스 4개 |
| `V13__create_notices.sql` | notices 테이블 (Phase 9) |
| `V14__add_bf_enabled_to_stores.sql` | stores에 bf_enabled 컬럼 추가 (Phase 3) |

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

### Phase 3: 배리어프리 원격 제어

배리어프리 모드(고대비, UI 확대, TTS) 자체는 클라이언트에서 처리하지만,
관리자가 원격으로 ON/OFF 제어할 수 있어야 하므로 백엔드에 설정 API 필요.

- [ ] `V14__add_bf_enabled_to_stores.sql` — stores에 `bf_enabled` (BOOLEAN, 기본 false) 컬럼 추가
- [ ] `Store` 엔티티에 `bfEnabled` 필드 추가
- [ ] `StoreUpdateRequest`에 `bfEnabled` 필드 추가 (관리자가 지점 수정 시 ON/OFF 설정)
- [ ] `GET /api/v1/kiosk/settings` — 키오스크가 로그인 후 호출하여 BF 설정값 조회 (KIOSK 인증 필요)
- [ ] 키오스크 클라이언트는 이 값에 따라 BF 모드 활성화/비활성화

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

### Phase 8: 키오스크 모니터링 (Redis Heartbeat)

키오스크 앱이 주기적으로 ping → Redis TTL로 온라인/오프라인 자동 판별.
DB 테이블 없이 Redis만 사용.

**원리:**
- 키오스크 클라이언트가 60초마다 heartbeat 호출
- 서버가 Redis에 `SET heartbeat:kiosk:{storeId} {timestamp} EX 120` (TTL 2분)
- 2분 안에 다음 ping이 안 오면 키 자동 만료 → 오프라인

**Redis 키 구조:**
- `heartbeat:kiosk:{storeId}` → ISO timestamp (TTL 120초)

**구현 목록:**
- [ ] `HeartbeatRedisService` — ping 저장 (SET EX), 상태 조회 (키 존재 여부 + 값)
- [ ] `HeartbeatController`: `POST /api/v1/kiosk/heartbeat` — 키오스크가 60초마다 호출 (KIOSK 인증 필요)
- [ ] `KioskStatusResponse` (storeId, storeName, online, lastSeen)
- [ ] `KioskStatusController`: `GET /api/v1/admin/kiosks/status` — 전체 지점 온/오프라인 조회 (ADMIN 인증 필요)

### Phase 9: 공지사항

관리자가 지점별 공지사항을 등록하고, 키오스크에서 목록 조회.

- [x] `V13__create_notices.sql` — store_id, title, content, pinned(고정), is_active, created_at, updated_at + 복합 인덱스
- [x] `Notice` 엔티티 (Store @ManyToOne LAZY, title, content, pinned, active)
- [x] `NoticeRepository` (findByIdWithStore, findAllWithStore, findAllByStoreIdWithStore, findAllActiveByStoreIdWithStore — 전부 FETCH JOIN)
- [x] `NoticeException` (도메인별 예외 분리)
- [x] `NoticeCreateRequest`, `NoticeUpdateRequest`, `NoticeResponse` (record)
- [x] `NoticeService` — CRUD + 지점별 목록 조회 + 키오스크용 active만 조회
- [x] 어드민 CRUD: `POST/GET/PUT/DELETE /api/v1/admin/notices` (?storeId= 필터)
- [x] 키오스크 조회: `GET /api/v1/kiosk/notices` — 해당 지점 공지 목록 (storeId 토큰에서, KIOSK 인증 필요)

---

## UX 개선 검토 사항

### 키오스크 학생 세션 도입 검토

**현재 방식:** QR/RFID를 매 동작마다 찍어야 함 (등원 1회 + 좌석 입실 1회 + 외출 1회 + ...)
**문제:** 학생 입장에서 매번 QR/RFID 인증이 번거로움. 일반적인 학원/독서실 키오스크는 최초 1회 인증 후 자유롭게 조작하는 방식이 주류.

**개선 방안: 프론트 임시 세션**
1. QR/RFID로 최초 본인 확인 (등원 또는 단순 인증)
2. 응답으로 받은 studentId를 프론트에서 임시 보관
3. 이후 좌석 입실, 외출 등은 studentId로 요청 (QR/RFID 재스캔 불필요)
4. 일정 시간(30초~1분) 무조작 시 자동으로 메인 화면 복귀 (세션 만료)
5. 다음 학생은 다시 QR/RFID 찍어야 사용 가능

**백엔드 변경 범위:**
- 좌석 입실/퇴실, 외출 시작/종료 API에 studentId 기반 요청 방식 추가
- 또는 프론트에서 studentId → qrUuid 매핑을 들고 있으면 백엔드 변경 없이 가능
- 보안: 키오스크 JWT(지점 인증) + 타임아웃 조합으로 충분

**결정 시점:** 프론트 개발 착수 시 UX 흐름 확정하면서 결정

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
    → Phase 8 (키오스크 모니터링) ← Phase 4.5 키오스크 인증 필요
    → Phase 9 (공지사항) ← Phase 2 지점 데이터 필요
  Phase 3 (배리어프리 원격 제어) ← Phase 2 Store 엔티티 확장
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
| `POST /api/v1/kiosk/heartbeat` | KIOSK JWT | 키오스크 heartbeat ping (Phase 8) |
| `GET /api/v1/admin/kiosks/status` | ADMIN JWT | 전체 키오스크 온/오프라인 조회 (Phase 8) |
| `GET /api/v1/kiosk/notices` | KIOSK JWT | 공지사항 목록 (Phase 9) |
| `GET /api/v1/kiosk/settings` | KIOSK JWT | BF 설정값 조회 (Phase 3) |
| `POST /api/admin/auth/refresh` | X | 관리자 토큰 재발급 (refreshToken 쿠키) |
| `/api/v1/admin/**` | ADMIN JWT | 모든 어드민 기능 |

---

## Swagger 그룹

| 그룹 | 경로 |
|------|------|
| 1. 관리자 API | `/api/v1/admin/**`, `/api/admin/**` |
| 2. 키오스크 API | `/api/v1/kiosk/**`, `/api/v1/stores/**` |

---

## 키오스크 클라이언트: 웹 앱 선택 근거

### 결론: 크롬 키오스크 모드 + 웹 앱 (Electron/네이티브 불필요)

### 필요한 기능이 전부 웹으로 가능

| 기능 | 웹 지원 여부 | 방법 |
|------|-------------|------|
| QR/RFID 인식 | O | HID 방식 → 브라우저에 키보드 입력으로 수신 |
| 터치스크린 | O | 터치 오버레이 → OS에서 마우스 입력으로 인식 → 브라우저 기본 지원 |
| 전체화면 고정 | O | 크롬 `--kiosk` 플래그 |
| TTS (음성 안내) | O | Web Speech API |
| 네트워크 감지 | O | `navigator.onLine` + heartbeat |
| 오프라인 대응 | O | PWA / Service Worker |
| 직원 호출 | O | API 호출 + SSE 실시간 알림 |

### 웹 vs Electron vs 네이티브

| 항목 | 웹 (크롬 키오스크) | Electron | 네이티브 (C#/Java) |
|------|-------------------|----------|-------------------|
| 배포/업데이트 | 서버 배포 → 즉시 반영 | 설치파일 배포 필요 | 설치파일 배포 필요 |
| 메모리 | 크롬 탭 하나 | +200MB (Chromium 내장) | 보통 |
| UI 개발 속도 | 빠름 (React) | 빠름 (웹 기술) | 느림 |
| 유지보수 | 웹 하나만 관리 | IPC, 프로세스 분리 등 추가 | 빌드/배포 반복 |
| 관리자 대시보드 통일 | 같은 React 프로젝트 | 별도 관리 | 별도 관리 |
| OS 제어 (재부팅) | X | O | O |

### OS 제어가 필요한 경우

원격 재부팅 등 OS 레벨 기능만 **에이전트(exe) 분리**로 해결.
Electron으로 전체 앱을 감쌀 필요 없이, 작은 에이전트 프로그램(Python/Go)이 서버 명령을 폴링하여 OS 명령 실행.

```
관리자 → POST /admin/kiosk/{storeId}/command → Redis → 에이전트 (폴링) → shutdown /r
```

### 키오스크 운영 환경

- PC 24시간 켜놓고 운영 (전원 끄지 않음)
- BIOS: 전원 복구 시 자동 켜짐 설정 (정전 대비)
- Windows 자동 로그인 + 크롬 키오스크 모드 자동 실행
- 문제 발생 시 에이전트로 원격 재시작
- WOL(Wake-on-LAN)은 불필요 (안 끄는 게 정석)

---

## 회의 확정 요구사항 (2025-02-25)

### 1. 통합 출결 및 급식 관리
- 학생증(RFID/QR) 스캔 시 출결 처리 + 급식 신청 여부 즉시 확인
- 기존 출석 API 응답에 급식 정보 포함 필요

### 2. 학생 정보 조회 서비스
- 키오스크에서 학생 본인이 학번, 배정 좌석, 소속 반 정보 조회 가능
- 새 키오스크 API 필요: `GET /api/v1/kiosk/students/me` (QR/RFID로 본인 식별)

### 3. 외출 관리 변경
- **현재:** 학생이 키오스크에서 자유 외출
- **변경:** 관리자(선생님)가 사전 승인한 학생만 외출 가능
- 관리자 시스템에서 외출 사전 처리 → 키오스크에서 외출 실행
- 하원 후 재입실: CHECKED_OUT 상태에서 다시 등원 시 새 CHECKED_IN 레코드 생성 (기존 로직과 동일)

### 4. 식단표 연동 변경
- **현재 계획:** S3 이미지 직접 업로드
- **변경:** 통합 홈페이지의 지점별 주간 식단표 데이터 자동 동기화
- 키오스크 관리자 페이지에서 수정 가능

### 5. 배리어프리 음성 조절
- 기존 ON/OFF 외에 음성 안내 볼륨/속도 조절 기능 추가

### 6. 기존 구현 영향도

| 기능 | 현재 구현 | 변경 필요 |
|------|-----------|-----------|
| 출석 (Phase 4) | 등원만 처리 | 등원 + 급식 신청 여부 반환 |
| 외출 (Phase 4.6) | 학생 자유 외출 | 관리자 사전 승인 필수 |
| 식단표 (Phase 6) | S3 업로드 | 홈페이지 동기화 + 관리자 수정 |
| 배리어프리 (Phase 3) | bf_enabled ON/OFF | 음성 볼륨/속도 조절 추가 |
| 학생 조회 | 관리자 전용 | 키오스크에서 본인 정보 조회 추가 |

### 7. 미확정 사항 (추론, DSA 스펙 확인 필요)
- DSA에 지점별 강의실 좌석표 + 학생별 지정 좌석이 있을 수 있음
- 확정 시 좌석 자유선택 → 지정좌석 방식으로 변경될 수 있음
- 현재 Redis HSETNX 좌석 경쟁 로직이 불필요해질 가능성 있음

### 선행 조건
- 디랩(D-Lab) 요구기능정의서 수령 → 회의 후 기능 픽스 → 개발 착수
- 계열사(학생관리 프로그램 운영) 미팅 → DB 접근 권한 + API 연동 규격 확정
- 디랩 개발팀과 협업 체계 유지 (회의록, 개발 진행 상시 공유)

---

## DSA 연동 설계

### 배경

기존 DSA(대성 시스템)에 학생 정보, 좌석, 출석 데이터가 존재.
우리 시스템은 QR 인식 레이어를 얹는 역할.

### 데이터 주체

| 데이터 | 원본 | 비고 |
|--------|------|------|
| 학생 정보 | DSA | 이름, 연락처, 지점, rfidUid 등 |
| qrUuid | 우리 DB | DSA에 없는 값, 동기화 시 우리가 생성 |
| 출석/하원/외출 | DSA | 우리 API에서 처리 후 DSA API로 전달 |
| 급식 신청 여부 | DSA | 우리가 학생 식별 후 DSA에 조회 |
| 좌석 배정 | DSA (추정) | 지점별 강의실 좌석표 + 학생별 지정좌석 (미확정) |

### 연동 구조

```
DSA 서버 ──(주기적 동기화)──→ 우리 DB (학생 정보 + qrUuid 추가)
키오스크 ──→ 우리 API (QR/RFID → 학생 식별) ──→ DSA API (결과 전달)
통합 홈페이지 ──(식단표 동기화)──→ 우리 DB
```

**우리 서버 역할:**
1. DSA 학생 데이터 주기적 동기화 + qrUuid 발급
2. QR/RFID 스캔 → 학생 식별 (우리 DB)
3. DSA API 호출 (출석/하원/급식 등 결과 전달)
4. 통합 홈페이지 식단표 동기화

### 동기화 정책

- DSA → 우리 DB 단방향 동기화
- 동기화 시 DSA 데이터로 덮어쓰기, qrUuid는 유지
- 동기화 주기: 설정 가능 (기본 1시간 등)
- 관리자 수동 트리거 API 제공

### DSA API 실패 대응

- 우리 DB에 임시 저장 → DSA 복구 후 재전송 (큐/재시도 방식)
- 키오스크는 DSA 장애와 무관하게 정상 동작

### 선행 조건

- DSA API 스펙 확인 필요 (REST/SOAP, 인증 방식, 엔드포인트 목록)
- DSA 측 학생 조회 API, 출석 기록 API, 급식 조회 API 존재 여부 확인
- 좌석 배정 데이터 존재 여부 및 형태 확인 필요

---

## 검증 방법

각 Phase 완료 후:
1. `./gradlew classes checkstyleMain` — 빌드 + 스타일 검사
2. `docker-compose up -d` → `./gradlew bootRun` — Flyway 자동 마이그레이션 + 앱 실행
3. P6Spy 로그로 SQL 확인 (local)
4. Swagger UI 또는 Postman/curl로 API 테스트
