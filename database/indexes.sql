-- ReadQuest Database Indexes Setup
SET search_path TO readquest, public;

-- Index for reading plans lookup by user and status
CREATE INDEX IF NOT EXISTS idx_reading_plans_user_status ON reading_plans(user_id, status);

-- Index for reading sessions by plan
CREATE INDEX IF NOT EXISTS idx_reading_sessions_plan ON reading_sessions(reading_plan_id);

-- Index for reading sessions by user
CREATE INDEX IF NOT EXISTS idx_reading_sessions_user ON reading_sessions(user_id);

-- Index for daily streaks lookup by date
CREATE INDEX IF NOT EXISTS idx_daily_streaks_user_date ON daily_streaks(user_id, activity_date);

-- Index for achievements unlocked by user
CREATE INDEX IF NOT EXISTS idx_user_ach_user ON user_achievements(user_id);

-- Index for unread user notifications
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read);

-- Index for book recommendations by user
CREATE INDEX IF NOT EXISTS idx_recommendations_user ON recommendations(user_id);
