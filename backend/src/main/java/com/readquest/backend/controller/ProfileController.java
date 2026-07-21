package com.readquest.backend.controller;

import com.readquest.backend.dto.ProfileResponse;
import com.readquest.backend.dto.SpeedTestRequest;
import com.readquest.backend.entity.ReaderType;
import com.readquest.backend.entity.User;
import com.readquest.backend.repository.ReaderTypeRepository;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.security.UserDetailsImpl;
import com.readquest.backend.service.AnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProfileController {

    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;
    private final ReaderTypeRepository readerTypeRepository;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfileData() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ProfileResponse profile = analyticsService.getUserProfileData(userDetails.getId());
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/speedtest")
    public ResponseEntity<?> submitSpeedTest(@Valid @RequestBody SpeedTestRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();

        user.setReadingSpeedWpm(request.getWpm());
        user.setReadingAccuracy(request.getAccuracy());
        user.setReadingType(request.getReadingType());

        // Select an initial classification code matching their speed type
        String code = "CASUAL";
        if ("Speed Reader".equals(request.getReadingType()) || "Fast Reader".equals(request.getReadingType())) {
            code = "SPEED_READER";
        }

        ReaderType readerType = readerTypeRepository.findByCode(code).orElse(null);
        if (readerType != null) {
            user.setReaderType(readerType);
        }

        userRepository.save(user);

        // Re-run statistics calculations
        analyticsService.recalculateUserStats(user);

        return ResponseEntity.ok(user);
    }
}
