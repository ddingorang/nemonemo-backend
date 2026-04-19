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

    private static final LocalDate TODAY = LocalDate.now();

    private static final String[] SURNAMES   = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임", "한", "오", "서", "신", "권"};
    private static final String[] GIVEN_NAMES = {"민준", "서연", "지호", "유나", "다은", "승우", "지수", "태양", "나영", "현식",
                                                  "보라", "지원", "수빈", "예진", "민서", "준혁", "하은", "지민", "수현", "도현"};

    private static final String[][] ADDRESSES = {
        {"경기도", "수원시", "팔달로",   "45"},
        {"서울특별시", "마포구", "월드컵로", "12"},
        {"부산광역시", "해운대구", "해운대해변로", "264"},
        {"경기도", "성남시", "분당로", "88"},
        {"서울특별시", "송파구", "올림픽로", "300"},
        {"인천광역시", "남동구", "소래로", "77"},
        {"대전광역시", "유성구", "대학로", "99"},
        {"경기도", "고양시", "중앙로", "151"},
        {"서울특별시", "강서구", "화곡로", "68"},
        {"광주광역시", "북구", "북문대로", "55"},
        {"경기도", "부천시", "경인로", "200"},
        {"서울특별시", "노원구", "동일로", "174"},
        {"대구광역시", "달서구", "달구벌대로", "1833"},
        {"경기도", "용인시", "중부대로", "35"},
        {"울산광역시", "남구", "삼산로", "123"},
        {"서울특별시", "관악구", "봉천로", "59"},
        {"경기도", "화성시", "봉담로", "22"},
        {"충청남도", "천안시", "불당대로", "16"},
        {"경기도", "파주시", "금촌로", "74"},
        {"서울특별시", "은평구", "연서로", "40"},
    };

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

        Random random = new Random(42);

        int nameIdx = 0;
        for (Unit unit : interleaved) {
            // 현재 활성 계약: 1~6개월 전 시작, 6~18개월 기간
            int activeStartMonthsAgo = 1 + random.nextInt(6);
            LocalDate activeStart = randomDate(random, TODAY.minusMonths(activeStartMonthsAgo));
            int activeDuration = 6 + random.nextInt(13);
            LocalDate activeEnd = activeStart.plusMonths(activeDuration).minusDays(1);

            contractRepository.save(buildContract(unit, nameIdx++, random, activeStart, activeEnd, ContractStatus.ACTIVE));

            // 과거 계약: 0~2개, 현재 계약 시작일 이전으로 역순 체인
            int pastCount = random.nextInt(3);
            LocalDate chainEnd = activeStart.minusDays(1 + random.nextInt(30));
            for (int p = 0; p < pastCount; p++) {
                int duration = 3 + random.nextInt(12);
                LocalDate pastStart = chainEnd.minusMonths(duration).plusDays(1);
                if (pastStart.isBefore(TODAY.minusMonths(36))) break;

                contractRepository.save(buildContract(unit, nameIdx++, random, pastStart, chainEnd, ContractStatus.EXPIRED));
                chainEnd = pastStart.minusDays(1 + random.nextInt(30));
            }
        }
    }

    private LocalDate randomDate(Random random, LocalDate base) {
        int day = Math.min(1 + random.nextInt(28), base.lengthOfMonth());
        return base.withDayOfMonth(day);
    }

    private Contract buildContract(Unit unit, int nameIdx, Random random, LocalDate startDate, LocalDate endDate, ContractStatus status) {
        String name = SURNAMES[nameIdx % SURNAMES.length] + GIVEN_NAMES[nameIdx % GIVEN_NAMES.length];
        String phone = String.format("010-%04d-%04d", 1000 + nameIdx, 1000 + (nameIdx * 7 + 3) % 9000);
        String[] addrParts = ADDRESSES[(nameIdx * 3) % ADDRESSES.length];
        String address = addrParts[0] + " " + addrParts[1] + " " + addrParts[2] + " " + addrParts[3];
        long months = Math.max(1, java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate));
        BigDecimal total = unit.getMonthlyPrice().multiply(BigDecimal.valueOf(months));

        return Contract.builder()
                .unit(unit)
                .customerName(name)
                .customerPhone(phone)
                .customerAddress(address)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(total)
                .status(status)
                .build();
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
