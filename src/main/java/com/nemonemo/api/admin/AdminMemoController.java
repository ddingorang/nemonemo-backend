package com.nemonemo.api.admin;

import com.nemonemo.common.response.ApiResponse;
import com.nemonemo.domain.memo.dto.MemoRequest;
import com.nemonemo.domain.memo.dto.MemoResponse;
import com.nemonemo.domain.memo.service.AdminMemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "[관리자] 메모", description = "관리자 메모 API")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/admin/memos")
@RequiredArgsConstructor
public class AdminMemoController {

    private final AdminMemoService adminMemoService;

    @Operation(summary = "메모 목록 조회")
    @GetMapping
    public ApiResponse<List<MemoResponse>> getMemos() {
        return ApiResponse.ok(adminMemoService.getMemos());
    }

    @Operation(summary = "메모 생성")
    @PostMapping
    public ApiResponse<MemoResponse> createMemo(@RequestBody MemoRequest request) {
        return ApiResponse.ok(adminMemoService.createMemo(request));
    }

    @Operation(summary = "메모 수정")
    @PutMapping("/{id}")
    public ApiResponse<MemoResponse> updateMemo(@PathVariable Long id, @RequestBody MemoRequest request) {
        return ApiResponse.ok(adminMemoService.updateMemo(id, request));
    }

    @Operation(summary = "메모 삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMemo(@PathVariable Long id) {
        adminMemoService.deleteMemo(id);
        return ApiResponse.ok();
    }
}
