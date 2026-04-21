// Created: 2026-04-07 22:42:05
package com.nemonemo.domain.unit.entity;

public enum UnitStatus {
    AVAILABLE,    // 이용 가능
    OCCUPIED,     // 사용 중 (계약 완료)
    RESERVED,     // 문의 중 (선택적 운영)
    DISABLED      // 비활성화
}
