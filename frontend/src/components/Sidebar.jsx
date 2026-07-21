import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  MdDashboard, 
  MdSearch, 
  MdAutoAwesome, 
  MdEmojiEvents, 
  MdBarChart, 
  MdPerson, 
  MdSettings, 
  MdAdminPanelSettings, 
  MdExitToApp 
} from 'react-icons/md';
import './Sidebar.css';

export const Sidebar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <span className="logo-icon">📖</span>
        <span className="logo-text">ReadQuest</span>
      </div>

      <nav className="sidebar-nav">
        <NavLink to="/dashboard" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdDashboard className="nav-icon" />
          <span className="nav-text">Dashboard</span>
        </NavLink>

        <NavLink to="/search" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdSearch className="nav-icon" />
          <span className="nav-text">Search Books</span>
        </NavLink>

        <NavLink to="/recommendations" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdAutoAwesome className="nav-icon" />
          <span className="nav-text">Recommendations</span>
        </NavLink>

        <NavLink to="/achievements" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdEmojiEvents className="nav-icon" />
          <span className="nav-text">Achievements</span>
        </NavLink>

        <NavLink to="/statistics" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdBarChart className="nav-icon" />
          <span className="nav-text">Analytics</span>
        </NavLink>

        <NavLink to="/profile" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdPerson className="nav-icon" />
          <span className="nav-text">My Profile</span>
        </NavLink>

        <NavLink to="/settings" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
          <MdSettings className="nav-icon" />
          <span className="nav-text">Settings</span>
        </NavLink>

        {isAdmin && (
          <NavLink to="/admin" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''} admin-link`}>
            <MdAdminPanelSettings className="nav-icon" />
            <span className="nav-text">Admin Panel</span>
          </NavLink>
        )}
      </nav>

      <div className="sidebar-footer">
        <button onClick={handleLogout} className="logout-btn">
          <MdExitToApp className="nav-icon" />
          <span className="nav-text">Logout</span>
        </button>
      </div>
    </aside>
  );
};
export default Sidebar;
