// Created: 2026-04-07 22:41:43
package com.nemonemo.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Unit
    UNIT_NOT_FOUND("유닛을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNIT_NOT_AVAILABLE("현재 이용 가능한 유닛이 아닙니다.", HttpStatus.CONFLICT),

    // Inquiry
    INQUIRY_NOT_FOUND("문의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INQUIRY_DUPLICATE("이미 처리 중인 문의가 있습니다. 접수된 문의 처리 후 다시 신청해 주세요.", HttpStatus.CONFLICT),

    // Warehouse
    WAREHOUSE_NOT_FOUND("창고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Contract
    CONTRACT_NOT_FOUND("계약을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONTRACT_ALREADY_ACTIVE("해당 유닛에 이미 활성 계약이 존재합니다.", HttpStatus.CONFLICT),
    CONTRACT_NOT_ACTIVE("활성 상태의 계약이 아닙니다.", HttpStatus.CONFLICT),

    // Memo
    MEMO_NOT_FOUND("메모를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Admin / Auth
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    ADMIN_NOT_FOUND("관리자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // Common
    INVALID_INPUT("입력값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
