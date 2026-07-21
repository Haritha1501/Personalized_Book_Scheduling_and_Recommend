package com.readquest.backend.controller;

import com.readquest.backend.dto.DashboardResponse;
import com.readquest.backend.entity.*;
import com.readquest.backend.repository.*;
import com.readquest.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    private final UserRepository userRepository;
    private final ReadingPlanRepository readingPlanRepository;
    private final DailyStreakRepository dailyStreakRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final NotificationRepository notificationRepository;
    private final ReadingSessionRepository readingSessionRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getId();

        User user = userRepository.findById(userId).orElseThrow();

        // 1. Level metrics
        int nextThreshold = getNextLevelThreshold(user.getXp());
        String levelName = getLevelName(user.getLevel());

        // 2. Active plans mapping
        List<ReadingPlan> activePlans = readingPlanRepository.findByUserIdAndStatus(userId, "ACTIVE");
        List<DashboardResponse.PlanDetail> planDetails = activePlans.stream().map(p -> {
            double completion = ((double) p.getCurrentProgressPages() / p.getBook().getTotalPages()) * 100.0;
            return DashboardResponse.PlanDetail.builder()
                    .id(p.getId())
                    .bookTitle(p.getBook().getTitle())
                    .bookAuthor(p.getBook().getAuthor())
                    .coverUrl(p.getBook().getCoverUrl())
                    .totalPages(p.getBook().getTotalPages())
                    .currentProgressPages(p.getCurrentProgressPages())
                    .completionPercentage(Math.min(completion, 100.0))
                    .dailyPagesGoal(p.getDailyPagesGoal())
                    .dailyMinutesGoal(p.getDailyMinutesGoal())
                    .targetDays(p.getTargetDays())
                    .targetDate(p.getTargetDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build();
        }).collect(Collectors.toList());

        // 3. Heatmap (Daily Streaks for the last 30 days)
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        List<DailyStreak> streaks = dailyStreakRepository.findByUserIdAndActivityDateBetween(userId, start, end);
        
        Map<LocalDate, Integer> streakMap = new HashMap<>();
        streaks.forEach(s -> streakMap.put(s.getActivityDate(), s.getPagesRead()));

        List<DashboardResponse.StreakDay> heatmap = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            heatmap.add(DashboardResponse.StreakDay.builder()
                    .date(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .pagesRead(streakMap.getOrDefault(date, 0))
                    .build());
        }

        // 4. Recent Achievements
        List<UserAchievement> userAchievements = userAchievementRepository.findByUserId(userId);
        List<DashboardResponse.AchievementDetail> recentAchievements = userAchievements.stream()
                .sorted((a, b) -> b.getUnlockedAt().compareTo(a.getUnlockedAt()))
                .limit(4)
                .map(ua -> DashboardResponse.AchievementDetail.builder()
                        .code(ua.getAchievement().getCode())
                        .title(ua.getAchievement().getTitle())
                        .description(ua.getAchievement().getDescription())
                        .iconUrl(ua.getAchievement().getIconUrl())
                        .unlockedAt(ua.getUnlockedAt().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build())
                .collect(Collectors.toList());

        // 5. Notifications (last 5 unread)
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        List<DashboardResponse.NotificationDetail> notificationDetails = unreadNotifications.stream()
                .limit(5)
                .map(n -> DashboardResponse.NotificationDetail.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .type(n.getType())
                        .createdAt(n.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .build())
                .collect(Collectors.toList());

        // 6. Today's Reading Progress
        int todayPages = 0;
        double todayMinutes = 0.0;
        List<ReadingSession> todaySessions = readingSessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(s -> s.getStartTime().toLocalDate().isEqual(LocalDate.now()))
                .collect(Collectors.toList());

        for (ReadingSession s : todaySessions) {
            todayPages += s.getPagesRead();
            todayMinutes += s.getDurationMinutes();
        }

        // Sum goals from active plans
        int dailyPagesGoal = planDetails.stream().mapToInt(DashboardResponse.PlanDetail::getDailyPagesGoal).sum();
        double dailyMinutesGoal = planDetails.stream().mapToDouble(DashboardResponse.PlanDetail::getDailyMinutesGoal).sum();
        if (dailyPagesGoal == 0) {
            dailyPagesGoal = 10; // Default fallback daily goal pages
            dailyMinutesGoal = 10.0; // Default fallback daily goal minutes
        }

        DashboardResponse response = DashboardResponse.builder()
                .level(user.getLevel())
                .xp(user.getXp())
                .levelName(levelName)
                .nextLevelXpThreshold(nextThreshold)
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .dailyPagesGoal(dailyPagesGoal)
                .dailyMinutesGoal(dailyMinutesGoal)
                .todayPagesRead(todayPages)
                .todayMinutesRead(todayMinutes)
                .activePlans(planDetails)
                .heatmap(heatmap)
                .recentAchievements(recentAchievements)
                .notifications(notificationDetails)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/notifications/{id}/read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
        return ResponseEntity.ok().build();
    }

    private int getNextLevelThreshold(int xp) {
        if (xp < 500) return 500;
        if (xp < 1500) return 1500;
        if (xp < 3500) return 3500;
        if (xp < 7500) return 7500;
        return 99999;
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
}
