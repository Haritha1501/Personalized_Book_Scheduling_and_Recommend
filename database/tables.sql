-- ReadQuest Database Tables Setup
SET search_path TO readquest, public;

-- 1. reader_types Table
CREATE TABLE IF NOT EXISTS reader_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL
);

-- 2. users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    xp INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 1,
    reading_speed_wpm DOUBLE PRECISION,
    reading_accuracy DOUBLE PRECISION,
    reading_type VARCHAR(50),
    reader_type_id BIGINT,
    current_streak INTEGER NOT NULL DEFAULT 0,
    longest_streak INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 4. user_roles Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- 5. refresh_tokens Table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL
);

-- 6. books Table
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    google_book_id VARCHAR(50) UNIQUE,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255),
    cover_url TEXT,
    description TEXT,
    total_pages INTEGER NOT NULL,
    categories VARCHAR(255),
    average_rating DOUBLE PRECISION,
    language VARCHAR(50),
    isbn VARCHAR(50),
    publisher VARCHAR(100),
    published_date VARCHAR(50)
);

-- 7. reading_plans Table
CREATE TABLE IF NOT EXISTS reading_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    target_days INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL,
    target_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    current_progress_pages INTEGER NOT NULL DEFAULT 0,
    daily_pages_goal INTEGER NOT NULL,
    daily_minutes_goal DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 8. reading_sessions Table
CREATE TABLE IF NOT EXISTS reading_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reading_plan_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    start_page INTEGER NOT NULL,
    end_page INTEGER NOT NULL,
    pages_read INTEGER NOT NULL,
    duration_minutes DOUBLE PRECISION NOT NULL,
    reading_speed_wpm DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 9. book_progress Table
CREATE TABLE IF NOT EXISTS book_progress (
    id BIGSERIAL PRIMARY KEY,
    reading_plan_id BIGINT NOT NULL,
    pages_read INTEGER NOT NULL,
    duration_minutes DOUBLE PRECISION NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 10. daily_streaks Table
CREATE TABLE IF NOT EXISTS daily_streaks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    activity_date DATE NOT NULL,
    pages_read INTEGER NOT NULL DEFAULT 0,
    UNIQUE (user_id, activity_date)
);

-- 11. achievements Table
CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    xp_reward INTEGER NOT NULL,
    icon_url VARCHAR(255)
);

-- 12. user_achievements Table
CREATE TABLE IF NOT EXISTS user_achievements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, achievement_id)
);

-- 13. reader_statistics Table
CREATE TABLE IF NOT EXISTS reader_statistics (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    total_books_completed INTEGER NOT NULL DEFAULT 0,
    total_books_in_progress INTEGER NOT NULL DEFAULT 0,
    total_pages_read INTEGER NOT NULL DEFAULT 0,
    total_hours_read DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    avg_reading_speed_wpm DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    favorite_genre VARCHAR(100),
    favorite_author VARCHAR(100),
    completion_rate DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 14. recommendations Table
CREATE TABLE IF NOT EXISTS recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    recommended_by VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 15. notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
