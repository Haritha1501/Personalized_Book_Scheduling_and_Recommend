import React from 'react';
import { useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { MdSpeed, MdPalette, MdAccountCircle } from 'react-icons/md';
import './Settings.css';

export const Settings = () => {
  const { user, updateUserProfile } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleRetakeSpeedTest = () => {
    // Clear local user speed to bypass route guard
    updateUserProfile({ readingSpeedWpm: null });
    navigate('/speedtest');
  };

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Settings" />

        <div className="settings-page-content fade-in">
          {/* Theme Settings */}
          <div className="settings-section-card glass-card">
            <div className="section-title-row">
              <MdPalette className="section-icon" />
              <h3>Visual Theme Configuration</h3>
            </div>
            <p className="section-desc">Toggle between Light and Dark application layouts.</p>
            
            <div className="theme-toggle-row">
              <span>Active Theme: <strong>{theme.toUpperCase()} MODE</strong></span>
              <button onClick={toggleTheme} className="btn-primary">
                Toggle Layout Theme
              </button>
            </div>
          </div>

          {/* Account Settings */}
          <div className="settings-section-card glass-card">
            <div className="section-title-row">
              <MdAccountCircle className="section-icon" />
              <h3>Account Profile Details</h3>
            </div>
            <p className="section-desc">Manage your profile credentials and reading speed stats.</p>
            
            <div className="account-details-grid">
              <div className="detail-item">
                <span className="lbl">Username:</span>
                <span className="val">{user?.username}</span>
              </div>
              <div className="detail-item">
                <span className="lbl">Email:</span>
                <span className="val">{user?.email}</span>
              </div>
            </div>
          </div>

          {/* Speed test retake */}
          <div className="settings-section-card glass-card">
            <div className="section-title-row">
              <MdSpeed className="section-icon" />
              <h3>Outread Reading Speed Diagnostics</h3>
            </div>
            <p className="section-desc">
              Your current speed is calibrated at <strong>{Math.round(user?.readingSpeedWpm || 250)} WPM</strong>. 
              Retaking the speed test will recalibrate estimated reading times.
            </p>
            
            <div className="speed-retest-row">
              <button onClick={handleRetakeSpeedTest} className="btn-primary retest-btn">
                Retake Reading Speed Test
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Settings;
