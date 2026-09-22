# 인증 기능

## URL

| 기능 | 화면 GET | 처리 POST (AJAX) |
|---|---|---|
| 회원가입 | /auth/sign-up | /api/v1/auth/sign-up |
| 로그인 | /auth/login | /api/v1/auth/login |
| 로그아웃 | 별도 화면 없음 | /api/v1/auth/logout |
| 비밀번호 찾기 | /auth/password/forgot | /api/v1/auth/password/forgot |
| 비밀번호 재설정 | /auth/password/reset | /api/v1/auth/password/reset |

요청은 fetch로 application/x-www-form-urlencoded 데이터를 전송한다.
Thymeleaf 폼의 CSRF 토큰과 세션 쿠키를 함께 보내며, 응답은 JSON이다.
로그인은 Spring Security 인증 필터가 처리하고 세션을 저장한다.
회원가입·재설정 성공 시 redirectUrl에 지정된 로그인 화면으로 이동한다.
로그인 성공 시 메인 화면, 로그아웃 성공 시 로그인 화면으로 이동한다.
이전 /login, /sign-up, /password-reset GET 주소는 새 화면 주소로 리다이렉트한다.

응답 예:

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "입력 내용을 확인해 주세요.",
  "fieldErrors": { "pwConfirm": "비밀번호가 일치하지 않습니다." },
  "redirectUrl": null
}
```

입력 오류는 항목 아래, 인증 실패·통신 오류는 제출 버튼 위에 표시된다.
실패 후 입력값을 유지하고 버튼을 다시 활성화한다.
로그인 자격 증명 오류는 계정 존재 여부를 노출하지 않는 통합 메시지를 사용한다.
입력 누락, 계정 잠금·비활성화 예외, 서버 오류는 별도 메시지로 처리한다.
현재 사용자 엔티티에는 잠금·비활성화 상태 필드는 없다.

## 메일 설정

실제 메일 발송 전 실행 환경에 다음 값을 설정한다.
자격 증명은 소스에 저장하지 않는다.

| 환경변수 | 설명 |
|---|---|
| SPRING_MAIL_HOST | SMTP 호스트 (미설정 시 비밀번호 찾기는 503 응답) |
| SPRING_MAIL_PORT | SMTP 포트, 예: 587 |
| SPRING_MAIL_USERNAME | SMTP 사용자명 |
| SPRING_MAIL_PASSWORD | SMTP 비밀번호 또는 앱 비밀번호 |
| MAIL_SMTP_AUTH | SMTP 인증 여부, 기본 true |
| MAIL_SMTP_STARTTLS_ENABLE | STARTTLS 사용 여부, 기본 true |
| MAIL_FROM | 발신 주소 |
| APP_BASE_URL | 서비스 외부 주소. 운영에서는 HTTPS 주소, 컨텍스트 경로가 있으면 포함 |

메일에 포함된 링크는 30분 동안 유효하며 한 번만 사용할 수 있다.
같은 계정의 재발송은 1분 간격으로 제한하고, 새 링크 발급 시 이전 링크를 폐기한다.
토큰 원문은 DB에 저장하지 않고 SHA-256 해시만 저장한다.
이메일과 닉네임만으로 변경하던 이전 처리 방식은 제거했다.
링크 토큰은 URL fragment에서 읽어 폼으로 전송하며 주소창에서는 즉시 지운다.
재설정 화면을 새로고침했다면 메일 링크를 다시 열어야 한다.

## 데이터베이스

로컬 DB 정리 완료: 사용하지 않는 users.password, nick_name, update_at 컬럼을 삭제했다.
삭제 전 기존 컬럼에만 남은 비밀번호·닉네임과 더 최신인 수정일이 없는지 확인했고 회원 행은 유지했다.
적용 SQL: [미사용 컬럼 삭제](sql/2026-09-15-auth-unused-columns.sql).
아래 복구 SQL은 삭제 전 스키마에만 해당하며, 정리 완료된 DB에 다시 실행하지 않는다.

기존 MySQL 컬럼에 맞춰 users.id(INT), password_hash(VARCHAR 255), nickname, updated_at을 명시적으로 매핑한다.
Java 식별자 필드 이름은 idx를 유지한다. 이메일과 닉네임의 중복을 검사한다.
이전 자동 스키마 변경으로 생성된 password, nick_name 컬럼이 NOT NULL이면
[로컬 스키마 복구 SQL](sql/2026-09-15-auth-schema-repair.sql)을 검토 후 적용한다.
이 SQL은 기존 데이터를 삭제하지 않고 사용하지 않는 두 컬럼을 nullable로 바꾼다.
재설정 토큰의 user_id 타입도 기존 users.id와 같은 INT로 맞춘다.
password_reset_tokens 테이블이 추가된다.
현재 개발 설정의 ddl-auto: update가 이를 반영한다.
스키마 자동 변경을 사용하지 않는 배포 환경은 해당 변경을 DB 마이그레이션에 반영해야 한다.

## 검증

- gradlew.bat test: H2를 사용한 9개 테스트 통과.
- 기존 MySQL 컬럼명으로 저장되는지와 닉네임 중복 검사를 회귀 테스트에 추가했다.
- 화면 렌더링, 가입 검증·중복, 로그인 실패·세션 유지, 로그아웃, CSRF 검사.
- 메일 발송 모의 객체로 링크 발급·재발송 제한·만료·재사용 거부·발송 실패 롤백 확인.
- node --check src/main/resources/static/js/main.js: 문법 검사 통과.
- 실제 SMTP 서버 및 브라우저 클릭 동작은 별도 실행 검증이 필요하다.

구현 참고: [Spring Security 인증 흐름](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html),
[Spring 메일 발송](https://docs.spring.io/spring-framework/reference/integration/email.html).
