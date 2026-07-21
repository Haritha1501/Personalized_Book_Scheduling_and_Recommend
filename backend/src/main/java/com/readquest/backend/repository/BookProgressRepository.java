package com.readquest.backend.repository;

import com.readquest.backend.entity.BookProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookProgressRepository extends JpaRepository<BookProgress, Long> {
    List<BookProgress> findByReadingPlanIdOrderByUpdatedAtDesc(Long readingPlanId);
}
