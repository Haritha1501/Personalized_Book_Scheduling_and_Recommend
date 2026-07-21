import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { 
  MdNotifications, 
  MdLightMode, 
  MdDarkMode, 
  MdLocalFireDepartment, 
  MdMenu, 
  MdClose, 
  MdStar 
} from 'react-icons/md';
import { NavLink } from 'react-router-dom';
import axios from 'axios';
import './Navbar.css';

export const Navbar = ({ onMobileToggle, title }) => {
  const { user, updateUserProfile } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState([]);

  const fetchNotifications = async () => {
    try {
      const res = await axios.get('/api/dashboard');
      setNotifications(res.data.notifications || []);
    } catch (e) {
      console.error("Failed to load notifications", e);
    }
  };

  const handleNotificationClick = () => {
    setShowNotifications(!showNotifications);
    if (!showNotifications) {
      fetchNotifications();
    }
  };

  const markAsRead = async (id) => {
    try {
      await axios.post(`/api/dashboard/notifications/${id}/read`);
      setNotifications(prev => prev.filter(n => n.id !== id));
      // Optionally decrement user notification state if tracked
    } catch (e) {
      console.error(e);
    }
  };

  const getInitials = (name) => {
    if (!name) return '?';
    return name.slice(0, 2).toUpperCase();
  };

  return (
    <header className="navbar-header">
      <div className="navbar-left">
        <button onClick={onMobileToggle} className="mobile-toggle-btn">
          <MdMenu />
        </button>
        <h2 className="navbar-title">{title || 'Dashboard'}</h2>
      </div>

      <div className="navbar-right">
        {/* Streak indicator */}
        {user && user.currentStreak > 0 && (
          <div className="navbar-streak" title="Daily streak maintained!">
            <MdLocalFireDepartment className="streak-icon" />
            <span className="streak-count">{user.currentStreak} Days</span>
          </div>
        )}

        {/* Level indicator */}
        {user && (
          <div className="navbar-xp" title={`Total XP: ${user.xp}`}>
            <MdStar className="xp-icon" />
            <span className="xp-text">Lv.{user.level}</span>
          </div>
        )}

        {/* Theme Toggle */}
        <button onClick={toggleTheme} className="theme-toggle-btn" title="Toggle theme">
          {theme === 'dark' ? <MdLightMode /> : <MdDarkMode />}
        </button>

        {/* Notifications Icon */}
        <div className="navbar-notifications-container">
          <button onClick={handleNotificationClick} className="nav-btn" title="Notifications">
            <MdNotifications />
            {notifications.length > 0 && <span className="notification-badge">{notifications.length}</span>}
          </button>

          {showNotifications && (
            <div className="notifications-dropdown glass-card">
              <div className="dropdown-header">
                <h3>Unread Alerts</h3>
                <button onClick={() => setShowNotifications(false)} className="close-dropdown-btn">
                  <MdClose />
                </button>
              </div>
              <div className="dropdown-body">
                {notifications.length === 0 ? (
                  <p className="no-notifications">No new notifications</p>
                ) : (
                  notifications.map(n => (
                    <div key={n.id} className="notification-item">
                      <div className="notification-meta">
                        <span className={`notification-tag type-${n.type.toLowerCase()}`}>{n.type}</span>
                        <span className="notification-time">{n.createdAt}</span>
                      </div>
                      <h4 className="notification-title">{n.title}</h4>
                      <p className="notification-msg">{n.message}</p>
                      <button onClick={() => markAsRead(n.id)} className="mark-read-btn">
                        Dismiss
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* User profile bubble */}
        {user && (
          <NavLink to="/profile" className="navbar-profile-bubble" title="My Profile">
            {getInitials(user.username)}
          </NavLink>
        )}
      </div>
    </header>
  );
};
export default Navbar;
