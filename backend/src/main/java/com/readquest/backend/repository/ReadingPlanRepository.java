package com.readquest.backend.repository;

import com.readquest.backend.entity.ReadingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingPlanRepository extends JpaRepository<ReadingPlan, Long> {
    List<ReadingPlan> findByUserIdAndStatus(Long userId, String status);
    List<ReadingPlan> findByUserId(Long userId);
    Optional<ReadingPlan> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}
