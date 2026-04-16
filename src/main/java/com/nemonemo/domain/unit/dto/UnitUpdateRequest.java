// Created: 2026-04-07 23:03:05
package com.nemonemo.domain.unit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class UnitUpdateRequest {

    @NotBlank(message = "유닛 호수를 입력해 주세요.")
    private String unitNumber;

    private String zone;

    @NotNull(message = "월 임대료를 입력해 주세요.")
    @DecimalMin(value = "0", message = "월 임대료는 0원 이상이어야 합니다.")
    private BigDecimal monthlyPrice;
}
