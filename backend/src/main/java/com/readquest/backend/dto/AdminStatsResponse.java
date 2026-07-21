package com.readquest.backend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AdminStatsResponse {
    private Long totalUsers;
    private Long totalBooks;
    private Long totalReadingPlans;
    private Long totalSessions;
    private List<ActiveReaderDetail> activeReaders;
    private List<PopularBookDetail> popularBooks;
    private List<GenreTrend> trendingGenres;
    private List<ClassificationDistribution> readerClassifications;

    @Data
    @Builder
    public static class ActiveReaderDetail {
        private String username;
        private String email;
        private Integer xp;
        private Integer level;
        private Integer totalPagesRead;
        private Integer currentStreak;
    }

    @Data
    @Builder
    public static class PopularBookDetail {
        private String title;
        private String author;
        private Long activePlanCount;
    }

    @Data
    @Builder
    public static class GenreTrend {
        private String genre;
        private Long count;
    }

    @Data
    @Builder
    public static class ClassificationDistribution {
        private String name;
        private Long count;
    }
}
