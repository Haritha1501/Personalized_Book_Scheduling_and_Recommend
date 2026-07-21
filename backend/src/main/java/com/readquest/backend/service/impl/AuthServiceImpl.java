package com.readquest.backend.service.impl;

import com.readquest.backend.dto.*;
import com.readquest.backend.entity.*;
import com.readquest.backend.exception.BadRequestException;
import com.readquest.backend.exception.ResourceNotFoundException;
import com.readquest.backend.repository.*;
import com.readquest.backend.security.UserDetailsImpl;
import com.readquest.backend.security.jwt.JwtTokenProvider;
import com.readquest.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ReaderStatisticsRepository readerStatisticsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.refreshTokenExpirationMs}")
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public void registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Fetch user role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role is not found."));

        // Create new user's account
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .passwordHash(passwordEncoder.encode(signupRequest.getPassword()))
                .xp(0)
                .level(1)
                .currentStreak(0)
                .longestStreak(0)
                .roles(new HashSet<>(Collections.singletonList(userRole)))
                .build();

        User savedUser = userRepository.save(user);

        // Initialize Reader Statistics for the user
        ReaderStatistics stats = ReaderStatistics.builder()
                .user(savedUser)
                .totalBooksCompleted(0)
                .totalBooksInProgress(0)
                .totalPagesRead(0)
                .totalHoursRead(0.0)
                .avgReadingSpeedWpm(0.0)
                .favoriteGenre("Fiction")
                .favoriteAuthor("N/A")
                .completionRate(0.0)
                .lastUpdated(LocalDateTime.now())
                .build();

        readerStatisticsRepository.save(stats);
        log.info("Successfully registered user: {}", signupRequest.getUsername());
    }

    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtTokenProvider.generateJwtToken(authentication);

        // Fetch User details for extra stats in payload
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        
        // Remove old and create new refresh token
        RefreshToken refreshToken = createRefreshToken(user);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .xp(user.getXp())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .readingSpeedWpm(user.getReadingSpeedWpm())
                .readingType(user.getReadingType())
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenRepository.findByToken(requestRefreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
                    // Rotate refresh token
                    refreshTokenRepository.deleteByUser(user);
                    RefreshToken newRefreshToken = createRefreshToken(user);
                    return new TokenRefreshResponse(token, newRefreshToken.getToken());
                })
                .orElseThrow(() -> new BadRequestException("Refresh token is not in database!"));
    }

    @Override
    @Transactional
    public void logoutUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        refreshTokenRepository.deleteByUser(user);
        log.info("User {} successfully logged out", username);
    }

    private RefreshToken createRefreshToken(User user) {

    	RefreshToken refreshToken = refreshTokenRepository
        	    .findByUser(user)
            	.orElse(new RefreshToken());

    	refreshToken.setUser(user);
    	refreshToken.setToken(UUID.randomUUID().toString());
    	refreshToken.setExpiryDate(
            	Instant.now().plusMillis(refreshTokenDurationMs)
    	);

    	return refreshTokenRepository.save(refreshToken);
}

    private RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new BadRequestException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
