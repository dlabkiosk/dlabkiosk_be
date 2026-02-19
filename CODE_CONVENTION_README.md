# Code Convention Guide

본 문서는 프로젝트의 코드 작성 규칙과 스타일 가이드를 정의합니다.

---

## 🛠️ IntelliJ 설정

### 1. Code Style 적용 ⭐⭐⭐

1. `SignalDecodeStyle.xml` 다운로드
2. IntelliJ: `Settings` → `Editor` → `Code Style` → `Java`
3. 톱니바퀴 ⚙️ → `Import Scheme` → `IntelliJ IDEA code style XML`
4. `SignalDecodeStyle.xml` 선택 → `Apply`

> ⭐ **SignalDecodeStyle.xml 파일 필요**

### 2. 자동 포맷팅 활성화

1. `Settings` → `Tools` → `Actions on Save`
2. 체크 항목:
   - ✅ Reformat code
   - ✅ Optimize imports
3. `Apply` → `OK`

---

## 📝 네이밍 및 메서드명 규칙

### 네이밍 규칙

| 대상 | 규칙 | 예시 |
| --- | --- | --- |
| 클래스 | PascalCase | `UserService`, `OrderController` |
| 메서드/변수 | camelCase | `getUserInfo()`, `userName` |
| 상수 | UPPER_SNAKE_CASE | `MAX_SIZE`, `DEFAULT_TIMEOUT` |
| 패키지 | lowercase | `com.example.service` |

### 메서드명 규칙

```java
// 이름만 보고 기능 유추가 가능해야함
✅ findUserById(Long id)
❌ get(Long id)
```

**좋은 메서드명 예시:**
- `findUserById()` - 사용자를 ID로 조회
- `createOrder()` - 주문 생성
- `validateEmail()` - 이메일 검증
- `calculateTotalPrice()` - 총 가격 계산

**나쁜 메서드명 예시:**
- `get()` - 무엇을 가져오는지 불명확
- `process()` - 어떤 처리를 하는지 불명확
- `doSomething()` - 구체적인 동작이 불명확

---

## ✍️ 코드 작성 규칙

### 메서드 체이닝: 한 줄에 `.` 하나씩

```java
// Bad ❌
user.setName("John").setAge(25).setEmail("john@example.com");

// Good ✅
user
    .setName("John")
    .setAge(25)
    .setEmail("john@example.com");
```

**빌더 패턴 예시:**

```java
// Bad ❌
User user = User.builder().name("John").age(25).email("john@example.com").build();

// Good ✅
User user = User.builder()
    .name("John")
    .age(25)
    .email("john@example.com")
    .build();
```

**Stream API 예시:**

```java
// Bad ❌
List<String> names = users.stream().filter(u -> u.getAge() > 20).map(User::getName).collect(Collectors.toList());

// Good ✅
List<String> names = users.stream()
    .filter(u -> u.getAge() > 20)
    .map(User::getName)
    .collect(Collectors.toList());
```

### 주석: 구문에 맞춰 들여쓰기

```java
public void processOrder() {
    // 주문 정보 조회
    Order order = orderRepository.findById(orderId);

    // 재고 확인
    validateStock(order);

    // 결제 처리
    processPayment(order);
}
```

**주석 작성 가이드:**

```java
// Bad ❌
public void process() {
//주석과 코드 사이에 공백 없음
Order order = getOrder();
}

// Good ✅
public void processOrder() {
    // 주석과 코드 사이에 적절한 공백
    Order order = getOrder();
}
```

---

## 추가 권장사항

### 1. 들여쓰기

- 들여쓰기는 **공백 4칸** 사용
- 탭 문자 사용 금지

### 2. 줄 바꿈

- 한 줄의 최대 길이: **120자**
- 긴 줄은 적절히 나눠서 작성

### 3. 공백

```java
// 연산자 앞뒤 공백
int result = a + b;

// 쉼표 뒤 공백
method(arg1, arg2, arg3);

// 제어문 키워드 뒤 공백
if (condition) {
    // ...
}
```

### 4. 중괄호

```java
// Good ✅
if (condition) {
    doSomething();
}

// Bad ❌
if (condition)
{
    doSomething();
}

// Bad ❌ (한 줄이어도 중괄호 사용)
if (condition) doSomething();
```

### 5. import 순서

1. static imports
2. java/javax imports
3. 서드파티 라이브러리
4. 프로젝트 내부 패키지

```java
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.persistence.Entity;

import org.springframework.stereotype.Service;

import com.signaldecode.domain.User;
```
