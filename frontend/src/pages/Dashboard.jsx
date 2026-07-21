import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import BookCard from '../components/BookCard';
import ReadingHeatmap from '../components/ReadingHeatmap';
import ProgressRing from '../components/ProgressRing';
import { useAuth } from '../context/AuthContext';
import { 
  MdPlayArrow, 
  MdOutlineMenuBook, 
  MdOutlineFlashOn, 
  MdTrendingUp, 
  MdStarBorder, 
  MdArrowForward 
} from 'react-icons/md';
import './Dashboard.css';

export const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [data, setData] = useState(null);
  const [recommendations, setRecommendations] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchDashboardData = async () => {
    try {
      const res = await axios.get('/api/dashboard');
      setData(res.data);
      
      const recRes = await axios.get('/api/books/recommendations');
      setRecommendations(recRes.data.slice(0, 5) || []);
    } catch (e) {
      console.error("Error loading dashboard data", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const handleStartPlan = (book) => {
    // If selecting recommended, jump to search or details
    navigate(`/search?q=${encodeURIComponent(book.title)}`);
  };

  const handleResumeSession = (planId) => {
    navigate(`/reading/${planId}`);
  };

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Dashboard" />
          <div className="dashboard-skeleton skeleton-container">
            <div className="skeleton skeleton-hero"></div>
            <div className="skeleton-grid">
              <div className="skeleton skeleton-card"></div>
              <div className="skeleton skeleton-card"></div>
              <div className="skeleton skeleton-card"></div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Active book is the first plan in list
  const activePlan = data?.activePlans?.[0] || null;

  // Calculate daily goal percentage
  const goalProgress = data ? (data.todayPagesRead / (data.dailyPagesGoal || 10)) * 100 : 0;
  
  // Calculate level progress percentage
  const levelProgress = data ? (data.xp / data.nextLevelXpThreshold) * 100 : 0;

  return (
    <div className="app-container">
      <Sidebar />
      
      <div className="main-content">
        <Navbar title="My Dashboard" />
        
        <div className="dashboard-content fade-in">
          {/* Welcome card row */}
          <div className="dashboard-hero-row">
            <div className="dashboard-welcome glass-card">
              <div className="welcome-text">
                <h2>Welcome Back, {user?.username}!</h2>
                <p>You are Level {data?.level} ({data?.levelName}). Keep reading to level up!</p>
              </div>
              
              <div className="level-progression-bar-container">
                <div className="progression-meta">
                  <span>Level {data?.level}</span>
                  <span>{data?.xp} / {data?.nextLevelXpThreshold} XP</span>
                  <span>Level {data?.level + 1}</span>
                </div>
                <div className="progression-bar-bg">
                  <div className="progression-bar-fill" style={{ width: `${Math.min(levelProgress, 100)}%` }}></div>
                </div>
              </div>
            </div>
          </div>

          {/* Core Analytics Cards Row */}
          <div className="dashboard-stats-grid">
            {/* 1. Continue Reading Quick Card */}
            <div className="continue-reading-card glass-card">
              <h3>Continue Reading</h3>
              {activePlan ? (
                <div className="active-book-focus">
                  <img src={activePlan.coverUrl} alt={activePlan.bookTitle} className="focus-cover" />
                  <div className="focus-meta">
                    <h4>{activePlan.bookTitle}</h4>
                    <p className="focus-author">by {activePlan.bookAuthor}</p>
                    
                    <div className="focus-progress-status">
                      <div className="progress-pills">
                        <span>{activePlan.currentProgressPages} / {activePlan.totalPages} Pages</span>
                        <span>{Math.round(activePlan.completionPercentage)}% Complete</span>
                      </div>
                      <div className="focus-progress-bar">
                        <div className="focus-progress-bar-fill" style={{ width: `${activePlan.completionPercentage}%` }}></div>
                      </div>
                    </div>

                    <button 
                      onClick={() => handleResumeSession(activePlan.id)} 
                      className="btn-primary resume-session-btn"
                    >
                      <MdPlayArrow />
                      Resume Session
                    </button>
                  </div>
                </div>
              ) : (
                <div className="empty-active-state text-center">
                  <MdOutlineMenuBook className="empty-icon" />
                  <p>You have no active reading plans. Choose a book from our search list to start.</p>
                  <button onClick={() => navigate('/search')} className="btn-primary empty-btn">
                    Find a Book
                  </button>
                </div>
              )}
            </div>

            {/* 2. Today's Reading Goal Ring */}
            <div className="daily-goal-card glass-card text-center">
              <h3>Today's Goal</h3>
              <div className="daily-goal-ring-box">
                <ProgressRing 
                  radius={70} 
                  stroke={10} 
                  progress={goalProgress} 
                  text={
                    <div className="ring-text-stack">
                      <span className="ring-bold-val">{data?.todayPagesRead}</span>
                      <span className="ring-lbl">Pages</span>
                    </div>
                  }
                />
              </div>
              <div className="daily-goal-meta">
                <div className="goal-detail-item">
                  <span className="goal-val">{data?.dailyPagesGoal} Pages</span>
                  <span className="goal-lbl">Daily Target</span>
                </div>
                <div className="goal-detail-item">
                  <span className="goal-val">{Math.round(data?.todayMinutesRead)} Min</span>
                  <span className="goal-lbl">Time Read</span>
                </div>
              </div>
              {goalProgress >= 100 ? (
                <p className="goal-motivation-alert font-success">🎉 Daily Goal Completed! (+50 XP earned)</p>
              ) : (
                <p className="goal-motivation-alert">
                  Read {Math.max(0, (data?.dailyPagesGoal || 10) - (data?.todayPagesRead || 0))} more pages to hit today's target!
                </p>
              )}
            </div>

            {/* 3. Streak / XP Card */}
            <div className="streaks-details-card glass-card">
              <h3>Streak & Achievements</h3>
              <div className="streak-stats-row">
                <div className="streak-stat-box pill-success">
                  <MdOutlineFlashOn className="streak-glow-icon" />
                  <div className="stat-val-group">
                    <span className="stat-num">{data?.currentStreak} Days</span>
                    <span className="stat-lbl">Current Streak</span>
                  </div>
                </div>
                <div className="streak-stat-box pill-info">
                  <MdTrendingUp className="streak-glow-icon" />
                  <div className="stat-val-group">
                    <span className="stat-num">{data?.longestStreak} Days</span>
                    <span className="stat-lbl">Longest Streak</span>
                  </div>
                </div>
              </div>

              <div className="recent-badges-box">
                <h4>Recent Badges</h4>
                <div className="recent-badges-list">
                  {data?.recentAchievements && data.recentAchievements.length > 0 ? (
                    data.recentAchievements.map(ac => (
                      <div key={ac.code} className="badge-mini-item" title={ac.description}>
                        <span className="badge-emoji">🏆</span>
                        <div className="badge-mini-meta">
                          <span className="badge-name">{ac.title}</span>
                          <span className="badge-date">{ac.unlockedAt}</span>
                        </div>
                      </div>
                    ))
                  ) : (
                    <p className="no-badges-yet">Start sessions and complete plans to unlock badges!</p>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Activity Heatmap Grid */}
          <div className="dashboard-heatmap-row">
            <ReadingHeatmap data={data?.heatmap} />
          </div>

          {/* Recommendations Carousel Row */}
          <div className="dashboard-recommendations-row">
            <div className="recommendations-row-header">
              <h3>Personalized Recommendations</h3>
              <button onClick={() => navigate('/recommendations')} className="view-all-btn">
                <span>View Recommendations</span>
                <MdArrowForward />
              </button>
            </div>
            
            <div className="recommendations-grid">
              {recommendations.length > 0 ? (
                recommendations.map(book => (
                  <BookCard 
                    key={book.googleBookId || book.id} 
                    book={book} 
                    actionText="Select" 
                    onAction={handleStartPlan} 
                  />
                ))
              ) : (
                <p className="no-recs">Check back later for personalized smart suggestions!</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Dashboard;
