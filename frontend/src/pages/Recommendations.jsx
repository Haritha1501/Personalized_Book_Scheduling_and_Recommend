import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import BookCard from '../components/BookCard';
import { MdAutoAwesome } from 'react-icons/md';
import './Recommendations.css';

export const Recommendations = () => {
  const navigate = useNavigate();
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchRecommendations = async () => {
    try {
      const res = await axios.get('/api/books/recommendations');
      setBooks(res.data || []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRecommendations();
  }, []);

  const handleSelectBook = (book) => {
    // Navigate to search catalog pre-filled with this title
    navigate(`/search?q=${encodeURIComponent(book.title)}`);
  };

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Personal Suggestions" />

        <div className="recs-page-content fade-in">
          <div className="recs-page-hero glass-card">
            <div className="recs-hero-icon"><MdAutoAwesome /></div>
            <h2>Smart Book Recommendations</h2>
            <p>
              Our recommendation engine matches your favorite subjects, genres, and reading speed to suggest classic 
              literature, highly rated books, and similar authors.
            </p>
          </div>

          <div className="recs-results-container">
            {loading ? (
              <div className="recs-skeleton-grid">
                <div className="skeleton skeleton-card"></div>
                <div className="skeleton skeleton-card"></div>
                <div className="skeleton skeleton-card"></div>
              </div>
            ) : books.length > 0 ? (
              <div className="recs-grid">
                {books.map((book, idx) => (
                  <BookCard 
                    key={book.googleBookId || idx} 
                    book={book} 
                    actionText="Schedule Book" 
                    onAction={handleSelectBook}
                  />
                ))}
              </div>
            ) : (
              <div className="glass-card text-center" style={{ padding: '4rem' }}>
                <p>No recommendations available. Start logging reading sessions to unlock recommendations!</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
export default Recommendations;
