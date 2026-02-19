# Project Rules for AI Assistant

## 프로젝트 개요
- Spring Boot 3.5.9 기반 백엔드 API 템플릿
- Java 17, MySQL, JPA, Flyway
- 패키지: com.signaldecode.templatebackendapi

---

## 코드 컨벤션

### 네이밍 규칙
- 클래스: PascalCase (UserService, OrderController)
- 메서드/변수: camelCase (getUserInfo, userName)
- 상수: UPPER_SNAKE_CASE (MAX_SIZE, DEFAULT_TIMEOUT)
- 패키지: lowercase (com.example.service)

### 메서드명
- 이름만 보고 기능 유추 가능해야 함
- ✅ findUserById(Long id)
- ❌ get(Long id)

### 코드 포맷팅
**메서드 체이닝 - 한 줄에 `.` 하나씩:**
```java
// Good
User user = User.builder()
    .name("John")
    .age(25)
    .email("john@example.com")
    .build();

// Bad
User user = User.builder().name("John").age(25).email("john@example.com").build();
```

**Stream API:**
```java
// Good
List<String> names = users.stream()
    .filter(u -> u.getAge() > 20)
    .map(User::getName)
    .collect(Collectors.toList());
```

**중괄호:**
- 한 줄이어도 중괄호 사용
- 중괄호는 같은 줄에 시작

**주석:**
- 구문에 맞춰 들여쓰기
- 주석과 코드 사이 적절한 공백

**기타:**
- 들여쓰기: 공백 4칸 (탭 금지)
- 최대 줄 길이: 120자
- import 순서: static → java/javax → 서드파티 → 프로젝트 내부

---

## API 설계 규칙

### HTTP Method
- GET: 조회
- POST: 생성
- PUT: 전체 수정
- PATCH: 부분 수정
- DELETE: 삭제

### URI 규칙
- 소문자 사용
- 하이픈(-) 사용 (언더스코어 금지)
- 복수형 사용
- 버전 포함: /api/v1/users
- ✅ /api/v1/users, /api/v1/user-profiles
- ❌ /api/v1/Users, /api/v1/user_profiles

### 중첩 리소스
- /api/v1/users/{id}/posts
- /api/v1/users/{id}/posts/{postId}

### Query Parameter
- page: 페이지 번호 (0부터 시작)
- size: 페이지 크기
- sort: 정렬 기준 (예: sort=createdAt,desc)

### Response 구조
**모든 API는 ApiResponse<T> 사용:**
```java
@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorCode errorCode;
}
```

**성공 응답:**
```java
return ApiResponse.success(data);
```

**실패 응답:**
```java
return ApiResponse.error(ErrorCode.USER_NOT_FOUND);
```

### Controller 작성
```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUser(id);
        return ApiResponse.success(user);
    }

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ApiResponse.success(user);
    }
}
```

### 에러 처리
- ErrorCode enum 사용
- GlobalExceptionHandler에서 전역 처리
- BusinessException 사용

**HTTP Status Code:**
- 200: GET, PUT, PATCH 성공
- 201: POST 성공
- 204: DELETE 성공
- 400: Validation 실패
- 404: 리소스 없음
- 500: 서버 오류

---

## Git Convention

### Commit 메시지 구조
```
[타입] 제목

본문 (선택사항)
```

### Commit 타입
- Feat: 새로운 기능 추가
- Fix: 버그 수정
- Design: CSS 등 UI 디자인 변경
- Style: 코드 포맷팅 (코드 변경 없음)
- Refactor: 코드 리팩토링
- Comment: 주석 추가 및 변경
- Docs: 문서 수정
- Test: 테스트 코드
- Rename: 파일/폴더명 수정
- Remove: 파일 삭제
- Chore: 빌드, 패키지 매니저 설정
- Setting: 프로젝트 설정 파일
- Deploy: 배포 관련
- !HOTFIX: 긴급 버그 수정

### Commit 규칙
- 제목: 50자 이내
- 명령문 사용 (과거형 X)
- ✅ [Feat] 회원가입 API 구현
- ❌ [Feat] 회원가입 API 구현함

### 브랜치 네이밍
```
{타입}/{이슈번호}-{간단한-설명}
```
- feature/123-user-signup
- fix/456-login-session-error
- refactor/789-user-service-improvement
- hotfix/321-critical-security-patch

---

## 프로젝트 구조

### 패키지 구조
```
com.signaldecode.templatebackendapi/
├── common/
│   ├── config/          # 설정 (Swagger, P6Spy 등)
│   ├── exception/       # 예외 처리
│   └── response/        # 응답 포맷
└── {domain}/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── repository/
    └── service/
```

### 계층 구조
- Controller: HTTP 요청/응답 처리, ApiResponse 반환
- Service: 비즈니스 로직
- Repository: 데이터 접근
- Entity: JPA 엔티티
- DTO: Request/Response 객체

---

## 기술 스택 및 라이브러리

### P6Spy (SQL 로깅)
- 의존성: com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0
- P6SpyFormatter 사용 (commit/rollback 제외)
- Hibernate FormatStyle로 SQL 포맷팅

### MapStruct (객체 매핑)
- 의존성: org.mapstruct:mapstruct:1.6.3
- Lombok과 함께 사용 시 lombok-mapstruct-binding 필수
- @Mapper(componentModel = "spring") 사용
- 빌드 순서: Lombok → MapStruct

### JPA
- ddl-auto: validate (Flyway 사용)
- open-in-view: false

### Flyway
- 마이그레이션 파일: src/main/resources/db/migration/
- 네이밍: V{번호}__{설명}.sql

---

## 코드 작성 시 주의사항

### 필수 사항
1. 모든 API는 ApiResponse<T> 사용
2. ErrorCode enum으로 에러 처리
3. 메서드 체이닝은 한 줄에 하나씩
4. 메서드명은 명확하게 (get 대신 findUserById)
5. 중괄호는 한 줄이어도 필수
6. @Valid로 Request validation
7. @RequiredArgsConstructor로 의존성 주입

### 금지 사항
1. 탭 문자 사용 (공백 4칸)
2. 언더스코어 네이밍 (하이픈 사용)
3. 한 줄 메서드 체이닝
4. 애매한 메서드명 (get, process, doSomething)
5. 중괄호 생략

### Service 레이어 예시
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    public UserResponse createUser(UserRequest request) {
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
}
```

### Entity 작성
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder
    public User(String name) {
        this.name = name;
    }
}
```

---

## 문서 참조
상세한 내용은 다음 문서를 참조:
- CODE_CONVENTION_README.md: 코드 스타일
- API_CONVENTION_README.md: API 설계
- GIT_CONVETION_README.md: Git 규칙
- LIBRARIES_CONVENTION_README.md: 라이브러리 사용법
