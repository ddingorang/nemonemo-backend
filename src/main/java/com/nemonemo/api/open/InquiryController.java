// Created: 2026-04-07 22:43:13
package com.nemonemo.api.open;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.inquiry.dto.InquiryRequest;
import com.nemonemo.domain.inquiry.dto.InquiryResponse;
import com.nemonemo.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "예약 문의", description = "예약 문의 제출 및 조회 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 특정 유닛 또는 희망 사이즈로 예약 문의 제출
    @Operation(summary = "예약 문의 제출", description = "특정 유닛 또는 희망 사이즈로 예약 문의를 제출합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InquiryResponse> submitInquiry(@Valid @RequestBody InquiryRequest request) {
        return ApiResponse.ok(inquiryService.submitInquiry(request));
    }

    // 문의 ID와 연락처 일치 여부 확인 후 문의 상태 조회
    @Operation(summary = "문의 상태 조회", description = "문의 ID와 연락처로 본인의 문의 상태를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<InquiryResponse> getInquiry(
            @PathVariable Long id,
            @RequestParam String customerPhone
    ) {
        return ApiResponse.ok(inquiryService.getInquiry(id, customerPhone));
    }
}
