import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { MdEmojiEvents, MdLockOutline } from 'react-icons/md';
import './Achievements.css';

export const Achievements = () => {
  const [achievements, setAchievements] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAchievements = async () => {
    try {
      const res = await axios.get('/api/profile');
      setAchievements(res.data.achievements || []);
    } catch (e) {
      console.error("Failed to load achievements list", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAchievements();
  }, []);

  const getEmoji = (code) => {
    if (code.startsWith('PROGRESS')) return '🎖️';
    if (code.startsWith('STREAK')) return '🔥';
    if (code.startsWith('PAGES')) return '📄';
    if (code.startsWith('BOOKS')) return '📚';
    return '🏆';
  };

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="My Achievements" />

        <div className="achievements-page-content fade-in">
          <div className="achievements-hero-card glass-card">
            <div className="achievements-hero-icon"><MdEmojiEvents /></div>
            <h2>Badges & Achievements Catalog</h2>
            <p>
              Complete daily targets, maintain streaks, read high page counts, and finish reading plans to unlock 
              exclusive badges and earn bonus XP.
            </p>
          </div>

          <div className="achievements-lists-grid">
            {loading ? (
              <div className="skeleton-container">
                <div className="skeleton" style={{ height: '100px' }}></div>
                <div className="skeleton" style={{ height: '100px' }}></div>
              </div>
            ) : achievements.length > 0 ? (
              <div className="achievements-showcase-grid">
                {achievements.map(ach => (
                  <div 
                    key={ach.code} 
                    className={`achievement-detail-card glass-card ${ach.unlocked ? 'unlocked' : 'locked'}`}
                  >
                    <div className="badge-visual-container">
                      <span className="badge-illustration">{getEmoji(ach.code)}</span>
                      {!ach.unlocked && <MdLockOutline className="badge-lock-icon" />}
                    </div>

                    <div className="badge-detail-meta">
                      <h3>{ach.title}</h3>
                      <p>{ach.description}</p>
                      <span className="badge-xp-reward">+{ach.xpReward} XP</span>
                      
                      {ach.unlocked ? (
                        <span className="badge-status-unlocked-at">Unlocked on {new Date(ach.unlockedAt).toLocaleDateString()}</span>
                      ) : (
                        <span className="badge-status-locked">Locked</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="glass-card text-center" style={{ padding: '4rem' }}>
                <p>No achievements logged in database.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
export default Achievements;
