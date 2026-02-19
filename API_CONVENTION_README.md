# API 설계 가이드

본 문서는 REST API 설계 규칙과 응답 포맷을 정의합니다.

---

## 📌 엔드포인트 규칙

### HTTP Method 사용 규칙

| HTTP Method | 용도 | URI 예시 | 설명 |
| --- | --- | --- | --- |
| GET | 조회 | /api/v1/users | 사용자 목록 조회 |
| GET | 단건 조회 | /api/v1/users/{id} | 특정 사용자 조회 |
| POST | 생성 | /api/v1/users | 사용자 생성 |
| PUT | 전체 수정 | /api/v1/users/{id} | 사용자 전체 수정 |
| PATCH | 부분 수정 | /api/v1/users/{id} | 사용자 부분 수정 |
| DELETE | 삭제 | /api/v1/users/{id} | 사용자 삭제 |

### 중첩 리소스

| HTTP Method | URI 예시 | 설명 |
| --- | --- | --- |
| GET | /api/v1/users/{id}/posts | 특정 사용자의 게시글 목록 조회 |
| POST | /api/v1/users/{id}/posts | 특정 사용자의 게시글 생성 |
| GET | /api/v1/users/{id}/posts/{postId} | 특정 사용자의 특정 게시글 조회 |

### 검색 및 필터링

| HTTP Method | URI 예시 | 설명 |
| --- | --- | --- |
| GET | /api/v1/users?status=active&page=0&size=20 | 활성 사용자 목록 조회 (페이징) |
| GET | /api/v1/posts?userId=123&category=tech | 특정 사용자의 기술 카테고리 게시글 조회 |

### Query Parameter 규칙

| Parameter | 용도 | 예시 |
| --- | --- | --- |
| page | 페이지 번호 (0부터 시작) | page=0 |
| size | 페이지 크기 | size=20 |
| sort | 정렬 기준 | sort=createdAt,desc |
| {필드명} | 필터링 조건 | status=active, category=tech |

---

## 📦 Response Type

### API Response 구조

모든 API 응답은 통일된 포맷을 사용합니다.

```java
@Getter
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorCode errorCode;  // ErrorCode enum

    // 성공 응답 (메시지 없음)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    // 실패 응답
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)  // enum만 넣으면 끝!
                .build();
    }
}
```

### 성공 응답 예시

**단건 조회**

```json
HTTP/1.1 200 OK
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동"
  },
  "errorCode": null
}
```

**목록 조회**

```json
HTTP/1.1 200 OK
{
  "success": true,
  "data": [
    {
      "id": 1,
      "email": "user1@example.com",
      "name": "홍길동"
    },
    {
      "id": 2,
      "email": "user2@example.com",
      "name": "김철수"
    }
  ],
  "errorCode": null
}
```

**생성 성공**

```json
HTTP/1.1 201 Created
{
  "success": true,
  "data": {
    "id": 3,
    "email": "newuser@example.com",
    "name": "이영희"
  },
  "errorCode": null
}
```

### 실패 응답 예시

**404 Not Found**

```json
HTTP/1.1 404 Not Found
{
  "success": false,
  "data": null,
  "errorCode": {
    "code": "U001",
    "message": "사용자를 찾을 수 없습니다",
    "status": "NOT_FOUND"
  }
}
```

**400 Bad Request**

```json
HTTP/1.1 400 Bad Request
{
  "success": false,
  "data": null,
  "errorCode": {
    "code": "V001",
    "message": "이메일 형식이 올바르지 않습니다",
    "status": "BAD_REQUEST"
  }
}
```

**500 Internal Server Error**

```json
HTTP/1.1 500 Internal Server Error
{
  "success": false,
  "data": null,
  "errorCode": {
    "code": "S001",
    "message": "서버 내부 오류가 발생했습니다",
    "status": "INTERNAL_SERVER_ERROR"
  }
}
```

---

## 🎯 Controller 예시

### 기본 CRUD Controller

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 목록 조회
    @GetMapping
    public ApiResponse<List<UserResponse>> getUsers(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserResponse> users = userService.getUsers(status, page, size);
        return ApiResponse.success(users);
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.getUser(id);
        return ApiResponse.success(user);
    }

    // 생성
    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse user = userService.createUser(request);
        return ApiResponse.success(user);
    }

    // 전체 수정
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ApiResponse.success(user);
    }

    // 부분 수정
    @PatchMapping("/{id}")
    public ApiResponse<UserResponse> patchUser(
            @PathVariable Long id,
            @RequestBody UserPatchRequest request) {
        UserResponse user = userService.patchUser(id, request);
        return ApiResponse.success(user);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success(null);
    }
}
```

### 중첩 리소스 Controller

```java
@RestController
@RequestMapping("/api/v1/users/{userId}/posts")
@RequiredArgsConstructor
public class UserPostController {
    private final PostService postService;

    // 특정 사용자의 게시글 목록
    @GetMapping
    public ApiResponse<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        List<PostResponse> posts = postService.getPostsByUserId(userId);
        return ApiResponse.success(posts);
    }

    // 특정 사용자의 게시글 생성
    @PostMapping
    public ApiResponse<PostResponse> createUserPost(
            @PathVariable Long userId,
            @Valid @RequestBody PostRequest request) {
        PostResponse post = postService.createPost(userId, request);
        return ApiResponse.success(post);
    }

    // 특정 사용자의 특정 게시글 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getUserPost(
            @PathVariable Long userId,
            @PathVariable Long postId) {
        PostResponse post = postService.getPost(userId, postId);
        return ApiResponse.success(post);
    }
}
```

---

## 🚨 에러 처리

### ErrorCode Enum

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // User 관련
    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("U002", "이미 존재하는 사용자입니다", HttpStatus.CONFLICT),

    // Validation 관련
    INVALID_EMAIL_FORMAT("V001", "이메일 형식이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("V002", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

    // Server 관련
    INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
```

### Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("Business exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ApiResponse.error(e.getErrorCode()));
    }

    // Validation 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.error("Validation exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.INVALID_INPUT));
    }

    // 그 외 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

### BusinessException

```java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 사용 예시

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserMapper.toResponse(user);
    }
}
```

### 도메인별 Exception 패턴

도메인마다 전용 Exception을 만들면 코드가 더 명확해집니다.

**도메인 Exception 생성:**

```java
public class UserException extends BusinessException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

    // 편의 메서드
    public static UserException notFound() {
        return new UserException(ErrorCode.USER_NOT_FOUND);
    }

    public static UserException duplicateEmail() {
        return new UserException(ErrorCode.USER_ALREADY_EXISTS);
    }
}
```

**Service에서 사용:**

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserException::notFound);
    }

    public void register(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw UserException.duplicateEmail();
        }
        // ...
    }
}
```

---

## 📄 페이징 응답

### Page Response

```java
@Getter
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
```

### 페이징 응답 예시

```json
HTTP/1.1 200 OK
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "email": "user1@example.com",
        "name": "홍길동"
      },
      {
        "id": 2,
        "email": "user2@example.com",
        "name": "김철수"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  },
  "errorCode": null
}
```

---

## 🔍 추가 권장사항

### 1. URI 네이밍

- **소문자** 사용
- **하이픈(-)** 사용 (언더스코어 사용 금지)
- **복수형** 사용

```
✅ /api/v1/users
✅ /api/v1/user-profiles
❌ /api/v1/Users
❌ /api/v1/user_profiles
❌ /api/v1/user
```

### 2. HTTP Status Code

| Status Code | 설명 | 사용 시점 |
| --- | --- | --- |
| 200 OK | 성공 | GET, PUT, PATCH 성공 |
| 201 Created | 생성 성공 | POST 성공 |
| 204 No Content | 성공 (응답 본문 없음) | DELETE 성공 |
| 400 Bad Request | 잘못된 요청 | Validation 실패 |
| 401 Unauthorized | 인증 실패 | 로그인 필요 |
| 403 Forbidden | 권한 없음 | 접근 권한 없음 |
| 404 Not Found | 리소스 없음 | 조회 실패 |
| 409 Conflict | 충돌 | 중복 데이터 |
| 500 Internal Server Error | 서버 오류 | 예상치 못한 오류 |

### 3. API Versioning

URI에 버전 정보 포함:

```
/api/v1/users
/api/v2/users
```
