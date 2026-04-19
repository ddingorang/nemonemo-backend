// Created: 2026-04-19 21:00:10
package com.nemonemo.domain.dashboard.service;

import com.nemonemo.domain.contract.dto.ContractStatRaw;
import com.nemonemo.domain.contract.repository.ContractQueryRepository;
import com.nemonemo.domain.dashboard.dto.ContractStatItem;
import com.nemonemo.domain.dashboard.dto.ContractStatsResponse;
import com.nemonemo.domain.unit.entity.UnitSize;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final ContractQueryRepository contractQueryRepository;

    public ContractStatsResponse getMonthlyStats(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        Map<Integer, List<ContractStatRaw>> byMonth = contractQueryRepository.findMonthlyStats(from, to)
                .stream().collect(Collectors.groupingBy(ContractStatRaw::periodKey));

        List<ContractStatItem> items = IntStream.rangeClosed(1, 12)
                .mapToObj(month -> toItem(String.format("%d-%02d", year, month), byMonth.get(month)))
                .toList();

        return toResponse(year, items);
    }

    public ContractStatsResponse getQuarterlyStats(int year) {
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = LocalDate.of(year, 12, 31);

        Map<Integer, List<ContractStatRaw>> byQuarter = contractQueryRepository.findQuarterlyStats(from, to)
                .stream().collect(Collectors.groupingBy(ContractStatRaw::periodKey));

        List<ContractStatItem> items = IntStream.rangeClosed(1, 4)
                .mapToObj(q -> toItem(String.format("%d-Q%d", year, q), byQuarter.get(q)))
                .toList();

        return toResponse(year, items);
    }

    private ContractStatItem toItem(String label, List<ContractStatRaw> rows) {
        if (rows == null || rows.isEmpty()) {
            Map<UnitSize, Long> emptyCount = Arrays.stream(UnitSize.values())
                    .collect(Collectors.toMap(s -> s, s -> 0L));
            Map<UnitSize, BigDecimal> emptyRevenue = Arrays.stream(UnitSize.values())
                    .collect(Collectors.toMap(s -> s, s -> BigDecimal.ZERO));
            return ContractStatItem.builder()
                    .label(label)
                    .contractCount(0L)
                    .contractCountByUnitSize(emptyCount)
                    .totalRevenue(BigDecimal.ZERO)
                    .totalRevenueByUnitSize(emptyRevenue)
                    .build();
        }

        Map<UnitSize, Long> countBySize = rows.stream()
                .collect(Collectors.toMap(ContractStatRaw::unitSize, ContractStatRaw::contractCount));
        Map<UnitSize, BigDecimal> revenueBySize = rows.stream()
                .collect(Collectors.toMap(ContractStatRaw::unitSize, ContractStatRaw::totalRevenue));

        for (UnitSize s : UnitSize.values()) {
            countBySize.putIfAbsent(s, 0L);
            revenueBySize.putIfAbsent(s, BigDecimal.ZERO);
        }

        long totalCount = rows.stream().mapToLong(ContractStatRaw::contractCount).sum();
        BigDecimal totalRevenue = rows.stream().map(ContractStatRaw::totalRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ContractStatItem.builder()
                .label(label)
                .contractCount(totalCount)
                .contractCountByUnitSize(countBySize)
                .totalRevenue(totalRevenue)
                .totalRevenueByUnitSize(revenueBySize)
                .build();
    }

    private ContractStatsResponse toResponse(int year, List<ContractStatItem> items) {
        long totalCount = items.stream().mapToLong(ContractStatItem::getContractCount).sum();
        BigDecimal totalRevenue = items.stream()
                .map(ContractStatItem::getTotalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ContractStatsResponse.builder()
                .year(year)
                .totalContractCount(totalCount)
                .totalRevenue(totalRevenue)
                .items(items)
                .build();
    }
}
