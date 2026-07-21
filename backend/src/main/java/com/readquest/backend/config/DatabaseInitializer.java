package com.readquest.backend.config;

import com.readquest.backend.entity.Achievement;
import com.readquest.backend.entity.ReaderType;
import com.readquest.backend.entity.Role;
import com.readquest.backend.repository.AchievementRepository;
import com.readquest.backend.repository.ReaderTypeRepository;
import com.readquest.backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final ReaderTypeRepository readerTypeRepository;
    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting automated database seeding checks...");

        seedRoles();
        seedReaderTypes();
        seedAchievements();

        log.info("Automated database seeding checks completed successfully.");
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            log.info("Seeding roles table...");
            Role userRole = Role.builder().name("ROLE_USER").build();
            Role adminRole = Role.builder().name("ROLE_ADMIN").build();
            roleRepository.saveAll(Arrays.asList(userRole, adminRole));
        }
    }

    private void seedReaderTypes() {
        if (readerTypeRepository.count() == 0) {
            log.info("Seeding reader classifications table...");
            List<ReaderType> types = Arrays.asList(
                ReaderType.builder().code("CASUAL").name("Casual Reader").description("Reads occasionally, usually short sessions.").build(),
                ReaderType.builder().code("WEEKEND").name("Weekend Reader").description("Active mostly on Saturdays and Sundays.").build(),
                ReaderType.builder().code("SCHOLAR").name("Scholar").description("Reads high volume, long sessions, highly consistent.").build(),
                ReaderType.builder().code("EXPLORER").name("Explorer").description("Reads diverse genres, always searching for new subjects.").build(),
                ReaderType.builder().code("NIGHT_OWL").name("Night Owl").description("Prefers late-night reading sessions.").build(),
                ReaderType.builder().code("SPEED_READER").name("Speed Reader").description("High reading speed (WPM) with good accuracy.").build(),
                ReaderType.builder().code("BOOK_COLLECTOR").name("Book Collector").description("Has many books in library and high target completion.").build(),
                ReaderType.builder().code("CONSISTENT").name("Consistent Reader").description("Reads every single day without fail.").build(),
                ReaderType.builder().code("FINISHER").name("Finisher").description("Has a 100% completion rate on reading plans.").build()
            );
            readerTypeRepository.saveAll(types);
        }
    }

    private void seedAchievements() {
        if (achievementRepository.count() == 0) {
            log.info("Seeding system achievements table...");
            List<Achievement> achievements = Arrays.asList(
                Achievement.builder().code("FIRST_BOOK").title("First Book Completed").description("You completed your first reading plan! Keep it up.").xpReward(500).iconUrl("first_book.png").build(),
                Achievement.builder().code("PAGES_100").title("Centurion Reader").description("Read a total of 100 pages across all sessions.").xpReward(100).iconUrl("pages_100.png").build(),
                Achievement.builder().code("PAGES_1000").title("Millennium Reader").description("Read a total of 1,000 pages.").xpReward(300).iconUrl("pages_1000.png").build(),
                Achievement.builder().code("PAGES_5000").title("Sage of Pages").description("Read a total of 5,000 pages.").xpReward(500).iconUrl("pages_5000.png").build(),
                Achievement.builder().code("BOOKS_10").title("Library Apprentice").description("Complete 10 book reading plans.").xpReward(1000).iconUrl("books_10.png").build(),
                Achievement.builder().code("BOOKS_25").title("Library Master").description("Complete 25 book reading plans.").xpReward(1500).iconUrl("books_25.png").build(),
                Achievement.builder().code("BOOKS_50").title("Legendary Archivist").description("Complete 50 book reading plans.").xpReward(2000).iconUrl("books_50.png").build(),
                Achievement.builder().code("BOOKS_100").title("Omniscient Reader").description("Complete 100 book reading plans.").xpReward(5000).iconUrl("books_100.png").build(),
                Achievement.builder().code("STREAK_7").title("Week-long Scholar").description("Maintain a reading streak for 7 days.").xpReward(100).iconUrl("streak_7.png").build(),
                Achievement.builder().code("STREAK_30").title("Habit Titan").description("Maintain a reading streak for 30 days.").xpReward(500).iconUrl("streak_30.png").build(),
                Achievement.builder().code("STREAK_365").title("Year of Wisdom").description("Maintain a reading streak for 365 days.").xpReward(2000).iconUrl("streak_365.png").build(),
                Achievement.builder().code("NIGHT_READER").title("Midnight Spark").description("Complete a session between 10 PM and 4 AM.").xpReward(150).iconUrl("night_reader.png").build(),
                Achievement.builder().code("MORNING_READER").title("Early Sunrise").description("Complete a session between 5 AM and 9 AM.").xpReward(150).iconUrl("morning_reader.png").build(),
                Achievement.builder().code("WEEKEND_READER").title("Weekend Warrior").description("Complete a session on Saturday or Sunday.").xpReward(150).iconUrl("weekend_reader.png").build()
            );
            achievementRepository.saveAll(achievements);
        }
    }
}
