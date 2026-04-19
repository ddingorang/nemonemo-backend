// Created: 2026-04-19 20:40:09
package com.nemonemo.domain.dashboard.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ContractStatsResponse {

    private int year;
    private long totalContractCount;
    private BigDecimal totalRevenue;
    private List<ContractStatItem> items;
}
