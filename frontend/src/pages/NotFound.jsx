import React from 'react';
import { Link } from 'react-router-dom';
import { MdErrorOutline } from 'react-icons/md';

export const NotFound = () => {
  return (
    <div className="auth-page text-center">
      <div className="glass-card" style={{ padding: '4rem 2rem', maxWidth: '440px', width: '100%' }}>
        <div style={{ fontSize: '4rem', color: 'var(--accent)', marginBottom: '1rem' }}>
          <MdErrorOutline />
        </div>
        <h1 style={{ fontSize: '2.5rem', marginBottom: '0.5rem', letterSpacing: '-0.03em' }}>404</h1>
        <h2 style={{ fontSize: '1.25rem', color: 'var(--text-primary)', marginBottom: '1rem' }}>Page Not Found</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '2rem', lineHeight: '1.6' }}>
          The path you are trying to access does not exist or has been moved. Check the URL or return to dashboard.
        </p>
        <Link to="/dashboard" className="btn-primary" style={{ display: 'inline-flex', padding: '0.75rem 2rem' }}>
          Enter Dashboard
        </Link>
      </div>
    </div>
  );
};
export default NotFound;
