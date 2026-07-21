package com.readquest.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Integer level;
    private Integer xp;
    private String levelName;
    private Integer nextLevelXpThreshold;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer dailyPagesGoal;
    private Double dailyMinutesGoal;
    private Integer todayPagesRead;
    private Double todayMinutesRead;
    private List<PlanDetail> activePlans;
    private List<StreakDay> heatmap;
    private List<AchievementDetail> recentAchievements;
    private List<NotificationDetail> notifications;

    @Data
    @Builder
    public static class PlanDetail {
        private Long id;
        private String bookTitle;
        private String bookAuthor;
        private String coverUrl;
        private Integer totalPages;
        private Integer currentProgressPages;
        private Double completionPercentage;
        private Integer dailyPagesGoal;
        private Double dailyMinutesGoal;
        private Integer targetDays;
        private String targetDate;
    }

    @Data
    @Builder
    public static class StreakDay {
        private String date; // YYYY-MM-DD
        private Integer pagesRead;
    }

    @Data
    @Builder
    public static class AchievementDetail {
        private String code;
        private String title;
        private String description;
        private String iconUrl;
        private String unlockedAt;
    }

    @Data
    @Builder
    public static class NotificationDetail {
        private Long id;
        private String title;
        private String message;
        private String type;
        private String createdAt;
    }
}
