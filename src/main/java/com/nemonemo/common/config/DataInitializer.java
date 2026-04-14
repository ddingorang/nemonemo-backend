// Created: 2026-04-07 22:43:25
package com.nemonemo.common.config;

import com.nemonemo.domain.admin.entity.Admin;
import com.nemonemo.domain.admin.repository.AdminRepository;
import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.contract.entity.ContractStatus;
import com.nemonemo.domain.contract.repository.ContractRepository;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.repository.UnitRepository;
import com.nemonemo.domain.warehouse.entity.Warehouse;
import com.nemonemo.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final WarehouseRepository warehouseRepository;
    private final UnitRepository unitRepository;
    private final AdminRepository adminRepository;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.count() == 0) {
            adminRepository.save(Admin.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin1234"))
                    .name("관리자")
                    .build());
        }

        if (warehouseRepository.count() > 0) {
            return;
        }

        Warehouse warehouse = warehouseRepository.save(
                Warehouse.builder()
                        .name("네모네모 1호점")
                        .address("서울특별시 강남구 테헤란로 123")
                        .description("강남역 도보 5분, 24시간 이용 가능한 공유 스토리지")
                        .build()
        );

        java.util.ArrayList<Unit> units = new java.util.ArrayList<>();

        // S 유닛 25개 (3㎡) — 1~3층
        UnitStatus[] sSample = {UnitStatus.AVAILABLE, UnitStatus.AVAILABLE, UnitStatus.OCCUPIED, UnitStatus.AVAILABLE, UnitStatus.AVAILABLE};
        for (int i = 1; i <= 25; i++) {
            units.add(buildUnit(warehouse, String.format("S-%02d", i), UnitSize.S, "A",
                    (i <= 10 ? 1 : i <= 20 ? 2 : 3), new BigDecimal("3.00"), new BigDecimal("50000"),
                    sSample[(i - 1) % sSample.length]));
        }

        // M 유닛 10개 (6㎡) — 1~2층
        UnitStatus[] mSample = {UnitStatus.AVAILABLE, UnitStatus.OCCUPIED, UnitStatus.AVAILABLE, UnitStatus.RESERVED, UnitStatus.AVAILABLE};
        for (int i = 1; i <= 10; i++) {
            units.add(buildUnit(warehouse, String.format("M-%02d", i), UnitSize.M, "B",
                    (i <= 5 ? 1 : 2), new BigDecimal("6.00"), new BigDecimal("90000"),
                    mSample[(i - 1) % mSample.length]));
        }

        // L 유닛 10개 (12㎡) — 1~2층
        UnitStatus[] lSample = {UnitStatus.AVAILABLE, UnitStatus.OCCUPIED, UnitStatus.AVAILABLE, UnitStatus.AVAILABLE, UnitStatus.MAINTENANCE};
        for (int i = 1; i <= 10; i++) {
            units.add(buildUnit(warehouse, String.format("L-%02d", i), UnitSize.L, "C",
                    (i <= 5 ? 1 : 2), new BigDecimal("12.00"), new BigDecimal("160000"),
                    lSample[(i - 1) % lSample.length]));
        }

        // XL 유닛 5개 (20㎡) — 1층
        UnitStatus[] xlSample = {UnitStatus.AVAILABLE, UnitStatus.OCCUPIED, UnitStatus.AVAILABLE, UnitStatus.AVAILABLE, UnitStatus.OCCUPIED};
        for (int i = 1; i <= 5; i++) {
            units.add(buildUnit(warehouse, String.format("XL-%02d", i), UnitSize.XL, "D",
                    1, new BigDecimal("20.00"), new BigDecimal("250000"),
                    xlSample[i - 1]));
        }

        List<Unit> saved = unitRepository.saveAll(units);

        // 샘플 계약 데이터 (OCCUPIED 유닛에 매핑)
        record SampleContract(String unitNumber, String name, String phone, String email,
                              LocalDate start, LocalDate end, BigDecimal price) {}

        // totalPrice = 월 임대료 × 계약 개월 수
        List<SampleContract> samples = List.of(
            new SampleContract("S-03", "김민준", "010-1234-5678", "minjun@email.com",  LocalDate.of(2026, 1, 1),  LocalDate.of(2026, 7, 31),  new BigDecimal("350000")),  // 7개월 × 50,000
            new SampleContract("S-08", "이서연", "010-2345-6789", null,                LocalDate.of(2026, 2, 1),  LocalDate.of(2026, 8, 31),  new BigDecimal("350000")),  // 7개월 × 50,000
            new SampleContract("S-13", "박지호", "010-3456-7890", "jiho@email.com",    LocalDate.of(2026, 3, 1),  LocalDate.of(2026, 4, 14),  new BigDecimal("75000")),   // 1.5개월 × 50,000
            new SampleContract("S-18", "최유나", "010-4567-8901", null,                LocalDate.of(2025, 11, 1), LocalDate.of(2026, 10, 31), new BigDecimal("600000")),  // 12개월 × 50,000
            new SampleContract("S-23", "정다은", "010-5678-9012", "daeun@email.com",   LocalDate.of(2026, 1, 15), LocalDate.of(2026, 12, 31), new BigDecimal("575000")),  // 11.5개월 × 50,000
            new SampleContract("M-02", "한승우", "010-6789-0123", "seungwoo@email.com",LocalDate.of(2025, 12, 1), LocalDate.of(2026, 5, 31),  new BigDecimal("540000")),  // 6개월 × 90,000
            new SampleContract("M-07", "오지수", "010-7890-1234", null,                LocalDate.of(2026, 2, 15), LocalDate.of(2026, 9, 14),  new BigDecimal("630000")),  // 7개월 × 90,000
            new SampleContract("L-02", "윤태양", "010-8901-2345", "taeyang@email.com", LocalDate.of(2026, 1, 1),  LocalDate.of(2026, 6, 30),  new BigDecimal("960000")),  // 6개월 × 160,000
            new SampleContract("L-07", "임나영", "010-9012-3456", null,                LocalDate.of(2025, 10, 1), LocalDate.of(2026, 4, 10),  new BigDecimal("1040000")), // 6.5개월 × 160,000
            new SampleContract("XL-02", "강현식", "010-0123-4567", "hyunsik@email.com", LocalDate.of(2025, 9, 1),  LocalDate.of(2026, 8, 31),  new BigDecimal("3000000")), // 12개월 × 250,000
            new SampleContract("XL-05", "신보라", "010-1357-2468", "bora@email.com",    LocalDate.of(2026, 3, 1),  LocalDate.of(2026, 12, 31), new BigDecimal("2500000"))  // 10개월 × 250,000
        );

        java.util.Map<String, Unit> unitMap = saved.stream()
                .collect(java.util.stream.Collectors.toMap(Unit::getUnitNumber, u -> u));

        for (SampleContract s : samples) {
            Unit unit = unitMap.get(s.unitNumber());
            if (unit == null) continue;
            contractRepository.save(Contract.builder()
                    .unit(unit)
                    .customerName(s.name())
                    .customerPhone(s.phone())
                    .customerEmail(s.email())
                    .startDate(s.start())
                    .endDate(s.end())
                    .totalPrice(s.price())
                    .status(ContractStatus.ACTIVE)
                    .build());
        }
    }

    private Unit buildUnit(Warehouse warehouse, String unitNumber, UnitSize size,
                           String zone, int floor, BigDecimal areaSqm,
                           BigDecimal monthlyPrice, UnitStatus status) {
        return Unit.builder()
                .warehouse(warehouse)
                .unitNumber(unitNumber)
                .size(size)
                .zone(zone)
                .floor(floor)
                .areaSqm(areaSqm)
                .monthlyPrice(monthlyPrice)
                .status(status)
                .build();
    }
}
