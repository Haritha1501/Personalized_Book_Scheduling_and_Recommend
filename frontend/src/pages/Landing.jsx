import React from 'react';
import { Link } from 'react-router-dom';
import { MdTrendingUp, MdSchool, MdStars, MdCheckCircleOutline } from 'react-icons/md';
import './Landing.css';

export const Landing = () => {
  return (
    <div className="landing-page">
      {/* Background decoration */}
      <div className="landing-bg-glow glow-1"></div>
      <div className="landing-bg-glow glow-2"></div>

      <header className="landing-header">
        <div className="landing-logo">
          <span className="logo-icon">📖</span>
          <span className="logo-text">ReadQuest</span>
        </div>
        <div className="landing-nav">
          <Link to="/login" className="btn-secondary">Login</Link>
          <Link to="/signup" className="btn-primary">Get Started</Link>
        </div>
      </header>

      <main className="landing-hero">
        <div className="hero-content">
          <span className="hero-badge">✨ Level Up Your Reading Habit</span>
          <h1>Personalized Reading Scheduler & Gamified Tracker</h1>
          <p>
            ReadQuest combines smart target calculations with Duolingo-style gamification. 
            Test your reading speed, create custom calendars, track streaks, and unlock certificates.
          </p>
          <div className="hero-actions">
            <Link to="/signup" className="btn-primary hero-btn">Create Free Account</Link>
            <Link to="/login" className="btn-secondary hero-btn">Explore App</Link>
          </div>
        </div>

        <div className="hero-visual">
          <div className="mock-card-container">
            <div className="mock-card glass-card visual-card-1">
              <span className="mock-card-tag">Current Streak</span>
              <h3>🔥 14 Days Active</h3>
              <p>You read 45 pages today</p>
            </div>
            <div className="mock-card glass-card visual-card-2">
              <span className="mock-card-tag">Speed Test</span>
              <h3>⚡ 310 WPM</h3>
              <p>Fast Reader (98% Accuracy)</p>
            </div>
            <div className="mock-card glass-card visual-card-3">
              <span className="mock-card-tag">Level Progress</span>
              <h3>🌟 Lv.3 Scholar</h3>
              <p>1,250 / 1,500 XP</p>
            </div>
          </div>
        </div>
      </main>

      <section className="landing-features">
        <h2 className="features-title">Why Gamify Your Reading?</h2>
        <div className="features-grid">
          <div className="feature-item glass-card">
            <div className="feature-icon-wrapper font-success">
              <MdSchool />
            </div>
            <h3>Outread Speed Test</h3>
            <p>Assess your words-per-minute speed and comprehension. Skip the generic assumptions.</p>
          </div>

          <div className="feature-item glass-card">
            <div className="feature-icon-wrapper font-accent">
              <MdTrendingUp />
            </div>
            <h3>Smart Schedulers</h3>
            <p>Select books, input target completion days, and let our algorithm compute daily targets.</p>
          </div>

          <div className="feature-item glass-card">
            <div className="feature-icon-wrapper font-stars">
              <MdStars />
            </div>
            <h3>Gamified Milestones</h3>
            <p>Earn XP, level up, and unlock certificates and custom badges at 25%, 50%, 75%, and 100% completion.</p>
          </div>
        </div>
      </section>

      <footer className="landing-footer">
        <p>&copy; {new Date().getFullYear()} ReadQuest. Built for readers who want to succeed.</p>
      </footer>
    </div>
  );
};
export default Landing;
