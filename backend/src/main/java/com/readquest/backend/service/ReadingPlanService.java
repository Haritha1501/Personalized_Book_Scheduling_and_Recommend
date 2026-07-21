package com.readquest.backend.service;

import com.readquest.backend.dto.ReadingPlanRequest;
import com.readquest.backend.entity.ReadingPlan;

import java.util.List;

public interface ReadingPlanService {
    ReadingPlan createReadingPlan(Long userId, ReadingPlanRequest request);
    List<ReadingPlan> getActivePlans(Long userId);
    ReadingPlan getPlanDetails(Long userId, Long planId);
}
