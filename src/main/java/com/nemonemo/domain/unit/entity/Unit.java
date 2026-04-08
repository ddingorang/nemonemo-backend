// Created: 2026-04-07 22:42:15
package com.nemonemo.domain.unit.entity;

import com.nemonemo.common.BaseTimeEntity;
import com.nemonemo.domain.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "unit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Unit extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, length = 20)
    private String unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UnitSize size;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal areaSqm;

    private Integer floor;

    @Column(length = 10)
    private String zone;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal monthlyPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UnitStatus status = UnitStatus.AVAILABLE;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    public void changeStatus(UnitStatus status) {
        this.status = status;
    }

    public void update(String unitNumber, BigDecimal areaSqm, Integer floor, String zone, BigDecimal monthlyPrice) {
        this.unitNumber = unitNumber;
        this.areaSqm = areaSqm;
        this.floor = floor;
        this.zone = zone;
        this.monthlyPrice = monthlyPrice;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
