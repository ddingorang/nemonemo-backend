// Created: 2026-04-08 22:46:31
package com.nemonemo.api.admin;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.contract.dto.ContractCreateRequest;
import com.nemonemo.domain.contract.dto.ContractResponse;
import com.nemonemo.domain.contract.dto.ContractUpdateRequest;
import com.nemonemo.domain.contract.entity.ContractStatus;
import com.nemonemo.domain.contract.service.AdminContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "[관리자] 계약 관리", description = "관리자 계약 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/contracts")
@RequiredArgsConstructor
public class AdminContractController {

    private final AdminContractService adminContractService;

    // 상태/유닛 ID 필터로 계약 목록 페이지네이션 조회
    @Operation(summary = "계약 목록 조회")
    @GetMapping
    public ApiResponse<Page<ContractResponse>> getContracts(
            @RequestParam(required = false) ContractStatus status,
            @RequestParam(required = false) Long unitId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.ok(adminContractService.getContracts(status, unitId, pageable));
    }

    // 특정 계약 상세 조회
    @Operation(summary = "계약 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<ContractResponse> getContract(@PathVariable Long id) {
        return ApiResponse.ok(adminContractService.getContract(id));
    }

    // 새 계약 등록
    @Operation(summary = "계약 등록")
    @PostMapping
    public ApiResponse<ContractResponse> createContract(@Valid @RequestBody ContractCreateRequest request) {
        return ApiResponse.ok(adminContractService.createContract(request));
    }

    // 계약 정보 수정
    @Operation(summary = "계약 수정")
    @PutMapping("/{id}")
    public ApiResponse<ContractResponse> updateContract(
            @PathVariable Long id,
            @Valid @RequestBody ContractUpdateRequest request
    ) {
        return ApiResponse.ok(adminContractService.updateContract(id, request));
    }

    // 계약 해지 처리
    @Operation(summary = "계약 해지")
    @PatchMapping("/{id}/terminate")
    public ApiResponse<ContractResponse> terminateContract(@PathVariable Long id) {
        return ApiResponse.ok(adminContractService.terminateContract(id));
    }
}
