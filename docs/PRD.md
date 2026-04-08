# Created: 2026-04-07 22:36:17
# 공유 스토리지 예약 관리 시스템 PRD

## 1. 프로젝트 개요

### 배경 및 목적
하나의 대형 창고를 S/M/L/XL 사이즈의 소형 유닛으로 파티션 분할하여 임대하는 공유 스토리지 사업을 위한 관리 시스템이다.
고객은 웹페이지를 통해 현재 유닛 사용 현황을 확인하고 원하는 공간에 대해 예약 문의를 남길 수 있으며,
관리자는 전용 관리 시스템을 통해 문의 접수 및 계약 현황을 통합 관리한다.

### 핵심 플로우
```
고객 문의 → 관리자 확인 → 유선/대면 상담 → 계약 체결(오프라인) → 관리자가 시스템에 계약 등록
```

---

## 2. 사용자 및 역할

| 구분 | 역할 | 인증 |
|------|------|------|
| 고객 (Customer) | 유닛 현황 조회, 예약 문의 제출 | 불필요 (비회원) |
| 관리자 (Admin) | 전체 시스템 관리 (문의/계약/유닛) | 로그인 필수 (JWT) |

---

## 3. 기능 요구사항

### 3.1 고객 기능

#### F-C01. 유닛 현황 조회
- 창고 내 전체 유닛의 사용 현황을 조회할 수 있다.
- 유닛별 사이즈(S/M/L/XL), 상태(가용/사용중/문의중), 면적, 월 임대료를 확인할 수 있다.
- 사이즈, 상태로 필터링할 수 있다.

#### F-C02. 예약 문의 제출
- 특정 유닛 또는 특정 사이즈에 대해 예약 문의를 제출할 수 있다.
- 입력 항목: 이름, 연락처, 이메일(선택), 희망 사이즈, 희망 사용 시작일, 희망 사용 기간(월), 문의 내용(선택)
- 제출 후 접수 완료 안내가 표시된다.
- 동일 연락처로 동시에 여러 건의 대기 중 문의를 남길 수 없다. (중복 방지)

#### F-C03. 문의 상태 확인 (선택 기능)
- 연락처와 문의 접수 번호로 본인의 문의 상태를 조회할 수 있다.

---

### 3.2 관리자 기능

#### F-A01. 관리자 인증
- 아이디/비밀번호로 로그인한다.
- JWT 토큰 기반 인증을 사용한다.
- 토큰 만료 시 재로그인이 필요하다.

#### F-A02. 유닛(Unit) 관리
- 유닛 목록 조회 (필터: 사이즈, 상태, 구역)
- 유닛 등록 / 수정 / 비활성화 (삭제 대신 상태 변경)
- 유닛 상태 수동 변경 (가용/유지보수 등)

#### F-A03. 예약 문의 관리
- 문의 목록 조회 (필터: 상태, 날짜, 사이즈)
- 문의 상세 조회
- 문의 상태 변경: `PENDING` → `IN_PROGRESS` → `COMPLETED` / `CANCELLED`
- 문의에 내부 메모 추가

#### F-A04. 계약 관리
- 계약 등록: 상담 완료 후 관리자가 직접 입력
  - 연결 유닛, 고객 정보(이름, 연락처, 이메일), 시작일, 종료일, 월 임대료
- 계약 목록 조회 (필터: 상태, 만료 임박, 유닛)
- 계약 수정 / 종료(해지)
- 계약 만료 30일 전 유닛에 만료 임박 표시

#### F-A05. 대시보드
- 전체 유닛 수 / 사용 중 / 가용 / 유지보수 수량 요약
- 이번 달 만료 예정 계약 목록
- 신규 미처리 문의 건수

---

## 4. 비기능 요구사항

| 항목 | 요구사항 |
|------|---------|
| 보안 | 관리자 API 전 구간 JWT 인증, 비밀번호 BCrypt 암호화 |
| 데이터 정합성 | 계약 등록 시 유닛 상태 자동 갱신, 계약 종료 시 유닛 상태 복원 |
| 확장성 | 추후 다중 창고(멀티 웨어하우스) 지원 가능한 구조 |
| API 설계 | RESTful, JSON 응답, 공통 응답 포맷 |

---

## 5. 도메인 모델

### 5.1 엔티티 목록

```
Warehouse (창고)
Unit (유닛)
Inquiry (예약 문의)
Contract (계약)
Admin (관리자)
```

### 5.2 ERD (논리 모델)

```
Warehouse
├── id (PK)
├── name          -- 창고 이름 (예: 강남 1호점)
├── address       -- 주소
└── description

Unit
├── id (PK)
├── warehouse_id (FK → Warehouse)
├── unit_number   -- 유닛 호수 (예: A-01)
├── size          -- ENUM: S, M, L, XL
├── area_sqm      -- 면적 (㎡)
├── floor         -- 층
├── zone          -- 구역 (예: A, B, C)
├── monthly_price -- 월 임대료
├── status        -- ENUM: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
└── is_active     -- 활성화 여부

Inquiry
├── id (PK)
├── unit_id (FK → Unit, nullable) -- 특정 유닛 지정 문의
├── desired_size  -- ENUM: S, M, L, XL (유닛 미지정 시 사용)
├── customer_name
├── customer_phone
├── customer_email (nullable)
├── desired_start_date
├── desired_duration_months
├── message (nullable)
├── admin_memo (nullable)  -- 관리자 내부 메모
├── status        -- ENUM: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
├── created_at
└── updated_at

Contract
├── id (PK)
├── unit_id (FK → Unit)
├── inquiry_id (FK → Inquiry, nullable) -- 연결된 문의
├── customer_name
├── customer_phone
├── customer_email (nullable)
├── start_date
├── end_date
├── monthly_price -- 계약 시점 임대료 스냅샷
├── status        -- ENUM: ACTIVE, EXPIRED, TERMINATED
├── created_at
└── updated_at

Admin
├── id (PK)
├── username
├── password      -- BCrypt 해시
├── name
└── created_at
```

### 5.3 유닛 상태 전이

```
AVAILABLE ──[계약 등록]──▶ OCCUPIED
          ◀──[계약 종료]──

AVAILABLE ──[문의 접수]──▶ RESERVED (선택적, 운영 정책에 따라)
          ◀──[문의 취소]──

AVAILABLE ──[관리자 변경]──▶ MAINTENANCE
          ◀──[관리자 변경]──
```

---

## 6. API 설계

### 6.1 공개 API (인증 불필요)

| Method | URI | 설명 |
|--------|-----|------|
| GET | `/api/units` | 유닛 현황 목록 조회 |
| GET | `/api/units/{id}` | 유닛 상세 조회 |
| POST | `/api/inquiries` | 예약 문의 제출 |
| GET | `/api/inquiries/{id}` | 문의 상태 조회 (연락처 검증) |

**GET /api/units 쿼리 파라미터**
- `size`: S, M, L, XL
- `status`: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE

### 6.2 관리자 API (JWT 인증 필요)

**인증**
| Method | URI | 설명 |
|--------|-----|------|
| POST | `/api/admin/auth/login` | 로그인, JWT 발급 |
| POST | `/api/admin/auth/refresh` | 토큰 갱신 |

**유닛 관리**
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/api/admin/units` | 유닛 목록 조회 |
| POST | `/api/admin/units` | 유닛 등록 |
| PUT | `/api/admin/units/{id}` | 유닛 수정 |
| PATCH | `/api/admin/units/{id}/status` | 유닛 상태 변경 |

**문의 관리**
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/api/admin/inquiries` | 문의 목록 조회 |
| GET | `/api/admin/inquiries/{id}` | 문의 상세 조회 |
| PATCH | `/api/admin/inquiries/{id}/status` | 문의 상태 변경 |
| PATCH | `/api/admin/inquiries/{id}/memo` | 관리자 메모 수정 |

**계약 관리**
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/api/admin/contracts` | 계약 목록 조회 |
| GET | `/api/admin/contracts/{id}` | 계약 상세 조회 |
| POST | `/api/admin/contracts` | 계약 등록 |
| PUT | `/api/admin/contracts/{id}` | 계약 수정 |
| PATCH | `/api/admin/contracts/{id}/terminate` | 계약 해지 |

**대시보드**
| Method | URI | 설명 |
|--------|-----|------|
| GET | `/api/admin/dashboard` | 현황 요약 통계 |

### 6.3 공통 응답 포맷

```json
{
  "success": true,
  "data": { ... },
  "message": null,
  "timestamp": "2026-04-07T10:00:00"
}
```

오류 응답:
```json
{
  "success": false,
  "data": null,
  "message": "유닛을 찾을 수 없습니다.",
  "errorCode": "UNIT_NOT_FOUND",
  "timestamp": "2026-04-07T10:00:00"
}
```

---

## 7. 기술 스택

| 구분 | 기술 |
|------|------|
| 언어 | Java 17 |
| 프레임워크 | Spring Boot 3.x |
| 데이터 접근 | Spring Data JPA + QueryDSL (복잡한 필터링 쿼리용) |
| 데이터베이스 | MySQL 8.x |
| 인증 | Spring Security + JWT (jjwt 라이브러리) |
| API 문서 | SpringDoc OpenAPI (Swagger UI) |
| 빌드 | Gradle |
| 검증 | Spring Validation (Bean Validation) |

---

## 8. 프로젝트 구조 (패키지)

```
com.nemostorage
├── domain
│   ├── warehouse
│   │   ├── entity/Warehouse.java
│   │   ├── repository/WarehouseRepository.java
│   │   └── service/WarehouseService.java
│   ├── unit
│   │   ├── entity/Unit.java
│   │   ├── entity/UnitSize.java       (enum)
│   │   ├── entity/UnitStatus.java     (enum)
│   │   ├── repository/UnitRepository.java
│   │   └── service/UnitService.java
│   ├── inquiry
│   │   ├── entity/Inquiry.java
│   │   ├── entity/InquiryStatus.java  (enum)
│   │   ├── repository/InquiryRepository.java
│   │   └── service/InquiryService.java
│   ├── contract
│   │   ├── entity/Contract.java
│   │   ├── entity/ContractStatus.java (enum)
│   │   ├── repository/ContractRepository.java
│   │   └── service/ContractService.java
│   └── admin
│       ├── entity/Admin.java
│       ├── repository/AdminRepository.java
│       └── service/AdminService.java
├── api
│   ├── public
│   │   ├── UnitController.java
│   │   └── InquiryController.java
│   └── admin
│       ├── AuthController.java
│       ├── AdminUnitController.java
│       ├── AdminInquiryController.java
│       ├── AdminContractController.java
│       └── AdminDashboardController.java
├── common
│   ├── response/ApiResponse.java
│   ├── exception/GlobalExceptionHandler.java
│   ├── exception/ErrorCode.java
│   └── security
│       ├── JwtProvider.java
│       ├── JwtFilter.java
│       └── SecurityConfig.java
└── NemostorageApplication.java
```

---

## 9. 개발 단계 (Phase)

### Phase 1 — 핵심 조회 및 문의 API
- [ ] 프로젝트 세팅 (Spring Boot, MySQL, JPA)
- [ ] 공통 응답 포맷 및 예외 처리 구조
- [ ] Warehouse / Unit 엔티티 및 조회 API
- [ ] Inquiry 엔티티 및 문의 제출 API

### Phase 2 — 관리자 인증 및 관리 API
- [ ] Admin 엔티티 및 Spring Security + JWT 인증
- [ ] 관리자 유닛 관리 API
- [ ] 관리자 문의 관리 API (상태 변경, 메모)

### Phase 3 — 계약 관리 및 비즈니스 로직
- [ ] Contract 엔티티 및 계약 CRUD API
- [ ] 계약 등록 시 유닛 상태 자동 변경
- [ ] 계약 만료 감지 (스케줄러, @Scheduled)

### Phase 4 — 대시보드 및 마무리
- [ ] 대시보드 집계 API
- [ ] Swagger 문서화
- [ ] 초기 데이터 시딩 (관리자 계정, 샘플 유닛)

---

## 10. 주요 비즈니스 규칙

1. **유닛 중복 계약 불가**: 동일 유닛에 `ACTIVE` 상태의 계약이 있으면 신규 계약 등록 불가
2. **계약 등록 시 유닛 상태 변경**: `AVAILABLE` → `OCCUPIED` 자동 처리
3. **계약 해지/만료 시 유닛 복원**: `OCCUPIED` → `AVAILABLE` 자동 처리
4. **문의 중복 제한**: 동일 연락처로 `PENDING` 또는 `IN_PROGRESS` 상태 문의가 이미 있으면 추가 제출 불가
5. **계약 삭제 불가**: 계약은 삭제하지 않고 `TERMINATED` 또는 `EXPIRED` 상태로 관리 (이력 보존)
6. **유닛 삭제 불가**: `is_active = false` 처리로 소프트 삭제
