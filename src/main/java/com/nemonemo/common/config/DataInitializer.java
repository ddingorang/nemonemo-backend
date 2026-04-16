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
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final WarehouseRepository warehouseRepository;
    private final UnitRepository unitRepository;
    private final AdminRepository adminRepository;
    private final ContractRepository contractRepository;
    private final PasswordEncoder passwordEncoder;

    // 현재 날짜 기준 (2026-04-15)
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 15);

    private static final String[] SURNAMES   = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권"};
    private static final String[] GIVEN_NAMES = {"민준", "서연", "지호", "유나", "다은", "승우", "지수", "태양", "나영", "현식",
                                                  "보라", "지원", "수빈", "예진", "민서", "준혁", "하은", "지민", "수현", "도현"};
    private static final String[] DOMAINS    = {"gmail.com", "naver.com", "kakao.com", "daum.net"};

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

        List<Unit> units = new ArrayList<>();

        // XS 50개 (35 OCCUPIED / 15 AVAILABLE) — A존
        addUnits(units, warehouse, "XS", UnitSize.XS, "A", new BigDecimal("30000"), 50, 35);

        // S 60개 (42 OCCUPIED / 18 AVAILABLE) — B존
        addUnits(units, warehouse, "S",  UnitSize.S,  "B", new BigDecimal("50000"), 60, 42);

        // M 26개 (18 OCCUPIED / 8 AVAILABLE) — C존
        addUnits(units, warehouse, "M",  UnitSize.M,  "C", new BigDecimal("90000"), 26, 18);

        // L 11개 (8 OCCUPIED / 3 AVAILABLE) — D존
        addUnits(units, warehouse, "L",  UnitSize.L,  "D", new BigDecimal("160000"), 11, 8);

        // XL 4개 (3 OCCUPIED / 1 AVAILABLE) — E존
        addUnits(units, warehouse, "XL", UnitSize.XL, "E", new BigDecimal("250000"), 4, 3);

        List<Unit> saved = unitRepository.saveAll(units);

        // 사이즈별로 OCCUPIED 유닛을 분리한 뒤 round-robin 인터리빙
        // → 계약 목록에서 XS끼리·S끼리 뭉치지 않도록
        UnitSize[] sizeOrder = {UnitSize.XS, UnitSize.S, UnitSize.M, UnitSize.L, UnitSize.XL};
        Map<UnitSize, List<Unit>> bySize = new EnumMap<>(UnitSize.class);
        for (UnitSize sz : sizeOrder) bySize.put(sz, new ArrayList<>());
        for (Unit u : saved) {
            if (u.getStatus() == UnitStatus.OCCUPIED) bySize.get(u.getSize()).add(u);
        }

        List<Unit> interleaved = new ArrayList<>();
        int maxLen = bySize.values().stream().mapToInt(List::size).max().orElse(0);
        for (int i = 0; i < maxLen; i++) {
            for (UnitSize sz : sizeOrder) {
                List<Unit> group = bySize.get(sz);
                if (i < group.size()) interleaved.add(group.get(i));
            }
        }

        // 시작일 변화를 위한 일자(day) 팔레트 — 소수 스텝으로 고루 분산
        int[] START_DAYS = {1, 3, 5, 7, 10, 12, 14, 15, 17, 19, 20, 22, 25, 27, 28};

        int idx = 0;
        for (Unit unit : interleaved) {
            String name  = SURNAMES[idx % SURNAMES.length] + GIVEN_NAMES[idx % GIVEN_NAMES.length];
            String phone = String.format("010-%04d-%04d", 1000 + idx, 1000 + (idx * 7 + 3) % 9000);
            String email = idx % 3 == 0 ? null
                                        : "user" + (idx + 1) + "@" + DOMAINS[idx % DOMAINS.length];

            // 시작 시점: 1~12개월 전, 소수 스텝(×5)으로 패턴 분산
            int startMonthsAgo = 1 + (idx * 5) % 12;
            // 시작 일자: START_DAYS 팔레트에서 소수 스텝(×11)으로 선택
            int startDay = START_DAYS[(idx * 11) % START_DAYS.length];
            // 계약 기간: 6~17개월, 소수 스텝(×7)으로 분산
            int durationMonths = 6 + (idx * 7) % 12;

            LocalDate base      = TODAY.minusMonths(startMonthsAgo);
            int       clampedDay = Math.min(startDay, base.lengthOfMonth());
            LocalDate startDate  = base.withDayOfMonth(clampedDay);
            LocalDate endDate    = startDate.plusMonths(durationMonths).minusDays(1);
            BigDecimal total     = unit.getMonthlyPrice().multiply(BigDecimal.valueOf(durationMonths));

            contractRepository.save(Contract.builder()
                    .unit(unit)
                    .customerName(name)
                    .customerPhone(phone)
                    .customerEmail(email)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalPrice(total)
                    .status(ContractStatus.ACTIVE)
                    .build());

            idx++;
        }
    }

    /** 유닛을 count개 생성하고, occupiedCount개를 랜덤하게 분산하여 OCCUPIED로, 나머지를 AVAILABLE로 설정 */
    private void addUnits(List<Unit> list, Warehouse warehouse,
                          String prefix, UnitSize size, String zone,
                          BigDecimal monthlyPrice, int count, int occupiedCount) {
        // 재현 가능한 시드로 섞어서 빈 칸이 드문드문 생기도록 분산
        List<Integer> indices = new ArrayList<>();
        for (int i = 1; i <= count; i++) indices.add(i);
        Collections.shuffle(indices, new Random(prefix.hashCode()));

        java.util.Set<Integer> occupiedSet = new java.util.HashSet<>(indices.subList(0, occupiedCount));

        for (int i = 1; i <= count; i++) {
            UnitStatus status = occupiedSet.contains(i) ? UnitStatus.OCCUPIED : UnitStatus.AVAILABLE;
            list.add(buildUnit(warehouse, String.format("%s-%02d", prefix, i),
                    size, zone, monthlyPrice, status));
        }
    }

    private Unit buildUnit(Warehouse warehouse, String unitNumber, UnitSize size,
                           String zone, BigDecimal monthlyPrice, UnitStatus status) {
        return Unit.builder()
                .warehouse(warehouse)
                .unitNumber(unitNumber)
                .size(size)
                .zone(zone)
                .monthlyPrice(monthlyPrice)
                .status(status)
                .build();
    }
}
