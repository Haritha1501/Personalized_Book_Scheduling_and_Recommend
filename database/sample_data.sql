-- ReadQuest Database Sample/Seed Data
SET search_path TO readquest, public;

-- Seed reader_types
INSERT INTO reader_types (code, name, description) VALUES
('CASUAL', 'Casual Reader', 'Reads occasionally, usually short sessions.'),
('WEEKEND', 'Weekend Reader', 'Active mostly on Saturdays and Sundays.'),
('SCHOLAR', 'Scholar', 'Reads high volume, long sessions, highly consistent.'),
('EXPLORER', 'Explorer', 'Reads diverse genres, always searching for new subjects.'),
('NIGHT_OWL', 'Night Owl', 'Prefers late-night reading sessions.'),
('SPEED_READER', 'Speed Reader', 'High reading speed (WPM) with good accuracy.'),
('BOOK_COLLECTOR', 'Book Collector', 'Has many books in library and high target completion.'),
('CONSISTENT', 'Consistent Reader', 'Reads every single day without fail.'),
('FINISHER', 'Finisher', 'Has a 100% completion rate on reading plans.');

-- Seed roles
INSERT INTO roles (name) VALUES
('ROLE_USER'),
('ROLE_ADMIN');

-- Seed achievements
INSERT INTO achievements (code, title, description, xp_reward, icon_url) VALUES
('FIRST_BOOK', 'First Book Completed', 'You completed your first reading plan! Keep it up.', 500, 'first_book.png'),
('PAGES_100', 'Centurion Reader', 'Read a total of 100 pages across all sessions.', 100, 'pages_100.png'),
('PAGES_1000', 'Millennium Reader', 'Read a total of 1,000 pages.', 300, 'pages_1000.png'),
('PAGES_5000', 'Sage of Pages', 'Read a total of 5,000 pages.', 500, 'pages_5000.png'),
('BOOKS_10', 'Library Apprentice', 'Complete 10 book reading plans.', 1000, 'books_10.png'),
('BOOKS_25', 'Library Master', 'Complete 25 book reading plans.', 1500, 'books_25.png'),
('BOOKS_50', 'Legendary Archivist', 'Complete 50 book reading plans.', 2000, 'books_50.png'),
('BOOKS_100', 'Omniscient Reader', 'Complete 100 book reading plans.', 5000, 'books_100.png'),
('STREAK_7', 'Week-long Scholar', 'Maintain a reading streak for 7 days.', 100, 'streak_7.png'),
('STREAK_30', 'Habit Titan', 'Maintain a reading streak for 30 days.', 500, 'streak_30.png'),
('STREAK_365', 'Year of Wisdom', 'Maintain a reading streak for 365 days.', 2000, 'streak_365.png'),
('NIGHT_READER', 'Midnight Spark', 'Complete a session between 10 PM and 4 AM.', 150, 'night_reader.png'),
('MORNING_READER', 'Early Sunrise', 'Complete a session between 5 AM and 9 AM.', 150, 'morning_reader.png'),
('WEEKEND_READER', 'Weekend Warrior', 'Complete a session on Saturday or Sunday.', 150, 'weekend_reader.png');

-- Seed a default user (password is 'password')
-- BCrypt of 'password' = $2a$10$v2H7b.lqZ0t3eS/R4YlGjOpU4C9G9.mK7Ym5Ew8Z1pB3d9/L3gOLe
INSERT INTO users (username, email, password_hash, xp, level, reading_speed_wpm, reading_accuracy, reading_type, reader_type_id) VALUES
('reader', 'reader@readquest.com', '$2a$10$v2H7b.lqZ0t3eS/R4YlGjOpU4C9G9.mK7Ym5Ew8Z1pB3d9/L3gOLe', 120, 1, 250.0, 95.0, 'Average Reader', 1),
('admin', 'admin@readquest.com', '$2a$10$v2H7b.lqZ0t3eS/R4YlGjOpU4C9G9.mK7Ym5Ew8Z1pB3d9/L3gOLe', 500, 2, 320.0, 98.0, 'Fast Reader', 3);

-- Map users to roles
INSERT INTO user_roles (user_id, role_id) VALUES
((SELECT id FROM users WHERE username = 'reader'), (SELECT id FROM roles WHERE name = 'ROLE_USER')),
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_USER')),
((SELECT id FROM users WHERE username = 'admin'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN'));

-- Initialize reader statistics for seeded users
INSERT INTO reader_statistics (user_id, total_books_completed, total_books_in_progress, total_pages_read, total_hours_read, avg_reading_speed_wpm, favorite_genre, favorite_author, completion_rate) VALUES
((SELECT id FROM users WHERE username = 'reader'), 0, 0, 0, 0.0, 250.0, 'Fiction', 'N/A', 0.0),
((SELECT id FROM users WHERE username = 'admin'), 0, 0, 0, 0.0, 320.0, 'Science', 'N/A', 0.0);
