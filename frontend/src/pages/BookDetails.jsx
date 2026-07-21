import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { useAuth } from '../context/AuthContext';
import { MdStar, MdArrowBack, MdCalendarToday, MdTimer, MdBookmarkBorder } from 'react-icons/md';
import './BookDetails.css';

export const BookDetails = () => {
  const { googleBookId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [targetDays, setTargetDays] = useState(30);
  const [submitting, setSubmitting] = useState(false);

  const fetchBookDetails = async () => {
    try {
      const res = await axios.get(`/api/books/details/${googleBookId}`);
      setBook(res.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookDetails();
  }, [googleBookId]);

  const handleCreatePlan = async () => {
    if (targetDays <= 0) return;
    setSubmitting(true);
    try {
      const payload = {
        googleBookId: book.googleBookId,
        title: book.title,
        author: book.author,
        coverUrl: book.coverUrl,
        description: book.description,
        totalPages: book.totalPages,
        categories: book.categories,
        averageRating: book.averageRating,
        language: book.language,
        isbn: book.isbn,
        publisher: book.publisher,
        publishedDate: book.publishedDate,
        targetDays: parseInt(targetDays)
      };

      await axios.post('/api/reading/plans', payload);
      navigate('/dashboard');
    } catch (e) {
      console.error("Failed to establish reading plan", e);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Book Details" />
          <div className="skeleton-container">
            <div className="skeleton" style={{ height: '350px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Book Details" />
          <div className="glass-card text-center" style={{ padding: '4rem' }}>
            <h3>Book not found</h3>
            <button onClick={() => navigate('/search')} className="btn-primary" style={{ marginTop: '1rem' }}>
              Back to Catalog
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Interactive Live calculations
  const wpm = user?.readingSpeedWpm || 250;
  const pagesPerDay = Math.ceil(book.totalPages / targetDays);
  const minutesPerDay = Math.ceil((pagesPerDay * 250) / wpm);

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Onboarding Quest" />

        <div className="book-details-content fade-in">
          <button onClick={() => navigate(-1)} className="back-btn-row">
            <MdArrowBack />
            <span>Go Back</span>
          </button>

          <div className="details-main-grid">
            {/* Book Cover Panel */}
            <div className="details-cover-panel glass-card">
              <img 
                src={book.coverUrl || 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&auto=format&fit=crop&q=60'} 
                alt={book.title} 
                className="details-cover" 
              />
              <div className="metadata-pills">
                <span className="pill">{book.totalPages} Pages</span>
                {book.language && <span className="pill">Lang: {book.language.toUpperCase()}</span>}
              </div>
            </div>

            {/* Book Metadata & Scheduler Panel */}
            <div className="details-info-panel glass-card">
              <h1 className="details-title">{book.title}</h1>
              <p className="details-author">by <strong>{book.author}</strong></p>
              
              {book.averageRating > 0 && (
                <div className="details-rating">
                  <MdStar className="star-icon active" />
                  <span>{book.averageRating} / 5</span>
                </div>
              )}

              <p className="details-desc">{book.description || 'No description available.'}</p>

              <div className="scheduler-box">
                <h3>Setup Reading Scheduler</h3>
                <p className="scheduler-intro">Decide how fast you want to finish this book.</p>

                <div className="form-group-slider">
                  <div className="slider-header">
                    <label htmlFor="targetDays">Target completion days:</label>
                    <span className="slider-val">{targetDays} Days</span>
                  </div>
                  <input 
                    type="range" 
                    id="targetDays" 
                    min="1" 
                    max="180" 
                    value={targetDays} 
                    onChange={(e) => setTargetDays(parseInt(e.target.value))} 
                    className="slider-input"
                  />
                </div>

                <div className="scheduler-preview-metrics">
                  <div className="preview-metric-box">
                    <MdCalendarToday className="preview-icon" />
                    <div className="preview-meta">
                      <span className="preview-val">{pagesPerDay} Pages</span>
                      <span className="preview-lbl">Target Pages / Day</span>
                    </div>
                  </div>

                  <div className="preview-metric-box">
                    <MdTimer className="preview-icon" />
                    <div className="preview-meta">
                      <span className="preview-val">{minutesPerDay} Min</span>
                      <span className="preview-lbl">Est. Time / Day</span>
                    </div>
                  </div>
                </div>

                <button 
                  onClick={handleCreatePlan} 
                  className="btn-primary start-plan-btn"
                  disabled={submitting}
                >
                  <MdBookmarkBorder />
                  <span>{submitting ? 'Creating scheduler...' : 'Add to My Reading Quest'}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
export default BookDetails;
