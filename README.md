# 네모네모 스토리지 — Backend

공유 스토리지(창고 파티션 임대) 예약 및 계약 관리 시스템의 백엔드 API 서버입니다.

---

## 서비스 개요

하나의 대형 창고를 S/M/L/XL 사이즈의 소형 유닛으로 파티션 분할하여 임대하는 공유 스토리지 사업을 위한 관리 시스템입니다.

**핵심 플로우**

```
고객 문의 → 관리자 확인 → 유선/대면 상담 → 계약 체결(오프라인) → 관리자가 시스템에 계약 등록
```

| 역할 | 기능 | 인증 |
|------|------|------|
| 고객 | 유닛 현황 조회, 예약 문의 제출 | 불필요 (비회원) |
| 관리자 | 문의/계약/유닛/대시보드 전체 관리 | JWT 로그인 필수 |

---

## 주요 기능

### 고객 기능

- **유닛 현황 조회**: 전체 유닛의 사이즈(S/M/L/XL), 상태(가용/사용중/문의중), 면적, 월 임대료 확인. 사이즈·상태 필터 지원
- **예약 문의 제출**: 이름, 연락처, 희망 사이즈, 희망 사용 시작일 등 입력. 동일 연락처 중복 문의 방지

### 관리자 기능

- **유닛 관리**: 유닛 목록 조회/등록/수정/비활성화. 상태 수동 변경 (가용/유지보수 등)
- **문의 관리**: 문의 목록 조회, 상태 변경(`PENDING` → `IN_PROGRESS` → `COMPLETED`/`CANCELLED`), 내부 메모 추가
- **계약 관리**:
  - 계약 등록 시 유닛 상태 `AVAILABLE` → `OCCUPIED` 자동 변경
  - 계약 해지/만료 시 `OCCUPIED` → `AVAILABLE` 자동 복원
  - 매일 01:00 스케줄러로 만료 계약 자동 처리
  - 만료 30일 전 유닛에 만료 임박 표시
- **대시보드**: 유닛 상태별 현황 요약, 이번 달 만료 예정 계약 목록, 미처리 문의 건수

### 주요 비즈니스 규칙

1. 동일 유닛에 `ACTIVE` 계약이 존재하면 신규 계약 등록 불가
2. 계약은 삭제하지 않고 `TERMINATED`/`EXPIRED` 상태로 관리 (이력 보존)
3. 유닛은 소프트 삭제(`is_active = false`)만 허용

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| 언어/프레임워크 | Java 21 / Spring Boot 3.5 |
| 데이터 접근 | Spring Data JPA + QueryDSL |
| 데이터베이스 | PostgreSQL (Supabase) |
| 인증 | Spring Security + JWT (jjwt 0.12.3) |
| API 문서 | SpringDoc OpenAPI (Swagger UI) |
| 빌드 | Gradle |

---

## 도메인 모델

```
Warehouse ─── Unit ─── Contract ──→ Inquiry
                            └──→ (고객 정보 스냅샷)
```

| 엔티티 | 설명 |
|--------|------|
| Warehouse | 창고 (현재 강남 1호점 1개) |
| Unit | S/M/L/XL 사이즈 유닛 (총 50개) |
| Inquiry | 고객 예약 문의 |
| Contract | 계약 (등록/해지 시 유닛 상태 자동 연동) |
| Admin | 관리자 계정 |

---

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

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 배포 아키텍처

```
GitHub (main)
    │
    │ push → webhook
    ▼
Jenkins (빌드 서버)
    ├─ 1. Gradle build & test
    ├─ 2. Docker build → AWS ECR push (ap-northeast-2)
    │       이미지 태그: :latest + :<build_number>
    │
    └─ 3. SSH → EC2 배포 서버
               docker pull → docker run -p 8080:8080
               환경변수: Jenkins Credentials로 주입

EC2 (백엔드 서버, 8080)
    └─ Docker 컨테이너: Spring Boot
            ↕ JDBC (Supabase Connection Pooler, 6543)
        Supabase (PostgreSQL 17)

Vercel
    └─ React 프론트엔드 → EC2:8080 API 호출
```

**주요 설정**

| 항목 | 내용 |
|------|------|
| CI 트리거 | `main` 브랜치 push → Jenkins 자동 실행 (`githubPush()`) |
| 컨테이너 레지스트리 | AWS ECR (`nemonemo` 레포지토리) |
| DB 연결 | Supabase Connection Pooler (`aws-1-ap-south-1`, 포트 6543) |
| DB Username 형식 | `postgres.{project-ref}` (Pooler 멀티테넌트 필수 형식) |
| IPv4 강제 | `-Djava.net.preferIPv4Stack=true` (Supabase DNS IPv6 이슈 우회) |
| 민감 정보 | Jenkins Credentials로 관리 (`PROD_DB_URL`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD`, `PROD_JWT_SECRET`) |
| CORS | Vercel 프론트엔드 origin 허용 설정 완료 |

배포 과정에서 겪은 시행착오는 [DEPLOYMENT_HISTORY.md](docs/DEPLOYMENT_HISTORY.md)에 기록되어 있습니다.

---

## 로컬 실행 방법

### 1. PostgreSQL 실행 (Docker)

```bash
docker run -d --name nemonemo-postgres \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=nemostorage \
  -p 5432:5432 postgres:15
```

### 2. 앱 실행

```bash
DB_URL=jdbc:postgresql://localhost:5432/nemostorage \
DB_USERNAME=postgres \
DB_PASSWORD=password \
./gradlew bootRun
```

서버: `http://localhost:8080`

---

## 초기 관리자 계정

| 항목 | 값 |
|------|----|
| 아이디 | `admin` |
| 비밀번호 | `admin1234` |
