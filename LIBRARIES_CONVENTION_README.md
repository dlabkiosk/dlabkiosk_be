# template-backend-api

Spring Boot 기반 백엔드 API 템플릿 프로젝트

## 기술 스택

- Java 17
- Spring Boot 3.5.9
- Spring Data JPA
- MySQL
- Flyway (DB Migration)
- Lombok
- SpringDoc OpenAPI (Swagger UI)
- Checkstyle
- JaCoCo (Code Coverage)

## P6Spy 설정 가이드

P6Spy는 SQL 쿼리를 로깅하고 성능을 모니터링하는 라이브러리입니다.

### 1. 의존성 추가

`build.gradle`에 다음 의존성을 추가:

```gradle
dependencies {
    // P6Spy
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
}
```

### 2. spy.properties 설정

`src/main/resources/spy.properties` 파일 생성:

```properties
appender=com.p6spy.engine.spy.appender.Slf4JLogger
logMessageFormat=com.signaldecode.templatebackendapi.common.config.P6SpyFormatter
```

### 3. P6SpyFormatter 클래스 생성

`src/main/java/com/signaldecode/templatebackendapi/common/config/P6SpyFormatter.java`:

```java
package com.signaldecode.templatebackendapi.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6SpyFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {

        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // commit, rollback 제외
        if ("commit".equalsIgnoreCase(category) || "rollback".equalsIgnoreCase(category)) {
            return "";
        }

        // SQL 포맷팅
        String formattedSql = formatSql(sql);

        return String.format("\n실행시간: %dms | 연결: %d | 시간: %s\n%s\n",
                            elapsed, connectionId, now, formattedSql);
    }

    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        try {
            String trimmedSql = sql.trim().toLowerCase();
            if (trimmedSql.startsWith("select") || trimmedSql.startsWith("insert") ||
                trimmedSql.startsWith("update") || trimmedSql.startsWith("delete")) {
                return FormatStyle.BASIC.getFormatter().format(sql);
            }
            return sql;
        } catch (Exception e) {
            return sql;
        }
    }
}
```

### 주요 기능

- **Hibernate FormatStyle 사용**: SQL 쿼리를 읽기 쉽게 포맷팅
- **commit/rollback 제외**: 불필요한 로그 제거
- **실행시간 표시**: 성능 모니터링 가능
- **연결 정보 표시**: 디버깅 시 유용

### 로그 출력 예시

```
실행시간: 15ms | 연결: 1 | 시간: 2025-01-05 10:30:45
select
    sampleentity0_.id as id1_0_,
    sampleentity0_.name as name2_0_
from
    sample_entity sampleentity0_
where
    sampleentity0_.id=?
```

---

## MapStruct 설정 가이드

MapStruct는 컴파일 타임에 타입 안전한 매핑 코드를 자동 생성하는 라이브러리입니다.

### 1. 의존성 추가

`build.gradle`에 다음 의존성을 추가:

```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'

    // Lombok과 함께 사용할 경우
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

**중요**: Lombok과 함께 사용할 경우 annotationProcessor 순서가 중요합니다:

```gradle
annotationProcessor 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'
```

### 2. IntelliJ MapStruct Support Plugin 설치

MapStruct를 더 편리하게 사용하기 위해 IntelliJ IDEA 플러그인을 설치합니다.

#### 플러그인 설치 방법

1. IntelliJ IDEA 실행
2. `Settings` (Windows/Linux) 또는 `Preferences` (Mac) 열기 (`Ctrl+Alt+S` 또는 `Cmd+,`)
3. `Plugins` 메뉴 선택
4. `Marketplace` 탭에서 **MapStruct Support** 검색
5. 설치 후 IDE 재시작

#### 플러그인 주요 기능

- **자동완성**: Mapper 메서드 작성 시 자동완성 지원
- **네비게이션**: 소스와 타겟 필드 간 이동 (Ctrl+Click)
- **에러 검증**: 잘못된 매핑 설정 감지
- **리팩토링**: 필드명 변경 시 자동으로 @Mapping 업데이트
- **미리보기**: 생성될 구현체 코드 미리보기

#### Annotation Processing 활성화

MapStruct가 정상적으로 작동하려면 Annotation Processing을 활성화해야 합니다:

1. `Settings` → `Build, Execution, Deployment` → `Compiler` → `Annotation Processors`
2. ✅ **Enable annotation processing** 체크
3. `Apply` → `OK`

### 3. Mapper 인터페이스 작성

#### 기본 매핑

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(UserRequest request);

    List<UserResponse> toResponseList(List<User> users);
}
```

#### 필드명이 다른 경우

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "fullName", target = "name")
    @Mapping(source = "emailAddress", target = "email")
    UserResponse toResponse(User user);
}
```

#### 중첩 객체 매핑

```java
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {
    UserResponse toResponse(User user);
}

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressResponse toResponse(Address address);
}
```

#### 날짜/시간 포맷

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "createdAt", target = "createdDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    UserResponse toResponse(User user);
}
```

#### 커스텀 매핑 로직

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserResponse toResponse(User user);

    @AfterMapping
    default void afterMapping(@MappingTarget UserResponse response, User user) {
        // 매핑 후 추가 로직
        response.setDisplayName(user.getFirstName().toUpperCase());
    }
}
```

#### 조건부 매핑

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", ignore = true)
    UserResponse toResponseWithoutEmail(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UserRequest request, @MappingTarget User user);
}
```

### 4. Service에서 사용

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    public UserResponse createUser(UserRequest request) {
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }
}
```

### 5. 고급 기능

#### 상수값 설정

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "type", constant = "REGULAR")
    @Mapping(target = "status", constant = "ACTIVE")
    UserResponse toResponse(User user);
}
```

#### 기본값 설정

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "role", defaultValue = "USER")
    User toEntity(UserRequest request);
}
```

#### 여러 소스 객체 매핑

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "profile.bio", target = "biography")
    UserDetailResponse toDetailResponse(User user, Profile profile);
}
```

#### Qualifier를 사용한 커스텀 메서드

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "password", target = "password", qualifiedByName = "encryptPassword")
    User toEntity(UserRequest request);

    @Named("encryptPassword")
    default String encryptPassword(String password) {
        // 비밀번호 암호화 로직
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
```

### 6. 설정 옵션

공통 설정을 위한 커스텀 어노테이션:

```java
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MapStructConfig {
}

@Mapper(config = MapStructConfig.class)
public interface UserMapper {
    // ...
}
```

### 7. 빌드 및 확인

MapStruct는 컴파일 타임에 구현체를 생성합니다:

```bash
./gradlew clean build
```

생성된 구현체는 `build/generated/sources/annotationProcessor/java` 경로에서 확인할 수 있습니다.

### 8. 주의사항

1. **Lombok과의 조합**: `lombok-mapstruct-binding` 의존성 필수
2. **빌드 순서**: Lombok → MapStruct 순서로 annotation processor 실행
3. **IDE 설정**: IntelliJ IDEA에서 "Enable annotation processing" 활성화 필수
4. **플러그인 설치 권장**: MapStruct Support 플러그인 설치 시 개발 편의성 대폭 향상
5. **성능**: 컴파일 타임에 코드가 생성되므로 런타임 오버헤드 없음
6. **타입 안정성**: 컴파일 타임에 타입 체크가 이루어져 안전함

---

## 프로젝트 실행

```bash
./gradlew bootRun
```

## 테스트 실행

```bash
./gradlew test
```

## 코드 품질 검사

```bash
./gradlew checkstyleAll
```

## 빌드

```bash
./gradlew build
```

## API 문서

애플리케이션 실행 후 다음 URL에서 확인:

- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs
