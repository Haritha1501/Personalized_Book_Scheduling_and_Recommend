package com.readquest.backend.service.impl;

import com.readquest.backend.dto.ReadingPlanRequest;
import com.readquest.backend.entity.Book;
import com.readquest.backend.entity.ReaderStatistics;
import com.readquest.backend.entity.ReadingPlan;
import com.readquest.backend.entity.User;
import com.readquest.backend.exception.BadRequestException;
import com.readquest.backend.exception.ResourceNotFoundException;
import com.readquest.backend.repository.BookRepository;
import com.readquest.backend.repository.ReaderStatisticsRepository;
import com.readquest.backend.repository.ReadingPlanRepository;
import com.readquest.backend.repository.UserRepository;
import com.readquest.backend.service.ReadingPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingPlanServiceImpl implements ReadingPlanService {

    private final ReadingPlanRepository readingPlanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReaderStatisticsRepository readerStatisticsRepository;

    @Override
    @Transactional
    public ReadingPlan createReadingPlan(Long userId, ReadingPlanRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1. Resolve Book (fetch cached or create)
        Book book = null;
        if (request.getGoogleBookId() != null && !request.getGoogleBookId().trim().isEmpty()) {
            Optional<Book> cachedBook = bookRepository.findByGoogleBookId(request.getGoogleBookId());
            if (cachedBook.isPresent()) {
                book = cachedBook.get();
            }
        }

        if (book == null) {
            book = Book.builder()
                    .googleBookId(request.getGoogleBookId())
                    .title(request.getTitle())
                    .author(request.getAuthor())
                    .coverUrl(request.getCoverUrl())
                    .description(request.getDescription())
                    .totalPages(request.getTotalPages())
                    .categories(request.getCategories())
                    .averageRating(request.getAverageRating() != null ? request.getAverageRating() : 0.0)
                    .language(request.getLanguage())
                    .isbn(request.getIsbn())
                    .publisher(request.getPublisher())
                    .publishedDate(request.getPublishedDate())
                    .build();
            book = bookRepository.save(book);
        }

        // Check if there is already an active plan for this user and this book
        Optional<ReadingPlan> existingActive = readingPlanRepository.findByUserIdAndBookIdAndStatus(userId, book.getId(), "ACTIVE");
        if (existingActive.isPresent()) {
            log.info("Reading plan already active for user {} and book {}", userId, book.getTitle());
            return existingActive.get();
        }

        // 2. Calculations
        double wpm = user.getReadingSpeedWpm() != null ? user.getReadingSpeedWpm() : 250.0;
        int targetDays = request.getTargetDays();
        
        int dailyPagesGoal = (int) Math.ceil((double) book.getTotalPages() / targetDays);
        // Estimate 250 words per page
        double dailyMinutesGoal = ((double) dailyPagesGoal * 250.0) / wpm;

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime targetDate = startDate.plusDays(targetDays);

        ReadingPlan plan = ReadingPlan.builder()
                .user(user)
                .book(book)
                .targetDays(targetDays)
                .startDate(startDate)
                .targetDate(targetDate)
                .status("ACTIVE")
                .currentProgressPages(0)
                .dailyPagesGoal(dailyPagesGoal)
                .dailyMinutesGoal(dailyMinutesGoal)
                .build();

        ReadingPlan savedPlan = readingPlanRepository.save(plan);

        // Update Reader Statistics
        ReaderStatistics stats = readerStatisticsRepository.findByUserId(userId)
                .orElseGet(() -> ReaderStatistics.builder().user(user).build());
        stats.setTotalBooksInProgress(stats.getTotalBooksInProgress() + 1);
        readerStatisticsRepository.save(stats);

        log.info("Created reading plan {} for user {}", savedPlan.getId(), user.getUsername());
        return savedPlan;
    }

    @Override
    public List<ReadingPlan> getActivePlans(Long userId) {
        return readingPlanRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }

    @Override
    public ReadingPlan getPlanDetails(Long userId, Long planId) {
        ReadingPlan plan = readingPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Reading plan not found: " + planId));
        if (!plan.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized access to reading plan.");
        }
        return plan;
    }
}
