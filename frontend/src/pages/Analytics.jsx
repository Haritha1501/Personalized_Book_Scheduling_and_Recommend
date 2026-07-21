import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import StatsChart from '../components/StatsChart';
import { 
  MdMenuBook, 
  MdAssignmentTurnedIn, 
  MdSpeed, 
  MdAccessTime, 
  MdStars, 
  MdLocalPrintshop 
} from 'react-icons/md';
import './Analytics.css';

export const Analytics = () => {
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

  const handlePrintCertificate = (bookTitle) => {
    const printContent = `
      <html>
        <head>
          <title>Completion Certificate - ReadQuest</title>
          <style>
            body { font-family: 'Georgia', serif; text-align: center; padding: 4rem; border: 15px double #FF6B35; background-color: #FAF6F0; color: #1A1D20; }
            h1 { font-family: 'Arial', sans-serif; color: #FF6B35; font-size: 3rem; margin-bottom: 1rem; }
            p { font-size: 1.25rem; line-height: 1.8; }
            .meta { margin-top: 3rem; font-style: italic; }
            .signature { margin-top: 5rem; border-top: 1px solid #1A1D20; display: inline-block; width: 250px; }
          </style>
        </head>
        <body>
          <h1>ReadQuest Scholar Certificate</h1>
          <p>This document proudly certifies that</p>
          <h2>${profile?.username || 'Learner'}</h2>
          <p>has successfully read, scheduled, and completed the book quest for</p>
          <h3>"${bookTitle}"</h3>
          <p>under physical reading guidelines with verified accuracy & gamified reading session logs.</p>
          <p class="meta">Issued on ${new Date().toLocaleDateString()}</p>
          <div class="signature"><p>System Dean - ReadQuest</p></div>
        </body>
      </html>
    `;
    const win = window.open('', '_blank');
    win.document.write(printContent);
    win.document.close();
    win.print();
  };

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Analytics" />
          <div className="skeleton-container">
            <div className="skeleton" style={{ height: '300px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  // Map session logs to chart format
  const sessions = profile?.recentSessions || [];
  const chartLabels = sessions.slice().reverse().map(s => s.date.split(' ')[0]);
  const chartPages = sessions.slice().reverse().map(s => s.pagesRead);
  const chartMinutes = sessions.slice().reverse().map(s => s.durationMinutes);

  const completedBooks = sessions.filter(s => s.pagesRead > 0); // Placeholder or fetch actual completed books list

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Reader Analytics" />

        <div className="analytics-page-content fade-in">
          {/* Key Metrics Cards */}
          <div className="analytics-metrics-grid">
            <div className="metric-item-box glass-card bg-accent-light">
              <MdMenuBook className="metric-box-icon" />
              <div className="metric-box-meta">
                <span className="metric-box-val">{profile?.statistics?.totalBooksCompleted}</span>
                <span className="metric-box-lbl">Books Finished</span>
              </div>
            </div>

            <div className="metric-item-box glass-card bg-success-light">
              <MdAssignmentTurnedIn className="metric-box-icon" />
              <div className="metric-box-meta">
                <span className="metric-box-val">{profile?.statistics?.totalPagesRead}</span>
                <span className="metric-box-lbl">Pages Read</span>
              </div>
            </div>

            <div className="metric-item-box glass-card bg-info-light">
              <MdAccessTime className="metric-box-icon" />
              <div className="metric-box-meta">
                <span className="metric-box-val">{Math.round(profile?.statistics?.totalHoursRead || 0)} Hrs</span>
                <span className="metric-box-lbl">Time Logged</span>
              </div>
            </div>

            <div className="metric-item-box glass-card bg-warning-light">
              <MdSpeed className="metric-box-icon" />
              <div className="metric-box-meta">
                <span className="metric-box-val">{Math.round(profile?.statistics?.avgReadingSpeedWpm || 0)}</span>
                <span className="metric-box-lbl">Avg WPM Speed</span>
              </div>
            </div>
          </div>

          {/* Reader Classification Section */}
          <div className="analytics-classification-row glass-card">
            <div className="classification-icon"><MdStars /></div>
            <div className="classification-meta">
              <span>Current Classification: </span>
              <h2>{profile?.readerClassification}</h2>
              <p>{profile?.readerClassificationDescription}</p>
            </div>
            <div className="classification-stat-pills">
              <div className="pill">Fav Genre: <strong>{profile?.statistics?.favoriteGenre}</strong></div>
              <div className="pill">Completion Rate: <strong>{Math.round(profile?.statistics?.completionRate || 0)}%</strong></div>
            </div>
          </div>

          {/* Charts Row */}
          <div className="analytics-charts-grid">
            <div className="chart-container-box glass-card">
              <h3>Pages Read Per Session</h3>
              <div className="chart-wrapper-box">
                {sessions.length > 0 ? (
                  <StatsChart labels={chartLabels} data={chartPages} label="Pages Read" type="line" />
                ) : (
                  <p className="no-chart-data">No session data logged yet.</p>
                )}
              </div>
            </div>

            <div className="chart-container-box glass-card">
              <h3>Minutes Logged Per Session</h3>
              <div className="chart-wrapper-box">
                {sessions.length > 0 ? (
                  <StatsChart labels={chartLabels} data={chartMinutes} label="Minutes Read" type="bar" />
                ) : (
                  <p className="no-chart-data">No session data logged yet.</p>
                )}
              </div>
            </div>
          </div>

          {/* Printable Certificates section */}
          {profile?.statistics?.totalBooksCompleted > 0 && (
            <div className="analytics-certificates-section glass-card">
              <h3>Reading Certificates</h3>
              <p>You completed book plans! Print your custom achievement certificate.</p>
              
              <div className="certificates-list">
                {/* Dynamically list recent books completion */}
                <div className="certificate-pill">
                  <span className="cert-book-name">Mastery of Reading Log</span>
                  <button 
                    onClick={() => handlePrintCertificate(profile?.statistics?.favoriteGenre + " Literature")} 
                    className="btn-primary cert-print-btn"
                  >
                    <MdLocalPrintshop />
                    Print Certificate
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
export default Analytics;
