package com.readquest.backend.service.impl;

import com.readquest.backend.dto.AdminStatsResponse;
import com.readquest.backend.entity.*;
import com.readquest.backend.repository.*;
import com.readquest.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingPlanRepository readingPlanRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReaderStatisticsRepository readerStatisticsRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getAdminStatistics() {
        long totalUsers = userRepository.count();
        long totalBooks = bookRepository.count();
        long totalReadingPlans = readingPlanRepository.count();
        long totalSessions = readingSessionRepository.count();

        // 1. Top Active Readers (by XP)
        List<User> topUsers = userRepository.findAll(
                Sort.by(Sort.Direction.DESC, "xp")
        ).stream().limit(5).collect(Collectors.toList());

        List<AdminStatsResponse.ActiveReaderDetail> activeReaders = topUsers.stream().map(u -> {
            ReaderStatistics stats = readerStatisticsRepository.findByUserId(u.getId()).orElse(null);
            int totalPages = stats != null ? stats.getTotalPagesRead() : 0;
            return AdminStatsResponse.ActiveReaderDetail.builder()
                    .username(u.getUsername())
                    .email(u.getEmail())
                    .xp(u.getXp())
                    .level(u.getLevel())
                    .totalPagesRead(totalPages)
                    .currentStreak(u.getCurrentStreak())
                    .build();
        }).collect(Collectors.toList());

        // 2. Popular Books (aggregate plans count)
        List<ReadingPlan> allPlans = readingPlanRepository.findAll();
        Map<Book, Long> bookPlanCounts = allPlans.stream()
                .collect(Collectors.groupingBy(ReadingPlan::getBook, Collectors.counting()));

        List<AdminStatsResponse.PopularBookDetail> popularBooks = bookPlanCounts.entrySet().stream()
                .sorted(Map.Entry.<Book, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> AdminStatsResponse.PopularBookDetail.builder()
                        .title(entry.getKey().getTitle())
                        .author(entry.getKey().getAuthor())
                        .activePlanCount(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 3. Trending Genres
        Map<String, Long> genreCounts = allPlans.stream()
                .map(p -> p.getBook().getCategories())
                .filter(c -> c != null && !c.trim().isEmpty())
                .map(c -> c.split(",")[0].trim())
                .collect(Collectors.groupingBy(g -> g, Collectors.counting()));

        List<AdminStatsResponse.GenreTrend> trendingGenres = genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> AdminStatsResponse.GenreTrend.builder()
                        .genre(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        // 4. Reader Classifications Distribution
        List<User> allUsers = userRepository.findAll();
        Map<String, Long> classificationCounts = allUsers.stream()
                .map(User::getReaderType)
                .filter(Objects::nonNull)
                .map(ReaderType::getName)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        List<AdminStatsResponse.ClassificationDistribution> classifications = classificationCounts.entrySet().stream()
                .map(entry -> AdminStatsResponse.ClassificationDistribution.builder()
                        .name(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalBooks(totalBooks)
                .totalReadingPlans(totalReadingPlans)
                .totalSessions(totalSessions)
                .activeReaders(activeReaders)
                .popularBooks(popularBooks)
                .trendingGenres(trendingGenres)
                .readerClassifications(classifications)
                .build();
    }
}
