package com.readquest.backend.service;

import com.readquest.backend.dto.ProfileResponse;
import com.readquest.backend.entity.User;

public interface AnalyticsService {
    void recalculateUserStats(User user);
    ProfileResponse getUserProfileData(Long userId);
}
