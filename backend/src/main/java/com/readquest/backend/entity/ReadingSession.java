package com.readquest.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_plan_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private ReadingPlan readingPlan;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "start_page", nullable = false)
    private Integer startPage;

    @Column(name = "end_page", nullable = false)
    private Integer endPage;

    @Column(name = "pages_read", nullable = false)
    private Integer pagesRead;

    @Column(name = "duration_minutes", nullable = false)
    private Double durationMinutes;

    @Column(name = "reading_speed_wpm", nullable = false)
    private Double readingSpeedWpm;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
