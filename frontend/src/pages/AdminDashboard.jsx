import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { 
  MdSupervisorAccount, 
  MdBook, 
  MdDateRange, 
  MdTimer, 
  MdTrendingUp, 
  MdPieChart 
} from 'react-icons/md';
import './AdminDashboard.css';

export const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchAdminStats = async () => {
    try {
      const res = await axios.get('/api/admin/statistics');
      setStats(res.data);
    } catch (e) {
      console.error("Failed to load admin stats", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAdminStats();
  }, []);

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Admin Diagnostics" />
          <div className="skeleton-container">
            <div className="skeleton" style={{ height: '350px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Admin Dashboard" />

        <div className="admin-page-content fade-in">
          {/* Numeric Aggregations */}
          <div className="admin-aggregations-grid">
            <div className="aggregation-box glass-card border-left-success">
              <MdSupervisorAccount className="agg-icon font-success-color" />
              <div className="agg-meta">
                <span className="agg-val">{stats?.totalUsers}</span>
                <span className="agg-lbl">Total Registered Users</span>
              </div>
            </div>

            <div className="aggregation-box glass-card border-left-info">
              <MdBook className="agg-icon font-info-color" />
              <div className="agg-meta">
                <span className="agg-val">{stats?.totalBooks}</span>
                <span className="agg-lbl">Books Cached in DB</span>
              </div>
            </div>

            <div className="aggregation-box glass-card border-left-accent">
              <MdDateRange className="agg-icon font-accent-color" />
              <div className="agg-meta">
                <span className="agg-val">{stats?.totalReadingPlans}</span>
                <span className="agg-lbl">Active Reading Plans</span>
              </div>
            </div>

            <div className="aggregation-box glass-card border-left-warning">
              <MdTimer className="agg-icon font-warning-color" />
              <div className="agg-meta">
                <span className="agg-val">{stats?.totalSessions}</span>
                <span className="agg-lbl">Reading Sessions Run</span>
              </div>
            </div>
          </div>

          {/* Users & Books lists grid */}
          <div className="admin-lists-grid">
            {/* Active Users Table */}
            <div className="admin-list-card glass-card">
              <h3>Top Active Readers</h3>
              <p className="list-subtitle">Ranked by overall experience points (XP)</p>

              <div className="table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Level</th>
                      <th>XP</th>
                      <th>Pages Read</th>
                      <th>Streak</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats?.activeReaders?.map((reader, idx) => (
                      <tr key={idx}>
                        <td>
                          <div className="reader-info-cell">
                            <strong>{reader.username}</strong>
                            <span>{reader.email}</span>
                          </div>
                        </td>
                        <td>Lv.{reader.level}</td>
                        <td>{reader.xp}</td>
                        <td>{reader.totalPagesRead}</td>
                        <td>🔥 {reader.currentStreak} Days</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Popular Books Table */}
            <div className="admin-list-card glass-card">
              <h3>Most Popular Books</h3>
              <p className="list-subtitle">Ranked by count of scheduling plans</p>

              <div className="table-wrapper">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Book</th>
                      <th>Author</th>
                      <th>Plans Count</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats?.popularBooks?.map((book, idx) => (
                      <tr key={idx}>
                        <td><strong>{book.title}</strong></td>
                        <td>{book.author}</td>
                        <td>📚 {book.activePlanCount} Plans</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* Heuristics and Trends Row */}
          <div className="admin-trends-grid">
            {/* Trending Genres */}
            <div className="trend-card glass-card">
              <div className="trend-header">
                <MdTrendingUp className="trend-icon" />
                <h3>Trending Genres</h3>
              </div>
              <div className="trend-list">
                {stats?.trendingGenres?.map((trend, idx) => (
                  <div key={idx} className="trend-item">
                    <span>{trend.genre}</span>
                    <span className="trend-count-badge">{trend.count} plans</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Classifications distribution */}
            <div className="trend-card glass-card">
              <div className="trend-header">
                <MdPieChart className="trend-icon" />
                <h3>Reader Classification Mix</h3>
              </div>
              <div className="trend-list">
                {stats?.readerClassifications?.map((c, idx) => (
                  <div key={idx} className="trend-item">
                    <span>{c.name}</span>
                    <span className="trend-count-badge bg-teal">{c.count} users</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default AdminDashboard;
