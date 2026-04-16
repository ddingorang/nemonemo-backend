// Created: 2026-04-07 23:03:03
package com.nemonemo.domain.unit.dto;

import com.nemonemo.domain.unit.entity.UnitSize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UnitCreateRequest {

    @NotNull(message = "창고 ID를 입력해 주세요.")
    private Long warehouseId;

    @NotBlank(message = "유닛 호수를 입력해 주세요.")
    private String unitNumber;

    @NotNull(message = "사이즈를 선택해 주세요.")
    private UnitSize size;

    private String zone;

    @NotNull(message = "월 임대료를 입력해 주세요.")
    @DecimalMin(value = "0", message = "월 임대료는 0원 이상이어야 합니다.")
    private BigDecimal monthlyPrice;
}
