import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoutes = ({ adminOnly = false }) => {
  const { user, token } = useAuth();

  if (!token || !user) {
    // User not authenticated
    return <Navigate to="/login" replace />;
  }

  // Redirect to reading speed test if they haven't completed it yet
  const needsSpeedTest = user.readingSpeedWpm === null || user.readingSpeedWpm === undefined || user.readingSpeedWpm === 0;
  
  // Prevent infinite loop if they are already heading to the speedtest page
  const currentPath = window.location.pathname;
  
  if (needsSpeedTest && currentPath !== '/speedtest') {
    return <Navigate to="/speedtest" replace />;
  }

  if (!needsSpeedTest && currentPath === '/speedtest') {
    // Already did speed test, send them to dashboard
    return <Navigate to="/dashboard" replace />;
  }

  if (adminOnly && !user.roles?.includes('ROLE_ADMIN')) {
    // Unauthorized access
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoutes;
