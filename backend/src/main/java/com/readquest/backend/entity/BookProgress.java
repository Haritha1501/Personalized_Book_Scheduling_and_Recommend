package com.readquest.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "book_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_plan_id", nullable = false)
    private ReadingPlan readingPlan;

    @Column(name = "pages_read", nullable = false)
    private Integer pagesRead;

    @Column(name = "duration_minutes", nullable = false)
    private Double durationMinutes;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
