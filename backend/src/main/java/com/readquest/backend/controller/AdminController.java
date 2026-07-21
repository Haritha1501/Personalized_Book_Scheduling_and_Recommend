package com.readquest.backend.controller;

import com.readquest.backend.dto.AdminStatsResponse;
import com.readquest.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminStatsResponse> getAdminStatistics() {
        AdminStatsResponse stats = adminService.getAdminStatistics();
        return ResponseEntity.ok(stats);
    }
}
