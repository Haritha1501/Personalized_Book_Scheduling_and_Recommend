package com.readquest.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class JwtResponse {
    private String token;
    private String refreshToken;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Integer xp;
    private Integer level;
    private Integer currentStreak;
    private Double readingSpeedWpm;
    private String readingType;
}
