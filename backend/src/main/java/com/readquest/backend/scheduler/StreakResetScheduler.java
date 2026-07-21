package com.readquest.backend.scheduler;

import com.readquest.backend.entity.User;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.service.StreakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreakResetScheduler {

    private final UserRepository userRepository;
    private final StreakService streakService;

    // Run at midnight every day: "0 0 0 * * ?"
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetExpiredStreaks() {
        log.info("Starting background execution for reading streak verification...");
        List<User> users = userRepository.findAll();
        
        int count = 0;
        for (User user : users) {
            try {
                streakService.checkAndResetStreak(user);
                count++;
            } catch (Exception e) {
                log.error("Failed to verify streak for user {}: {}", user.getUsername(), e.getMessage());
            }
        }
        log.info("Streak verification completed. Verified {} users.", count);
    }
}
