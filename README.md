# Created: 2026-04-09 00:30:47
# 네모네모 스토리지 — Backend

공유 스토리지(창고 파티션 임대) 예약 및 계약 관리 시스템의 백엔드 API 서버입니다.

## 기술 스택

- Java 21 / Spring Boot 3.5
- Spring Data JPA + QueryDSL
- Spring Security + JWT (jjwt 0.12.3)
- MySQL 8
- SpringDoc OpenAPI (Swagger UI)
- Gradle

## 실행 방법

### 1. MySQL 실행 (Docker)

```bash
docker run -d --name nemonemo-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=nemostorage \
  -p 3306:3306 mysql:8
```

### 2. 앱 실행

```bash
DB_USERNAME=root DB_PASSWORD=password ./gradlew bootRun
```

서버: `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## 초기 관리자 계정

| 항목 | 값 |
|------|----|
| 아이디 | `admin` |
| 비밀번호 | `admin1234` |

## API 구조

| 분류 | 경로 | 인증 |
|------|------|------|
| 유닛 현황 조회 | `GET /api/units` | 불필요 |
| 예약 문의 제출 | `POST /api/inquiries` | 불필요 |
| 관리자 로그인 | `POST /api/admin/auth/login` | - |
| 유닛 관리 | `/api/admin/units` | JWT |
| 문의 관리 | `/api/admin/inquiries` | JWT |
| 계약 관리 | `/api/admin/contracts` | JWT |
| 대시보드 | `GET /api/admin/dashboard` | JWT |

## 도메인 모델

```
Warehouse ─── Unit ─── Contract ──→ Inquiry
                            └──→ (고객 정보 스냅샷)
```

- **Warehouse**: 창고 (현재 강남 1호점 1개)
- **Unit**: S/M/L/XL 사이즈 유닛 (총 50개)
- **Inquiry**: 고객 예약 문의
- **Contract**: 계약 (등록 시 유닛 상태 자동 변경)
- **Admin**: 관리자 계정
