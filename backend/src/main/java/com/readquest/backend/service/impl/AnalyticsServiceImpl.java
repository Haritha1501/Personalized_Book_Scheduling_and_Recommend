package com.readquest.backend.service.impl;

import com.readquest.backend.dto.ProfileResponse;
import com.readquest.backend.entity.*;
import com.readquest.backend.exception.ResourceNotFoundException;
import com.readquest.backend.repository.*;
import com.readquest.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ReaderStatisticsRepository readerStatisticsRepository;
    private final ReadingPlanRepository readingPlanRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ReaderTypeRepository readerTypeRepository;
    private final UserRepository userRepository;
    private final DailyStreakRepository dailyStreakRepository;
    private final AchievementRepository achievementRepository;

    @Override
    @Transactional
    public void recalculateUserStats(User user) {
        List<ReadingPlan> plans = readingPlanRepository.findByUserId(user.getId());
        List<ReadingSession> sessions = readingSessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        int completed = 0;
        int inProgress = 0;
        List<String> genres = new ArrayList<>();
        List<String> authors = new ArrayList<>();

        for (ReadingPlan plan : plans) {
            if ("COMPLETED".equals(plan.getStatus())) {
                completed++;
            } else if ("ACTIVE".equals(plan.getStatus())) {
                inProgress++;
            }

            if (plan.getBook().getCategories() != null && !plan.getBook().getCategories().trim().isEmpty()) {
                genres.add(plan.getBook().getCategories().split(",")[0].trim());
            }
            if (plan.getBook().getAuthor() != null && !plan.getBook().getAuthor().trim().isEmpty()) {
                authors.add(plan.getBook().getAuthor().trim());
            }
        }

        int totalPages = 0;
        double totalMinutes = 0.0;
        int weekendSessionCount = 0;
        int nightSessionCount = 0;

        for (ReadingSession session : sessions) {
            totalPages += session.getPagesRead();
            totalMinutes += session.getDurationMinutes();

            // Check if weekend session
            DayOfWeek day = session.getStartTime().getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekendSessionCount++;
            }

            // Check if night session (between 10 PM and 4 AM)
            int hour = session.getStartTime().getHour();
            if (hour >= 22 || hour < 4) {
                nightSessionCount++;
            }
        }

        double totalHours = totalMinutes / 60.0;
        double avgSpeed = totalMinutes > 0 ? ((double) totalPages * 250.0) / totalMinutes : 250.0;
        if (user.getReadingSpeedWpm() != null && totalPages == 0) {
            avgSpeed = user.getReadingSpeedWpm();
        }

        String favGenre = findMode(genres, "Fiction");
        String favAuthor = findMode(authors, "N/A");

        double completionRate = plans.isEmpty() ? 0.0 : ((double) completed / plans.size()) * 100.0;

        // Fetch or create stats entity
        ReaderStatistics stats = readerStatisticsRepository.findByUserId(user.getId())
                .orElseGet(() -> ReaderStatistics.builder().user(user).build());

        stats.setTotalBooksCompleted(completed);
        stats.setTotalBooksInProgress(inProgress);
        stats.setTotalPagesRead(totalPages);
        stats.setTotalHoursRead(totalHours);
        stats.setAvgReadingSpeedWpm(avgSpeed);
        stats.setFavoriteGenre(favGenre);
        stats.setFavoriteAuthor(favAuthor);
        stats.setCompletionRate(completionRate);
        stats.setLastUpdated(LocalDateTime.now());
        readerStatisticsRepository.save(stats);

        // 2. Classify user ReaderType
        String classificationCode = classifyReader(user, stats, sessions, genres.size(), weekendSessionCount, nightSessionCount);
        ReaderType readerType = readerTypeRepository.findByCode(classificationCode)
                .orElseGet(() -> readerTypeRepository.save(ReaderType.builder()
                        .code(classificationCode)
                        .name(classificationCode.replace("_", " "))
                        .description("Automated Reader Type classification based on statistics.")
                        .build()));

        user.setReaderType(readerType);
        userRepository.save(user);

        log.info("Recalculated stats and classified user {} as {}", user.getUsername(), classificationCode);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getUserProfileData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ReaderStatistics stats = readerStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> ReaderStatistics.builder()
                        .user(user)
                        .totalBooksCompleted(0)
                        .totalBooksInProgress(0)
                        .totalPagesRead(0)
                        .totalHoursRead(0.0)
                        .avgReadingSpeedWpm(user.getReadingSpeedWpm() != null ? user.getReadingSpeedWpm() : 250.0)
                        .favoriteGenre("Fiction")
                        .favoriteAuthor("N/A")
                        .completionRate(0.0)
                        .build());

        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        List<AchievementDetail> allAchievements = getOrCreateAllAchievements();

        List<ProfileResponse.UserAchievementDetail> achievementPayload = allAchievements.stream().map(a -> {
            Optional<UserAchievement> uaOpt = userAchievements.stream()
                    .filter(ua -> ua.getAchievement().getCode().equals(a.getCode()))
                    .findFirst();
            return ProfileResponse.UserAchievementDetail.builder()
                    .code(a.getCode())
                    .title(a.getTitle())
                    .description(a.getDescription())
                    .xpReward(a.getXpReward())
                    .iconUrl(a.getIconUrl())
                    .unlocked(uaOpt.isPresent())
                    .unlockedAt(uaOpt.map(ua -> ua.getUnlockedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).orElse(null))
                    .build();
        }).collect(Collectors.toList());

        List<ReadingSession> sessions = readingSessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<ProfileResponse.SessionLog> sessionLogs = sessions.stream().limit(10).map(s -> 
            ProfileResponse.SessionLog.builder()
                    .id(s.getId())
                    .bookTitle(s.getReadingPlan().getBook().getTitle())
                    .coverUrl(s.getReadingPlan().getBook().getCoverUrl())
                    .date(s.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .pagesRead(s.getPagesRead())
                    .durationMinutes(s.getDurationMinutes())
                    .speedWpm(s.getReadingSpeedWpm())
                    .build()
        ).collect(Collectors.toList());

        return ProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .xp(user.getXp())
                .level(user.getLevel())
                .levelName(getLevelName(user.getLevel()))
                .readingSpeedWpm(user.getReadingSpeedWpm())
                .readingAccuracy(user.getReadingAccuracy())
                .readingType(user.getReadingType())
                .readerClassification(user.getReaderType() != null ? user.getReaderType().getName() : "Casual Reader")
                .readerClassificationDescription(user.getReaderType() != null ? user.getReaderType().getDescription() : "Reads occasionally.")
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .statistics(ProfileResponse.StatsDetail.builder()
                        .totalBooksCompleted(stats.getTotalBooksCompleted())
                        .totalBooksInProgress(stats.getTotalBooksInProgress())
                        .totalPagesRead(stats.getTotalPagesRead())
                        .totalHoursRead(stats.getTotalHoursRead())
                        .avgReadingSpeedWpm(stats.getAvgReadingSpeedWpm())
                        .favoriteGenre(stats.getFavoriteGenre())
                        .favoriteAuthor(stats.getFavoriteAuthor())
                        .completionRate(stats.getCompletionRate())
                        .build())
                .achievements(achievementPayload)
                .recentSessions(sessionLogs)
                .build();
    }

    private String classifyReader(User user, ReaderStatistics stats, List<ReadingSession> sessions, int distinctBooks, int weekendCount, int nightCount) {
        if (sessions.isEmpty()) {
            return "CASUAL";
        }

        // Finisher Heuristic
        if (stats.getCompletionRate() >= 90.0 && stats.getTotalBooksCompleted() >= 3) {
            return "FINISHER";
        }

        // Speed Reader Heuristic
        if (stats.getAvgReadingSpeedWpm() > 350.0) {
            return "SPEED_READER";
        }

        // Scholar Heuristic
        if (stats.getTotalBooksCompleted() >= 10 || stats.getTotalPagesRead() >= 5000) {
            return "SCHOLAR";
        }

        // Book Collector Heuristic
        if (stats.getTotalBooksInProgress() >= 5) {
            return "BOOK_COLLECTOR";
        }

        // Consistent Reader Heuristic
        if (user.getCurrentStreak() >= 15) {
            return "CONSISTENT";
        }

        // Night Owl Heuristic
        if (nightCount > (sessions.size() / 2)) {
            return "NIGHT_OWL";
        }

        // Weekend Reader Heuristic
        if (weekendCount > (sessions.size() / 2)) {
            return "WEEKEND";
        }

        // Explorer Heuristic
        if (distinctBooks >= 4) {
            return "EXPLORER";
        }

        return "CASUAL";
    }

    private String findMode(List<String> items, String defaultValue) {
        if (items.isEmpty()) return defaultValue;
        return items.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(defaultValue);
    }

    private String getLevelName(int level) {
        return switch (level) {
            case 2 -> "Explorer";
            case 3 -> "Scholar";
            case 4 -> "Master Reader";
            case 5 -> "Legend";
            default -> "Bookworm";
        };
    }

    private List<AchievementDetail> getOrCreateAllAchievements() {
        // Build local cache helper for profile listing
        List<AchievementDetail> details = new ArrayList<>();
        achievementRepository.findAll().forEach(a -> details.add(
                new AchievementDetail(a.getCode(), a.getTitle(), a.getDescription(), a.getXpReward(), a.getIconUrl())
        ));
        return details;
    }

    @lombok.Value
    private static class AchievementDetail {
        String code;
        String title;
        String description;
        int xpReward;
        String iconUrl;
    }
}
