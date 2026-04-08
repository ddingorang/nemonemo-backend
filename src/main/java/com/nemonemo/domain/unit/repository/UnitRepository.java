// Created: 2026-04-07 22:42:28
package com.nemonemo.domain.unit.repository;

import com.nemonemo.domain.unit.entity.Unit;
import com.nemonemo.domain.unit.entity.UnitSize;
import com.nemonemo.domain.unit.entity.UnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findAllByIsActiveTrue();

    List<Unit> findAllByIsActiveTrueAndSize(UnitSize size);

    List<Unit> findAllByIsActiveTrueAndStatus(UnitStatus status);

    List<Unit> findAllByIsActiveTrueAndSizeAndStatus(UnitSize size, UnitStatus status);

    Optional<Unit> findByIdAndIsActiveTrue(Long id);

    long countByIsActiveTrue();

    long countByIsActiveTrueAndStatus(UnitStatus status);
}
