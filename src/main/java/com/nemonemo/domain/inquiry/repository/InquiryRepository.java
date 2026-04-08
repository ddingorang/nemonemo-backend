// Created: 2026-04-07 22:42:29
package com.nemonemo.domain.inquiry.repository;

import com.nemonemo.domain.inquiry.entity.Inquiry;
import com.nemonemo.domain.inquiry.entity.InquiryStatus;
import com.nemonemo.domain.unit.entity.UnitSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    boolean existsByCustomerPhoneAndStatusIn(String customerPhone, List<InquiryStatus> statuses);

    @Query("SELECT i FROM Inquiry i WHERE (:status IS NULL OR i.status = :status) AND (:size IS NULL OR i.desiredSize = :size) ORDER BY i.createdAt DESC")
    List<Inquiry> findAllByFilter(@Param("status") InquiryStatus status, @Param("size") UnitSize size);

    long countByStatus(InquiryStatus status);
}
