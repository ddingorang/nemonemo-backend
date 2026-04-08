// Created: 2026-04-07 22:42:39
package com.nemonemo.domain.unit.dto;

import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UnitResponse {

    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private String unitNumber;
    private UnitSize size;
    private BigDecimal areaSqm;
    private Integer floor;
    private String zone;
    private BigDecimal monthlyPrice;
    private UnitStatus status;
    private boolean expiringSoon;

    // 활성 계약 정보 (없으면 null)
    private Long contractId;
    private String contractCustomerName;
    private String contractCustomerPhone;
    private LocalDateTime contractCreatedAt;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;

    public static UnitResponse from(Unit unit) {
        return from(unit, false, null);
    }

    public static UnitResponse from(Unit unit, boolean expiringSoon) {
        return from(unit, expiringSoon, null);
    }

    public static UnitResponse from(Unit unit, boolean expiringSoon, Contract contract) {
        UnitResponseBuilder b = UnitResponse.builder()
                .id(unit.getId())
                .warehouseId(unit.getWarehouse().getId())
                .warehouseName(unit.getWarehouse().getName())
                .unitNumber(unit.getUnitNumber())
                .size(unit.getSize())
                .areaSqm(unit.getAreaSqm())
                .floor(unit.getFloor())
                .zone(unit.getZone())
                .monthlyPrice(unit.getMonthlyPrice())
                .status(unit.getStatus())
                .expiringSoon(expiringSoon);

        if (contract != null) {
            b.contractId(contract.getId())
             .contractCustomerName(contract.getCustomerName())
             .contractCustomerPhone(contract.getCustomerPhone())
             .contractCreatedAt(contract.getCreatedAt())
             .contractStartDate(contract.getStartDate())
             .contractEndDate(contract.getEndDate());
        }

        return b.build();
    }
}
