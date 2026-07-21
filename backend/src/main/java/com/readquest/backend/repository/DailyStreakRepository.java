package com.readquest.backend.repository;

import com.readquest.backend.entity.DailyStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStreakRepository extends JpaRepository<DailyStreak, Long> {
    Optional<DailyStreak> findByUserIdAndActivityDate(Long userId, LocalDate activityDate);
    List<DailyStreak> findByUserIdAndActivityDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<DailyStreak> findByUserId(Long userId);
}
