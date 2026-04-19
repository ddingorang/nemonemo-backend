// Created: 2026-04-07 22:43:09
package com.nemonemo.api.open;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.unit.dto.UnitResponse;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.unit.service.UnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "유닛 조회", description = "창고 유닛 현황 조회 API")
@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    // 사이즈/상태 필터로 유닛 목록 조회 (만료 임박 표시 포함)
    @Operation(summary = "유닛 목록 조회", description = "사이즈, 상태로 필터링하여 유닛 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<UnitResponse>> getUnits(
            @RequestParam(required = false) UnitSize size,
            @RequestParam(required = false) UnitStatus status
    ) {
        return ApiResponse.ok(unitService.getUnits(size, status));
    }

    // 특정 유닛 상세 및 만료 임박 여부 조회
    @Operation(summary = "유닛 상세 조회", description = "특정 유닛의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<UnitResponse> getUnit(@PathVariable Long id) {
        return ApiResponse.ok(unitService.getUnit(id));
    }
}
