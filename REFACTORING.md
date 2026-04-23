# 백엔드 책임 분리 리팩토링

## 배경

서비스 계층이 존재함에도 일부 클래스가 레포지토리를 직접 참조하거나, 하나의 클래스에 성격이 다른 책임이 혼재하는 문제가 있었다. 4가지 사항을 식별하고 개선하였다.

---

## 1. 서비스 계층을 우회하는 레포지토리 직접 참조

### 문제

`DashboardService`, `AdminContractService`, `InquiryService` 세 곳에서 타 도메인의 레포지토리를 직접 주입받아 사용하고 있었다.

```java
// DashboardService — 3개 도메인 레포지토리 직접 의존
private final UnitRepository unitRepository;
private final ContractRepository contractRepository;
private final InquiryRepository inquiryRepository;

// AdminContractService — 계약 생성 시 문의 직접 조회
private final InquiryRepository inquiryRepository;

// InquiryService — 문의 제출 시 유닛 직접 조회
private final UnitRepository unitRepository;
```

각 도메인의 서비스(AdminUnitService, AdminInquiryService, UnitService)가 이미 존재하는데 그것을 우회하여 레포지토리에 직접 접근하면, 소프트 딜리트·활성 여부 등 도메인별 조회 정책이 적용되지 않을 위험이 생긴다.

### 개선

- **`DashboardService`**: `UnitRepository`, `ContractRepository`, `InquiryRepository` 제거 → `AdminUnitService`, `AdminContractService`, `AdminInquiryService` 주입
- **`AdminContractService`**: `InquiryRepository` 제거 → `AdminInquiryService.getInquiryEntity()` 호출
- **`InquiryService`**: `UnitRepository` 제거 → `UnitService.getUnitEntity()` 호출

각 서비스에 내부 서비스 간 호출을 위한 엔티티 반환 메서드와 집계 메서드를 추가하였다.

```java
// AdminInquiryService에 추가
public Inquiry getInquiryEntity(Long id) { ... }
public long countByStatus(InquiryStatus status) { ... }

// UnitService에 추가
public Unit getUnitEntity(Long id) { ... }

// AdminUnitService에 추가
public long countActive() { ... }
public long countActiveByStatus(UnitStatus status) { ... }

// AdminContractService에 추가
public List<ContractResponse> getExpiringSoon(LocalDate from, LocalDate to) { ... }
```

---

## 2. AdminContractService에 @Scheduled 혼재

### 문제

관리자 API 요청을 처리하는 서비스 클래스에 시스템 자동화 작업(`@Scheduled`)이 함께 존재하였다. API 처리와 스케줄링은 변경 이유가 다른 별개의 책임이다.

```java
// AdminContractService 내부에 혼재
public ContractResponse createContract(...) { ... }   // API 요청 처리
public ContractResponse terminateContract(...) { ... }

@Scheduled(cron = "0 0 1 * * *")                      // 시스템 자동화
public void processExpiredContracts() { ... }
```

### 개선

`@Scheduled`를 별도의 `ContractScheduler` 컴포넌트로 분리하였다. 스케줄러는 `AdminContractService.processExpiredContracts()`를 호출하는 역할만 담당한다.

```java
// domain/contract/scheduler/ContractScheduler.java (신규)
@Component
public class ContractScheduler {
    private final AdminContractService adminContractService;

    @Scheduled(cron = "0 0 1 * * *")
    public void processExpiredContracts() {
        adminContractService.processExpiredContracts();
    }
}
```

---

## 3. 계약 삭제 책임이 유닛 서비스에 있음

### 문제

`AdminUnitService.deleteUnitContracts()`가 유닛 서비스 내부에서 계약 삭제 작업을 직접 수행하고 있었다. 계약 생명주기 관리는 `AdminContractService`의 책임이다.

```java
// AdminUnitService — 계약 도메인 작업이 유닛 서비스에 존재
public void deleteUnitContracts(Long id) {
    contractRepository.deleteAllByUnitId(unit.getId()); // 계약 삭제
    unit.changeStatus(UnitStatus.AVAILABLE);
}
```

### 개선

해당 메서드를 `AdminContractService.deleteContractsByUnit()`으로 이동하였다. `AdminUnitController`는 이 작업을 `AdminContractService`에 위임한다.

```java
// AdminContractService로 이동
public void deleteContractsByUnit(Long unitId) {
    Unit unit = unitRepository.findByIdAndIsActiveTrue(unitId)
            .orElseThrow(() -> new BusinessException(ErrorCode.UNIT_NOT_FOUND));
    contractRepository.deleteAllByUnitId(unit.getId());
    unit.changeStatus(UnitStatus.AVAILABLE);
}

// AdminUnitController
public void deleteUnitContracts(@PathVariable Long id) {
    adminContractService.deleteContractsByUnit(id); // AdminContractService에 위임
}
```

---

## 4. UnitService / AdminUnitService 필터링 로직 중복

### 문제

두 서비스에 `size`, `status` 조합에 따른 동일한 if-else 분기가 반복되어 있었다.

```java
// UnitService와 AdminUnitService에 동일한 코드 중복
if (size != null && status != null) {
    units = unitRepository.findAllByIsActiveTrueAndSizeAndStatus(size, status);
} else if (size != null) {
    units = unitRepository.findAllByIsActiveTrueAndSize(size);
} else if (status != null) {
    units = unitRepository.findAllByIsActiveTrueAndStatus(status);
} else {
    units = unitRepository.findAllByIsActiveTrue();
}
```

### 개선

`UnitRepository`에 JPQL 옵셔널 파라미터 쿼리(`findAllByFilter`)를 추가하여 분기 로직을 레포지토리로 위임하였다.

```java
// UnitRepository에 추가
@Query("SELECT u FROM Unit u WHERE u.isActive = true " +
       "AND (:size IS NULL OR u.size = :size) " +
       "AND (:status IS NULL OR u.status = :status)")
List<Unit> findAllByFilter(@Param("size") UnitSize size, @Param("status") UnitStatus status);

// UnitService / AdminUnitService — 4줄로 단순화
return unitRepository.findAllByFilter(size, status).stream()
        .map(u -> UnitResponse.from(u, expiringSoonUnitIds.contains(u.getId())))
        .toList();
```

---

## 변경 파일 목록

| 파일 | 변경 유형 |
|------|-----------|
| `domain/unit/repository/UnitRepository.java` | `findAllByFilter` 추가, 개별 메서드 제거 |
| `domain/unit/service/UnitService.java` | `findAllByFilter` 사용, `getUnitEntity` 추가 |
| `domain/unit/service/AdminUnitService.java` | `findAllByFilter` 사용, `deleteUnitContracts` 제거, count 메서드 추가 |
| `domain/inquiry/service/InquiryService.java` | `UnitRepository` → `UnitService` |
| `domain/inquiry/service/AdminInquiryService.java` | `getInquiryEntity`, `countByStatus` 추가 |
| `domain/contract/service/AdminContractService.java` | `@Scheduled` 제거, `InquiryRepository` → `AdminInquiryService`, `deleteContractsByUnit`·`getExpiringSoon` 추가 |
| `domain/contract/scheduler/ContractScheduler.java` | **신규** — `@Scheduled` 전담 |
| `domain/dashboard/service/DashboardService.java` | 레포지토리 → 서비스 계층 |
| `api/admin/AdminUnitController.java` | `deleteUnitContracts` → `AdminContractService` 위임 |
