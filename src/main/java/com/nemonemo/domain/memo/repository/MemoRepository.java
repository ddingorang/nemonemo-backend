package com.nemonemo.domain.memo.repository;

import com.nemonemo.domain.memo.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoRepository extends JpaRepository<Memo, Long> {
    List<Memo> findAllByOrderByPinnedDescCreatedAtDesc();
}
