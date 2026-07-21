import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { MdPerson, MdEmail, MdLock, MdOutlineArrowBack } from 'react-icons/md';
import './Auth.css';

export const Signup = () => {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!username || !email || !password || !confirmPassword) {
      setError('Please fill in all fields.');
      return;
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters.');
      return;
    }
    setError('');
    setSuccess('');
    setSubmitting(true);
    try {
      await signup(username, email, password);
      setSuccess('Account created successfully! Redirecting to login...');
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create account. Username or email may be taken.');
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
            <h2>Start Your Quest</h2>
            <p>Track, schedule, and gamify your reading</p>
          </div>

          {error && <div className="auth-error-alert">{error}</div>}
          {success && <div className="auth-success-alert">{success}</div>}

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="auth-form-group">
              <label htmlFor="username">Username</label>
              <div className="input-with-icon">
                <MdPerson className="input-icon" />
                <input 
                  type="text" 
                  id="username" 
                  className="form-input" 
                  placeholder="Choose username" 
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>
            </div>

            <div className="auth-form-group">
              <label htmlFor="email">Email Address</label>
              <div className="input-with-icon">
                <MdEmail className="input-icon" />
                <input 
                  type="email" 
                  id="email" 
                  className="form-input" 
                  placeholder="Enter email" 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
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
                  placeholder="Min 6 characters" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>
            </div>

            <div className="auth-form-group">
              <label htmlFor="confirmPassword">Confirm Password</label>
              <div className="input-with-icon">
                <MdLock className="input-icon" />
                <input 
                  type="password" 
                  id="confirmPassword" 
                  className="form-input" 
                  placeholder="Re-enter password" 
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  disabled={submitting}
                  required
                />
              </div>
            </div>

            <button type="submit" className="btn-primary auth-submit-btn" disabled={submitting}>
              {submitting ? 'Creating Account...' : 'Sign Up'}
            </button>
          </form>

          <div className="auth-card-footer">
            <p>Already have an account? <Link to="/login">Log In</Link></p>
          </div>
        </div>
      </div>
    </div>
  );
};
export default Signup;
