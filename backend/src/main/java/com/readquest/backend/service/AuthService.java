package com.readquest.backend.service;

import com.readquest.backend.dto.*;

public interface AuthService {
    void registerUser(SignupRequest signupRequest);
    JwtResponse authenticateUser(LoginRequest loginRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    void logoutUser(String username);
}
