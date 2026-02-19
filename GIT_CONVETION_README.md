# Git Convention Guide

본 문서는 프로젝트의 Git 사용 규칙과 협업 프로세스를 정의합니다.

---

## 📝 Commit 메시지

### 커밋 메시지 구조

```
[타입] 제목

본문 (선택사항)
```

### 커밋 타입

| 타입 | 설명 |
| --- | --- |
| **`Feat`** | 새로운 기능 추가 |
| **`Fix`** | 버그 수정 |
| **`Design`** | CSS 등 UI 디자인 변경 |
| **`Style`** | 코드 포맷팅, 세미콜론 누락 등 (코드 변경 없음) |
| **`Refactor`** | 코드 리팩토링 |
| **`Comment`** | 주석 추가 및 변경 |
| **`Docs`** | 문서 수정 (README 등) |
| **`Test`** | 테스트 코드 추가/수정 |
| **`Rename`** | 파일/폴더명 수정 또는 이동 |
| **`Remove`** | 파일 삭제 |
| **`Chore`** | 빌드 업무, 패키지 매니저 설정 등 |
| **`Setting`** | 프로젝트 설정 파일 추가/수정 |
| **`Deploy`** | 배포 관련 |
| **`!HOTFIX`** | 급한 치명적 버그 수정 |

### 커밋 예시

#### 기본 형태

```
[Feat] 회원가입 API 구현
[Fix] 게시글 삭제 시 권한 검증 오류 수정
[Design] 버튼 컴포넌트 스타일 수정
[Refactor] 사용자 서비스 레이어 구조 개선
[Docs] README에 설치 가이드 추가
[Remove] 사용하지 않는 테스트 파일 삭제
```

#### 본문 포함

```
[Feat] 게시글 검색 기능 추가

- 제목, 내용, 작성자 기준 검색 지원
- 페이지네이션 적용
- 검색 결과 정렬 옵션 추가
```

```
[Fix] 회원가입 시 중복 이메일 처리 오류

- 중복 이메일 검증 로직 수정
- 에러 메시지 명확화
- 관련 테스트 케이스 추가
```

### 커밋 메시지 작성 규칙

1. **제목은 50자 이내**로 작성
2. **제목과 본문 사이 빈 줄** 추가
3. **본문은 72자 단위**로 줄바꿈
4. **무엇을, 왜 했는지** 명확하게 작성
5. **명령문** 사용 (과거형 X)

```
✅ [Feat] 회원가입 API 구현
❌ [Feat] 회원가입 API 구현함
❌ [Feat] 회원가입 API 구현했음
```

---

## 🚫 지양할 커밋

다음과 같은 커밋은 지양합니다:

### 1. 애매한 메시지

```
❌ [Feat] 기능 추가
❌ [Fix] 버그 수정
❌ [Refactor] 코드 수정
```

```
✅ [Feat] 게시글 좋아요 기능 추가
✅ [Fix] 로그인 시 세션 만료 오류 수정
✅ [Refactor] UserService 의존성 주입 방식 개선
```

### 2. 여러 작업을 하나의 커밋으로

```
❌ [Feat] 회원가입, 로그인, 프로필 수정 기능 추가
```

```
✅ [Feat] 회원가입 API 구현
✅ [Feat] 로그인 API 구현
✅ [Feat] 프로필 수정 API 구현
```

### 3. 의미 없는 커밋

```
❌ [Feat] 테스트
❌ [Fix] 오류
❌ [Chore] 수정
❌ [Refactor] asdf
```

### 4. 너무 큰 단위의 커밋

하나의 커밋에는 **하나의 논리적 변경사항**만 포함

```
❌ 100개 파일 변경, 3000줄 추가
✅ 관련 파일들만 변경, 적절한 단위로 분리
```

---

## 🌿 브랜치 전략

### 브랜치 네이밍 규칙

```
{타입}/{이슈번호}-{간단한-설명}
```

**타입 종류:**
- `feature`: 새로운 기능 개발
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `hotfix`: 긴급 수정
- `release`: 릴리즈 준비
- `docs`: 문서 작업

**예시:**

```
feature/123-user-signup
fix/456-login-session-error
refactor/789-user-service-improvement
hotfix/321-critical-security-patch
docs/654-update-readme
```

### 주요 브랜치

| 브랜치 | 용도 | 보호 여부 |
| --- | --- | --- |
| `main` | 프로덕션 배포 브랜치 | ✅ 보호 |
| `develop` | 개발 통합 브랜치 | ✅ 보호 |
| `feature/*` | 기능 개발 브랜치 | ❌ |
| `fix/*` | 버그 수정 브랜치 | ❌ |
| `hotfix/*` | 긴급 수정 브랜치 | ❌ |

### 브랜치 작업 흐름

```
1. develop에서 새 브랜치 생성
   git checkout develop
   git pull origin develop
   git checkout -b feature/123-user-signup

2. 작업 및 커밋
   git add .
   git commit -m "[Feat] 회원가입 API 구현"

3. 원격 저장소에 푸시
   git push origin feature/123-user-signup

4. Pull Request 생성
   GitHub에서 develop <- feature/123-user-signup PR 생성

5. 코드 리뷰 및 머지
   리뷰 승인 후 develop에 머지

6. 로컬 브랜치 정리
   git checkout develop
   git pull origin develop
   git branch -d feature/123-user-signup
```

---

## 🔀 PR (Pull Request) 프로세스

### PR 제목 규칙

**형식:** `[타입] 기능 설명`

**예시:**

```
[FEAT] 회원가입 API 구현
[FIX] 로그인 오류 수정
[REFACTOR] UserService 리팩토링
[DOCS] API 문서 추가
[TEST] 회원가입 테스트 추가
```

### PR 템플릿

```markdown
## 변경 사항
- User 엔티티 생성
- 회원가입 API 구현 (POST /api/auth/signup)
- 이메일 중복 체크 로직 추가

## 테스트 방법
- 회원가입 API 테스트 코드 작성
- Postman으로 수동 테스트 완료

## 참고 사항
- JWT 발급은 다음 PR에서 추가 예정
```

### PR 작성 가이드

1. **제목은 명확하게**
   - 무엇을 구현/수정했는지 한눈에 파악 가능하도록

2. **변경 사항 상세 작성**
   - 추가된 기능
   - 수정된 버그
   - 리팩토링 내용

3. **테스트 방법 명시**
   - 단위 테스트
   - 통합 테스트
   - 수동 테스트

4. **스크린샷/GIF 첨부** (UI 변경 시)
   - Before/After 비교

5. **연관된 이슈 링크**
   - Resolves #123
   - Related to #456

### PR 체크리스트

PR을 올리기 전에 다음을 확인하세요:

- [ ] 코드 컨벤션 준수
- [ ] 테스트 코드 작성 및 통과
- [ ] 불필요한 주석/로그 제거
- [ ] 충돌(Conflict) 해결
- [ ] 관련 문서 업데이트
- [ ] 자체 리뷰 완료

---

## 👥 코드 리뷰 가이드

### 리뷰어 체크사항

1. **코드 품질**
   - 가독성
   - 유지보수성
   - 확장성

2. **로직 검증**
   - 비즈니스 로직 정확성
   - 예외 처리
   - 성능 이슈

3. **테스트**
   - 테스트 커버리지
   - 테스트 케이스 적절성

4. **보안**
   - 보안 취약점
   - 민감 정보 노출

### 리뷰 코멘트 작성 가이드

**좋은 코멘트:**

```
✅ 이 부분은 null 체크가 필요할 것 같습니다. NPE 가능성이 있어요.
✅ Optional을 사용하면 더 명확할 것 같습니다.
✅ 좋은 리팩토링이네요! 가독성이 훨씬 좋아졌습니다.
```

**지양할 코멘트:**

```
❌ 이거 왜 이렇게 했어요?
❌ 이해가 안 가네요.
❌ 다시 하세요.
```

### 리뷰 승인 기준

- 2명 이상의 Approve 필요 (팀 규모에 따라 조정)
- 모든 코멘트 해결
- CI/CD 통과
- 충돌(Conflict) 없음

---

## 📋 추가 Git 규칙

### 1. Rebase vs Merge

**develop 브랜치 최신화:**

```bash
# Rebase 사용 (권장)
git checkout feature/123-user-signup
git rebase develop

# Merge 사용 (지양)
git merge develop
```

### 2. 커밋 수정

**마지막 커밋 메시지 수정:**

```bash
git commit --amend -m "[Fix] 수정된 커밋 메시지"
```

**이미 푸시한 경우 (주의!):**

```bash
git push --force-with-lease
```

### 3. 작업 중 임시 저장

```bash
# 작업 임시 저장
git stash

# 저장 목록 확인
git stash list

# 저장한 작업 복원
git stash pop
```

### 4. 브랜치 정리

```bash
# 로컬 브랜치 삭제
git branch -d feature/123-user-signup

# 원격 브랜치 삭제
git push origin --delete feature/123-user-signup

# 이미 머지된 브랜치 일괄 삭제
git branch --merged | grep -v "\*" | xargs -n 1 git branch -d
```

---

## 🚨 주의사항

1. **main, develop 브랜치에 직접 푸시 금지**
2. **force push 지양** (불가피한 경우 팀원과 협의)
3. **대용량 파일 커밋 금지** (.gitignore 활용)
4. **민감 정보 커밋 금지** (API 키, 비밀번호 등)
5. **작업 전 최신 코드 pull** 필수
