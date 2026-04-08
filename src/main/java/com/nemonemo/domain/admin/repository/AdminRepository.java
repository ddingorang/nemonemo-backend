// Created: 2026-04-07 23:02:21
package com.nemonemo.domain.admin.repository;

import com.nemonemo.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUsername(String username);
}
