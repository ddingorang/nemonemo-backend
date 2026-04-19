// Created: 2026-04-19 20:59:49
package com.nemonemo.domain.contract.dto;

import com.nemonemo.domain.unit.entity.UnitSize;

import java.math.BigDecimal;

public record ContractStatRaw(int periodKey, UnitSize unitSize, long contractCount, BigDecimal totalRevenue) {
}
