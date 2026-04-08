// Created: 2026-04-07 23:03:07
package com.nemonemo.domain.unit.dto;

import com.nemonemo.domain.unit.entity.UnitStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UnitStatusUpdateRequest {

    @NotNull(message = "변경할 상태를 선택해 주세요.")
    private UnitStatus status;
}
