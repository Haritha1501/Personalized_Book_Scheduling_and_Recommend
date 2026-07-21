package com.readquest.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reader_statistics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReaderStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_books_completed", nullable = false)
    @Builder.Default
    private Integer totalBooksCompleted = 0;

    @Column(name = "total_books_in_progress", nullable = false)
    @Builder.Default
    private Integer totalBooksInProgress = 0;

    @Column(name = "total_pages_read", nullable = false)
    @Builder.Default
    private Integer totalPagesRead = 0;

    @Column(name = "total_hours_read", nullable = false)
    @Builder.Default
    private Double totalHoursRead = 0.0;

    @Column(name = "avg_reading_speed_wpm", nullable = false)
    @Builder.Default
    private Double avgReadingSpeedWpm = 0.0;

    @Column(name = "favorite_genre", length = 100)
    private String favoriteGenre;

    @Column(name = "favorite_author", length = 100)
    private String favoriteAuthor;

    @Column(name = "completion_rate", nullable = false)
    @Builder.Default
    private Double completionRate = 0.0;

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
}
