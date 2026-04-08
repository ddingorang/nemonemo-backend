// Created: 2026-04-07 23:04:17
package com.nemonemo.api.admin;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.inquiry.dto.InquiryMemoUpdateRequest;
import com.nemonemo.domain.inquiry.dto.InquiryResponse;
import com.nemonemo.domain.inquiry.dto.InquiryStatusUpdateRequest;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.inquiry.service.AdminInquiryService;
import com.nemonemo.domain.unit.entity.UnitSize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "[관리자] 문의 관리", description = "관리자 예약 문의 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    @Operation(summary = "문의 목록 조회")
    @GetMapping
    public ApiResponse<List<InquiryResponse>> getInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) UnitSize size
    ) {
        return ApiResponse.ok(adminInquiryService.getInquiries(status, size));
    }

    @Operation(summary = "문의 상세 조회")
    @GetMapping("/{id}")
    public ApiResponse<InquiryResponse> getInquiry(@PathVariable Long id) {
        return ApiResponse.ok(adminInquiryService.getInquiry(id));
    }

    @Operation(summary = "문의 상태 변경")
    @PatchMapping("/{id}/status")
    public ApiResponse<InquiryResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody InquiryStatusUpdateRequest request
    ) {
        return ApiResponse.ok(adminInquiryService.updateStatus(id, request));
    }

    @Operation(summary = "관리자 메모 수정")
    @PatchMapping("/{id}/memo")
    public ApiResponse<InquiryResponse> updateMemo(
            @PathVariable Long id,
            @Valid @RequestBody InquiryMemoUpdateRequest request
    ) {
        return ApiResponse.ok(adminInquiryService.updateMemo(id, request));
    }
}
