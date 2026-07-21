package com.readquest.backend.service;

import com.readquest.backend.entity.ReadingPlan;
import com.readquest.backend.entity.User;

public interface GamificationService {
    void processSessionRewards(User user, ReadingPlan plan, int pagesRead, double durationMinutes, boolean isCompleted);
    void checkAndUnlockAchievements(User user);
    void checkProgressMilestones(User user, ReadingPlan plan);
}
