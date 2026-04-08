// Created: 2026-04-07 22:42:25
package com.nemonemo.domain.warehouse.repository;

import com.nemonemo.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
