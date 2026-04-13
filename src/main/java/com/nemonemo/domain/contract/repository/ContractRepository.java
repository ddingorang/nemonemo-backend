// Created: 2026-04-08 22:45:48
package com.nemonemo.domain.contract.repository;

import com.nemonemo.domain.contract.entity.Contract;
import com.nemonemo.domain.contract.entity.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    boolean existsByUnitIdAndStatus(Long unitId, ContractStatus status);

    @Query("SELECT c FROM Contract c WHERE (:status IS NULL OR c.status = :status) AND (:unitId IS NULL OR c.unit.id = :unitId) ORDER BY c.createdAt DESC")
    List<Contract> findAllByFilter(@Param("status") ContractStatus status, @Param("unitId") Long unitId);

    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate < :today")
    List<Contract> findAllExpired(@Param("today") LocalDate today);

    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :today AND :threshold")
    List<Contract> findAllExpiringSoon(@Param("today") LocalDate today, @Param("threshold") LocalDate threshold);

    @Query("SELECT c.unit.id FROM Contract c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :today AND :in7days")
    List<Long> findUnitIdsExpiringSoon(@Param("today") LocalDate today, @Param("in7days") LocalDate in7days);

    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE'")
    List<Contract> findAllActive();

    void deleteAllByUnitId(Long unitId);
}
