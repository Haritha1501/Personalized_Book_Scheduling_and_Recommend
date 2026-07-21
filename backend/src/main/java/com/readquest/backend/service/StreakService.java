package com.readquest.backend.service;

import com.readquest.backend.entity.User;

public interface StreakService {
    void recordActivity(User user, int pagesRead);
    void checkAndResetStreak(User user);
}
