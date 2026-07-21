package com.readquest.backend.service.impl;

import com.readquest.backend.dto.ReadingSessionEndRequest;
import com.readquest.backend.dto.ReadingSessionStartRequest;
import com.readquest.backend.entity.*;
import com.readquest.backend.exception.BadRequestException;
import com.readquest.backend.exception.ResourceNotFoundException;
import com.readquest.backend.repository.BookProgressRepository;
import com.readquest.backend.repository.ReadingPlanRepository;
import com.readquest.backend.repository.ReadingSessionRepository;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.service.AnalyticsService;
import com.readquest.backend.service.GamificationService;
import com.readquest.backend.service.ReadingSessionService;
import com.readquest.backend.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingSessionServiceImpl implements ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final ReadingPlanRepository readingPlanRepository;
    private final UserRepository userRepository;
    private final BookProgressRepository bookProgressRepository;
    private final StreakService streakService;
    private final GamificationService gamificationService;
    
    // Use Lazy to prevent circular reference with AnalyticsService
    @Lazy
    private final AnalyticsService analyticsService;

    @Override
    @Transactional
    public ReadingSession startSession(Long userId, ReadingSessionStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ReadingPlan plan = readingPlanRepository.findById(request.getReadingPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Reading plan not found: " + request.getReadingPlanId()));

        if (!plan.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to reading plan.");
        }

        if (!"ACTIVE".equals(plan.getStatus())) {
            throw new BadRequestException("This reading plan is no longer active.");
        }

        // Lazy streak verify on login or session start
        streakService.checkAndResetStreak(user);

        ReadingSession session = ReadingSession.builder()
                .user(user)
                .readingPlan(plan)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now()) // Will be updated on end
                .startPage(request.getStartPage())
                .endPage(request.getStartPage())
                .pagesRead(0)
                .durationMinutes(0.0)
                .readingSpeedWpm(0.0)
                .build();

        ReadingSession savedSession = readingSessionRepository.save(session);
        log.info("Started reading session {} for plan {}", savedSession.getId(), plan.getId());
        return savedSession;
    }

    @Override
    @Transactional
    public ReadingSession endSession(Long userId, ReadingSessionEndRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ReadingSession session = readingSessionRepository.findById(request.getReadingSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Reading session not found: " + request.getReadingSessionId()));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to reading session.");
        }

        ReadingPlan plan = session.getReadingPlan();
        Book book = plan.getBook();

        if (request.getEndPage() < session.getStartPage()) {
            throw new BadRequestException("End page (" + request.getEndPage() + ") cannot be less than start page (" + session.getStartPage() + ").");
        }

        if (request.getEndPage() > book.getTotalPages()) {
            throw new BadRequestException("End page cannot exceed total pages of the book (" + book.getTotalPages() + ").");
        }

        LocalDateTime now = LocalDateTime.now();
        long millis = Duration.between(session.getStartTime(), now).toMillis();
        double durationMinutes = (double) millis / 60000.0;
        if (durationMinutes < 0.1) {
            durationMinutes = 0.1; // Capped minimal duration
        }

        int pagesRead = request.getEndPage() - session.getStartPage();
        // Assuming 250 words per page
        double speedWpm = ((double) pagesRead * 250.0) / durationMinutes;

        // 1. Update session
        session.setEndTime(now);
        session.setEndPage(request.getEndPage());
        session.setPagesRead(pagesRead);
        session.setDurationMinutes(durationMinutes);
        session.setReadingSpeedWpm(speedWpm);
        ReadingSession savedSession = readingSessionRepository.save(session);

        // 2. Update plan progress
        plan.setCurrentProgressPages(request.getEndPage());
        boolean isCompleted = plan.getCurrentProgressPages() >= book.getTotalPages();
        if (isCompleted) {
            plan.setStatus("COMPLETED");
            plan.setCurrentProgressPages(book.getTotalPages());
        }
        readingPlanRepository.save(plan);

        // 3. Log intermediate progress
        if (pagesRead > 0) {
            BookProgress progress = BookProgress.builder()
                    .readingPlan(plan)
                    .pagesRead(pagesRead)
                    .durationMinutes(durationMinutes)
                    .build();
            bookProgressRepository.save(progress);
        }

        // 4. Update Streak Counter
        streakService.recordActivity(user, pagesRead);

        // 5. Update Gamification Rewards (XP, Level, Badges)
        gamificationService.processSessionRewards(user, plan, pagesRead, durationMinutes, isCompleted);

        // 6. Recalculate Stats & Classification
        analyticsService.recalculateUserStats(user);

        // Check if there are achievements to unlock
        gamificationService.checkAndUnlockAchievements(user);

        log.info("Ended reading session {}. Pages read: {}, Speed: {} WPM, Completed: {}", 
                session.getId(), pagesRead, speedWpm, isCompleted);
        return savedSession;
    }
}
