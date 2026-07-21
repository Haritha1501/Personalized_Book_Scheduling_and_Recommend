package com.readquest.backend.repository;

import com.readquest.backend.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    List<ReadingSession> findByReadingPlanIdOrderByCreatedAtDesc(Long readingPlanId);
    List<ReadingSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
