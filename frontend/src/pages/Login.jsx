import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { MdPerson, MdLock, MdOutlineArrowBack } from 'react-icons/md';
import './Auth.css';

export const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !password) {
      setError('Please fill in all fields.');
      return;
    }
    setError('');
    setSubmitting(true);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid username or password.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <Link to="/" className="back-home-btn">
        <MdOutlineArrowBack />
        <span>Back to Home</span>
      </Link>

      <div className="auth-card-container fade-in">
        <div className="auth-card glass-card">
          <div className="auth-card-header">
            <span className="auth-logo-icon">📖</span>
            <h2>Welcome Back</h2>
            <p>Log in to resume your reading quest</p>
          </div>

          {error && <div className="auth-error-alert">{error}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="auth-form-group">
              <label htmlFor="username">Username</label>
              <div className="input-with-icon">
                <MdPerson className="input-icon" />
                <input 
                  type="text" 
                  id="username" 
                  className="form-input" 
                  placeholder="Enter username" 
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>
            </div>

            <div className="auth-form-group">
              <label htmlFor="password">Password</label>
              <div className="input-with-icon">
                <MdLock className="input-icon" />
                <input 
                  type="password" 
                  id="password" 
                  className="form-input" 
                  placeholder="Enter password" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn-primary auth-submit-btn" disabled={submitting}>
              {submitting ? 'Authenticating...' : 'Log In'}
            </button>
          </form>

          <div className="auth-card-footer">
            <p>New to ReadQuest? <Link to="/signup">Create Account</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Login;
