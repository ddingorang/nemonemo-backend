// Created: 2026-04-07 23:04:12
package com.nemonemo.api.admin;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.unit.dto.UnitCreateRequest;
import com.nemonemo.domain.unit.dto.UnitResponse;
import com.nemonemo.domain.unit.dto.UnitStatusUpdateRequest;
import com.nemonemo.domain.unit.dto.UnitUpdateRequest;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import com.nemonemo.domain.contract.service.AdminContractService;
import com.nemonemo.domain.unit.service.AdminUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "[관리자] 유닛 관리", description = "관리자 유닛 CRUD API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/units")
@RequiredArgsConstructor
public class AdminUnitController {

    private final AdminUnitService adminUnitService;
    private final AdminContractService adminContractService;

    // 사이즈/상태 필터로 유닛 목록 조회
    @Operation(summary = "유닛 목록 조회")
    @GetMapping
    public ApiResponse<List<UnitResponse>> getUnits(
            @RequestParam(required = false) UnitSize size,
            @RequestParam(required = false) UnitStatus status
    ) {
        return ApiResponse.ok(adminUnitService.getUnits(size, status));
    }

    // 새 유닛 등록
    @Operation(summary = "유닛 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UnitResponse> createUnit(@Valid @RequestBody UnitCreateRequest request) {
        return ApiResponse.ok(adminUnitService.createUnit(request));
    }

    // 유닛 정보 수정
    @Operation(summary = "유닛 수정")
    @PutMapping("/{id}")
    public ApiResponse<UnitResponse> updateUnit(
            @PathVariable Long id,
            @Valid @RequestBody UnitUpdateRequest request
    ) {
        return ApiResponse.ok(adminUnitService.updateUnit(id, request));
    }

    // 유닛 상태 변경
    @Operation(summary = "유닛 상태 변경")
    @PatchMapping("/{id}/status")
    public ApiResponse<UnitResponse> updateUnitStatus(
            @PathVariable Long id,
            @Valid @RequestBody UnitStatusUpdateRequest request
    ) {
        return ApiResponse.ok(adminUnitService.updateUnitStatus(id, request));
    }

    // 유닛 연관 계약 전체 삭제 및 상태 초기화
    @Operation(summary = "유닛 계약 초기화 (연관 계약 전체 삭제)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUnitContracts(@PathVariable Long id) {
        adminContractService.deleteContractsByUnit(id);
    }
}
