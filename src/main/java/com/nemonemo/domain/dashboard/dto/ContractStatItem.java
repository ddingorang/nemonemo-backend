// Created: 2026-04-19 20:59:50
package com.nemonemo.domain.dashboard.dto;

import com.nemonemo.domain.unit.entity.UnitSize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class ContractStatItem {

    private String label;
    private long contractCount;
    private Map<UnitSize, Long> contractCountByUnitSize;
    private BigDecimal totalRevenue;
    private Map<UnitSize, BigDecimal> totalRevenueByUnitSize;
}
