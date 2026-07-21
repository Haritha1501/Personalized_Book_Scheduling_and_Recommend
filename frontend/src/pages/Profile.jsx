import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { MdPerson, MdEmail, MdSpeed, MdBookmark, MdTimelapse } from 'react-icons/md';
import './Profile.css';

export const Profile = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchProfile = async () => {
    try {
      const res = await axios.get('/api/profile');
      setProfile(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="My Profile" />
          <div className="skeleton-container">
            <div className="skeleton" style={{ height: '320px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="User Profile" />

        <div className="profile-page-content fade-in">
          {/* Top User Overview Header Card */}
          <div className="profile-header-card glass-card">
            <div className="profile-avatar-bubble">
              {profile?.username?.slice(0, 2).toUpperCase()}
            </div>
            
            <div className="profile-user-summary">
              <h2>{profile?.username}</h2>
              <p className="email-meta">
                <MdEmail className="email-icon" />
                <span>{profile?.email}</span>
              </p>
              
              <div className="profile-badge-tier">
                <span className="badge-level">Level {profile?.level}</span>
                <span className="badge-name">{profile?.levelName}</span>
              </div>
            </div>

            <div className="profile-classification-bubble">
              <span className="bubble-label">Reader Classification</span>
              <span className="bubble-val">{profile?.readerClassification}</span>
            </div>
          </div>

          {/* Speed & Statistics overview cards */}
          <div className="profile-diagnostics-grid">
            <div className="diagnostic-box glass-card">
              <div className="diag-icon-wrapper"><MdSpeed /></div>
              <div className="diag-text">
                <h3>{Math.round(profile?.readingSpeedWpm || 250)} WPM</h3>
                <p>Reading Speed ({profile?.readingType || 'Average Reader'})</p>
              </div>
            </div>

            <div className="diagnostic-box glass-card">
              <div className="diag-icon-wrapper"><MdBookmark /></div>
              <div className="diag-text">
                <h3>{profile?.statistics?.totalPagesRead}</h3>
                <p>Total Pages Read</p>
              </div>
            </div>

            <div className="diagnostic-box glass-card">
              <div className="diag-icon-wrapper"><MdTimelapse /></div>
              <div className="diag-text">
                <h3>{Math.round(profile?.statistics?.totalHoursRead || 0)} Hrs</h3>
                <p>Total Hours Read</p>
              </div>
            </div>
          </div>

          {/* Historical Logs Table */}
          <div className="profile-history-card glass-card">
            <h3>Recent Reading Logs</h3>
            <p className="history-subtitle">Your last 10 reading session logs</p>

            <div className="logs-table-container">
              {profile?.recentSessions && profile.recentSessions.length > 0 ? (
                <table className="logs-table">
                  <thead>
                    <tr>
                      <th>Book</th>
                      <th>Pages</th>
                      <th>Duration</th>
                      <th>Reading Speed</th>
                      <th>Logged Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {profile.recentSessions.map(session => (
                      <tr key={session.id}>
                        <td className="table-book-title-cell">
                          <img src={session.coverUrl} alt="" className="table-book-thumb" />
                          <span>{session.bookTitle}</span>
                        </td>
                        <td>{session.pagesRead} Pages</td>
                        <td>{Math.round(session.durationMinutes)} Min</td>
                        <td>{Math.round(session.speedWpm)} WPM</td>
                        <td>{session.date}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              ) : (
                <p className="no-logs">No reading logs found. Start a reading timer to log progress!</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Profile;
