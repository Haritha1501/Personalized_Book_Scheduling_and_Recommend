package com.readquest.backend.controller;

import com.readquest.backend.dto.ReadingPlanRequest;
import com.readquest.backend.dto.ReadingSessionEndRequest;
import com.readquest.backend.dto.ReadingSessionStartRequest;
import com.readquest.backend.entity.ReadingPlan;
import com.readquest.backend.entity.ReadingSession;
import com.readquest.backend.security.UserDetailsImpl;
import com.readquest.backend.service.ReadingPlanService;
import com.readquest.backend.service.ReadingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReadingController {

    private final ReadingPlanService readingPlanService;
    private final ReadingSessionService readingSessionService;

    @PostMapping("/plans")
    public ResponseEntity<ReadingPlan> createReadingPlan(@Valid @RequestBody ReadingPlanRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReadingPlan plan = readingPlanService.createReadingPlan(userDetails.getId(), request);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/plans/active")
    public ResponseEntity<List<ReadingPlan>> getActivePlans() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ReadingPlan> plans = readingPlanService.getActivePlans(userDetails.getId());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<ReadingPlan> getPlanDetails(@PathVariable Long planId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReadingPlan plan = readingPlanService.getPlanDetails(userDetails.getId(), planId);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/sessions/start")
    public ResponseEntity<ReadingSession> startSession(@Valid @RequestBody ReadingSessionStartRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReadingSession session = readingSessionService.startSession(userDetails.getId(), request);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/end")
    public ResponseEntity<ReadingSession> endSession(@Valid @RequestBody ReadingSessionEndRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ReadingSession session = readingSessionService.endSession(userDetails.getId(), request);
        return ResponseEntity.ok(session);
    }
}
