package com.readquest.backend.service.impl;

import com.readquest.backend.entity.*;
import com.readquest.backend.external.openlibrary.OpenLibraryService;
import com.readquest.backend.repository.*;
import com.readquest.backend.service.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GamificationServiceImpl implements GamificationService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReaderStatisticsRepository readerStatisticsRepository;
    private final RecommendationRepository recommendationRepository;
    private final OpenLibraryService openLibraryService;

    @Override
    @Transactional
    public void processSessionRewards(User user, ReadingPlan plan, int pagesRead, double durationMinutes, boolean isCompleted) {
        // 1. Award Base XP (2 XP per page)
        int xpEarned = pagesRead * 2;
        
        // 2. Check Daily Goal Milestone
        // Compute what was read today prior to this session
        int dailyPagesReadPrior = user.getCurrentStreak() > 0 ? plan.getCurrentProgressPages() - pagesRead : 0;
        if (dailyPagesReadPrior < plan.getDailyPagesGoal() && plan.getCurrentProgressPages() >= plan.getDailyPagesGoal()) {
            xpEarned += 50; // +50 XP for daily goal
            createNotification(user, "Daily Goal Achieved!", "You reached your reading goal for today! +50 XP", "PROGRESS");
        }

        // 3. Check Book Completion reward (+500 XP)
        if (isCompleted) {
            xpEarned += 500;
            createNotification(user, "Book Completed!", "Congratulations on finishing " + plan.getBook().getTitle() + "! +500 XP", "PROGRESS");
        }

        // Apply XP
        int oldXp = user.getXp();
        int newXp = oldXp + xpEarned;
        user.setXp(newXp);

        // 4. Calculate Level Up
        int oldLevel = user.getLevel();
        int newLevel = calculateLevel(newXp);
        if (newLevel > oldLevel) {
            user.setLevel(newLevel);
            String levelName = getLevelName(newLevel);
            createNotification(user, "Leveled Up!", "Congratulations! You reached Level " + newLevel + " (" + levelName + ")!", "LEVEL_UP");
        }

        userRepository.save(user);
        log.info("Processed session rewards for user {}. XP Gained: {}. New Level: {}", user.getUsername(), xpEarned, user.getLevel());
    }

    @Override
    @Transactional
    public void checkAndUnlockAchievements(User user) {
        ReaderStatistics stats = readerStatisticsRepository.findByUserId(user.getId())
                .orElse(null);
        if (stats == null) return;

        // Check page thresholds
        checkAndUnlock(user, "PAGES_100", "Centurion Reader", "Read a total of 100 pages across all sessions.", 100, stats.getTotalPagesRead() >= 100);
        checkAndUnlock(user, "PAGES_1000", "Millennium Reader", "Read a total of 1,000 pages.", 300, stats.getTotalPagesRead() >= 1000);
        checkAndUnlock(user, "PAGES_5000", "Sage of Pages", "Read a total of 5,000 pages.", 500, stats.getTotalPagesRead() >= 5000);

        // Check book thresholds
        checkAndUnlock(user, "FIRST_BOOK", "First Book Completed", "You completed your first reading plan! Keep it up.", 500, stats.getTotalBooksCompleted() >= 1);
        checkAndUnlock(user, "BOOKS_10", "Library Apprentice", "Complete 10 book reading plans.", 1000, stats.getTotalBooksCompleted() >= 10);
        checkAndUnlock(user, "BOOKS_25", "Library Master", "Complete 25 book reading plans.", 1500, stats.getTotalBooksCompleted() >= 25);
        checkAndUnlock(user, "BOOKS_50", "Legendary Archivist", "Complete 50 book reading plans.", 2000, stats.getTotalBooksCompleted() >= 50);
        checkAndUnlock(user, "BOOKS_100", "Omniscient Reader", "Complete 100 book reading plans.", 5000, stats.getTotalBooksCompleted() >= 100);

        // Check streaks
        checkAndUnlock(user, "STREAK_7", "Week-long Scholar", "Maintain a reading streak for 7 days.", 100, user.getCurrentStreak() >= 7);
        checkAndUnlock(user, "STREAK_30", "Habit Titan", "Maintain a reading streak for 30 days.", 500, user.getCurrentStreak() >= 30);
        checkAndUnlock(user, "STREAK_365", "Year of Wisdom", "Maintain a reading streak for 365 days.", 2000, user.getCurrentStreak() >= 365);
    }

    @Override
    @Transactional
    public void checkProgressMilestones(User user, ReadingPlan plan) {
        double pct = ((double) plan.getCurrentProgressPages() / plan.getBook().getTotalPages()) * 100.0;

        // 25% milestone: Beginner Badge (+100 XP)
        if (pct >= 25.0) {
            checkAndUnlock(user, "BADGE_BEGINNER", "Beginner Badge", "Completed 25% of a book plan.", 100, true);
        }

        // 50% milestone: Explorer Badge (+200 XP)
        if (pct >= 50.0) {
            checkAndUnlock(user, "BADGE_EXPLORER", "Explorer Badge", "Completed 50% of a book plan.", 200, true);
        }

        // 75% milestone: Master Reader Badge (+300 XP) + Recommend 5 books from Open Library
        if (pct >= 75.0) {
            boolean unlockedJustNow = checkAndUnlock(user, "BADGE_MASTER", "Master Reader Badge", "Completed 75% of a book plan.", 300, true);
            if (unlockedJustNow) {
                generateRecommendations(user, plan, "OPEN_LIBRARY_75", 5);
            }
        }

        // 100% milestone: Champion Badge (+500 XP) + Certificate + Recommend 3 books
        if (pct >= 100.0) {
            boolean unlockedJustNow = checkAndUnlock(user, "BADGE_CHAMPION", "Champion Badge", "Completed 100% of a book plan.", 500, true);
            if (unlockedJustNow) {
                generateRecommendations(user, plan, "OPEN_LIBRARY_100", 3);
                // Create notification carrying the completion certificate log
                createNotification(user, "Certificate Unlocked!", 
                        "Reading Certificate issued to " + user.getUsername() + " for completing " + plan.getBook().getTitle() + "!", 
                        "PROGRESS");
            }
        }
    }

    private boolean checkAndUnlock(User user, String code, String title, String desc, int xpReward, boolean condition) {
        if (!condition) return false;

        boolean alreadyUnlocked = userAchievementRepository.existsByUserIdAndAchievementCode(user.getId(), code);
        if (!alreadyUnlocked) {
            Achievement achievement = getOrCreateAchievement(code, title, desc, xpReward);
            
            UserAchievement ua = UserAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .unlockedAt(LocalDateTime.now())
                    .build();
            userAchievementRepository.save(ua);

            // Award achievement XP
            user.setXp(user.getXp() + xpReward);
            user.setLevel(calculateLevel(user.getXp()));
            userRepository.save(user);

            createNotification(user, "Achievement Unlocked!", "You unlocked: " + title + "! +" + xpReward + " XP", "ACHIEVEMENT");
            log.info("User {} unlocked achievement: {}", user.getUsername(), title);
            return true;
        }
        return false;
    }

    private Achievement getOrCreateAchievement(String code, String title, String desc, int xpReward) {
        return achievementRepository.findByCode(code)
                .orElseGet(() -> achievementRepository.save(
                        Achievement.builder()
                                .code(code)
                                .title(title)
                                .description(desc)
                                .xpReward(xpReward)
                                .iconUrl(code.toLowerCase() + ".png")
                                .build()
                ));
    }

    private void generateRecommendations(User user, ReadingPlan plan, String type, int limit) {
        try {
            // Delete older recommendation logs to keep suggestions fresh
            recommendationRepository.deleteByUserId(user.getId());

            List<Book> recommendedBooks = openLibraryService.getRecommendations(
                    plan.getBook().getCategories(), plan.getBook().getAuthor());
            
            int count = 0;
            for (Book b : recommendedBooks) {
                if (count >= limit) break;
                // Check if book in DB, otherwise save it
                Book dbBook = recommendationRepository.save(Recommendation.builder()
                        .user(user)
                        .book(b)
                        .recommendedBy(type)
                        .build()).getBook();
                count++;
            }
            log.info("Generated {} recommendations for user {}", count, user.getUsername());
        } catch (Exception e) {
            log.error("Failed to generate recommendations: {}", e.getMessage());
        }
    }

    private void createNotification(User user, String title, String message, String type) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    private int calculateLevel(int xp) {
        if (xp >= 7500) return 5;
        if (xp >= 3500) return 4;
        if (xp >= 1500) return 3;
        if (xp >= 500) return 2;
        return 1;
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
