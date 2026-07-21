import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import ProtectedRoutes from './components/ProtectedRoutes';

// Pages
import Landing from './pages/Landing';
import Login from './pages/Login';
import Signup from './pages/Signup';
import ReadingSpeedTest from './pages/ReadingSpeedTest';
import Dashboard from './pages/Dashboard';
import SearchBooks from './pages/SearchBooks';
import BookDetails from './pages/BookDetails';
import ActiveReading from './pages/ActiveReading';
import Recommendations from './pages/Recommendations';
import Achievements from './pages/Achievements';
import Analytics from './pages/Analytics';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import AdminDashboard from './pages/AdminDashboard';
import NotFound from './pages/NotFound';

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            {/* Guarded User Routes */}
            <Route element={<ProtectedRoutes />}>
              <Route path="/speedtest" element={<ReadingSpeedTest />} />
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/search" element={<SearchBooks />} />
              <Route path="/books/:googleBookId" element={<BookDetails />} />
              <Route path="/reading/:planId" element={<ActiveReading />} />
              <Route path="/recommendations" element={<Recommendations />} />
              <Route path="/achievements" element={<Achievements />} />
              <Route path="/statistics" element={<Analytics />} />
              <Route path="/profile" element={<Profile />} />
              <Route path="/settings" element={<Settings />} />
            </Route>

            {/* Guarded Admin Routes */}
            <Route element={<ProtectedRoutes adminOnly={true} />}>
              <Route path="/admin" element={<AdminDashboard />} />
            </Route>

            {/* Catch All - 404 */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
