package com.readquest.backend.service.impl;

import com.readquest.backend.entity.DailyStreak;
import com.readquest.backend.entity.User;
import com.readquest.backend.repository.DailyStreakRepository;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements StreakService {

    private final DailyStreakRepository dailyStreakRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void recordActivity(User user, int pagesRead) {
        if (pagesRead <= 0) return;

        LocalDate today = LocalDate.now();
        Optional<DailyStreak> todayStreakOpt = dailyStreakRepository.findByUserIdAndActivityDate(user.getId(), today);

        if (todayStreakOpt.isPresent()) {
            DailyStreak todayStreak = todayStreakOpt.get();
            todayStreak.setPagesRead(todayStreak.getPagesRead() + pagesRead);
            dailyStreakRepository.save(todayStreak);
            log.info("Updated daily streak record for user {} on {}. Pages read today: {}", 
                    user.getUsername(), today, todayStreak.getPagesRead());
        } else {
            // First activity of today
            DailyStreak newStreak = DailyStreak.builder()
                    .user(user)
                    .activityDate(today)
                    .pagesRead(pagesRead)
                    .build();
            dailyStreakRepository.save(newStreak);

            // Check yesterday
            LocalDate yesterday = today.minusDays(1);
            Optional<DailyStreak> yesterdayStreakOpt = dailyStreakRepository.findByUserIdAndActivityDate(user.getId(), yesterday);

            if (yesterdayStreakOpt.isPresent() && yesterdayStreakOpt.get().getPagesRead() > 0) {
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else {
                user.setCurrentStreak(1);
            }

            if (user.getCurrentStreak() > user.getLongestStreak()) {
                user.setLongestStreak(user.getCurrentStreak());
            }
            userRepository.save(user);
            log.info("New daily activity logged for user {}. Current streak: {}", user.getUsername(), user.getCurrentStreak());
        }
    }

    @Override
    @Transactional
    public void checkAndResetStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Optional<DailyStreak> todayStreak = dailyStreakRepository.findByUserIdAndActivityDate(user.getId(), today);
        Optional<DailyStreak> yesterdayStreak = dailyStreakRepository.findByUserIdAndActivityDate(user.getId(), yesterday);

        boolean readToday = todayStreak.isPresent() && todayStreak.get().getPagesRead() > 0;
        boolean readYesterday = yesterdayStreak.isPresent() && yesterdayStreak.get().getPagesRead() > 0;

        if (!readToday && !readYesterday) {
            // Missed both today and yesterday, reset streak
            if (user.getCurrentStreak() > 0) {
                user.setCurrentStreak(0);
                userRepository.save(user);
                log.info("Streak reset to 0 for user {} due to inactivity.", user.getUsername());
            }
        }
    }
}
