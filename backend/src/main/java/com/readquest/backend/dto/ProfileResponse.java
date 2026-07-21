package com.readquest.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private String username;
    private String email;
    private Integer xp;
    private Integer level;
    private String levelName;
    private Double readingSpeedWpm;
    private Double readingAccuracy;
    private String readingType;
    private String readerClassification;
    private String readerClassificationDescription;
    private Integer currentStreak;
    private Integer longestStreak;
    private StatsDetail statistics;
    private List<UserAchievementDetail> achievements;
    private List<SessionLog> recentSessions;

    @Data
    @Builder
    public static class StatsDetail {
        private Integer totalBooksCompleted;
        private Integer totalBooksInProgress;
        private Integer totalPagesRead;
        private Double totalHoursRead;
        private Double avgReadingSpeedWpm;
        private String favoriteGenre;
        private String favoriteAuthor;
        private Double completionRate;
    }

    @Data
    @Builder
    public static class UserAchievementDetail {
        private String code;
        private String title;
        private String description;
        private Integer xpReward;
        private String iconUrl;
        private boolean unlocked;
        private String unlockedAt;
    }

    @Data
    @Builder
    public static class SessionLog {
        private Long id;
        private String bookTitle;
        private String coverUrl;
        private String date;
        private Integer pagesRead;
        private Double durationMinutes;
        private Double speedWpm;
    }
}
