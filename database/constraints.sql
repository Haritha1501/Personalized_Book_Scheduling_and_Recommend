-- ReadQuest Database Constraints Setup
SET search_path TO readquest, public;

-- Foreign Keys for users
ALTER TABLE users 
    ADD CONSTRAINT fk_users_reader_type FOREIGN KEY (reader_type_id) REFERENCES reader_types(id) ON DELETE SET NULL;

-- Foreign Keys for user_roles
ALTER TABLE user_roles 
    ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- Foreign Keys for refresh_tokens
ALTER TABLE refresh_tokens 
    ADD CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Foreign Keys for reading_plans
ALTER TABLE reading_plans 
    ADD CONSTRAINT fk_reading_plans_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_reading_plans_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE;

-- Foreign Keys for reading_sessions
ALTER TABLE reading_sessions 
    ADD CONSTRAINT fk_reading_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_reading_sessions_plan FOREIGN KEY (reading_plan_id) REFERENCES reading_plans(id) ON DELETE CASCADE;

-- Foreign Keys for book_progress
ALTER TABLE book_progress 
    ADD CONSTRAINT fk_book_progress_plan FOREIGN KEY (reading_plan_id) REFERENCES reading_plans(id) ON DELETE CASCADE;

-- Foreign Keys for daily_streaks
ALTER TABLE daily_streaks 
    ADD CONSTRAINT fk_daily_streaks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Foreign Keys for user_achievements
ALTER TABLE user_achievements 
    ADD CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements(id) ON DELETE CASCADE;

-- Foreign Keys for reader_statistics
ALTER TABLE reader_statistics 
    ADD CONSTRAINT fk_reader_statistics_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Foreign Keys for recommendations
ALTER TABLE recommendations 
    ADD CONSTRAINT fk_recommendations_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_recommendations_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE;

-- Foreign Keys for notifications
ALTER TABLE notifications 
    ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
