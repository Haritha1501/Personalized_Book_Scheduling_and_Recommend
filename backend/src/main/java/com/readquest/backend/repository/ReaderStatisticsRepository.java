package com.readquest.backend.repository;

import com.readquest.backend.entity.ReaderStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReaderStatisticsRepository extends JpaRepository<ReaderStatistics, Long> {
    Optional<ReaderStatistics> findByUserId(Long userId);
}
