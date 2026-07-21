import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import { 
  MdPlayArrow, 
  MdStop, 
  MdFormatSize, 
  MdFormatLineSpacing, 
  MdBookmark, 
  MdNavigateBefore, 
  MdNavigateNext 
} from 'react-icons/md';
import './ActiveReading.css';

export const ActiveReading = () => {
  const { planId } = useParams();
  const navigate = useNavigate();

  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(true);
  
  // Font / spacing configs
  const [fontSize, setFontSize] = useState(16);
  const [lineHeight, setLineHeight] = useState(1.6);
  
  // Stopwatch session variables
  const [sessionActive, setSessionActive] = useState(false);
  const [session, setSession] = useState(null);
  const [timerSeconds, setTimerSeconds] = useState(0);
  
  // Modal configs
  const [showEndModal, setShowEndModal] = useState(false);
  const [endPage, setEndPage] = useState(0);
  const [savingProgress, setSavingProgress] = useState(false);
  const [error, setError] = useState('');
  
  // Celebration popups
  const [celebrationMsg, setCelebrationMsg] = useState('');

  const intervalRef = useRef(null);

  const fetchPlanDetails = async () => {
    try {
      const res = await axios.get(`/api/reading/plans/${planId}`);
      setPlan(res.data);
      setEndPage(res.data.currentProgressPages);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPlanDetails();
  }, [planId]);

  // Handle Stopwatch ticking
  useEffect(() => {
    if (sessionActive) {
      intervalRef.current = setInterval(() => {
        setTimerSeconds(prev => prev + 1);
      }, 1000);
    } else {
      clearInterval(intervalRef.current);
    }

    return () => clearInterval(intervalRef.current);
  }, [sessionActive]);

  const handleStartSession = async () => {
    try {
      const res = await axios.post('/api/reading/sessions/start', {
        readingPlanId: plan.id,
        startPage: plan.currentProgressPages
      });
      setSession(res.data);
      setTimerSeconds(0);
      setSessionActive(true);
    } catch (e) {
      console.error("Failed to start reading session", e);
    }
  };

  const handleStopSession = () => {
    setSessionActive(false);
    setEndPage(plan.currentProgressPages);
    setShowEndModal(true);
  };

  const handleSaveProgress = async () => {
    if (endPage < plan.currentProgressPages) {
      setError(`Ending page cannot be less than your starting page (${plan.currentProgressPages}).`);
      return;
    }
    if (endPage > plan.book.totalPages) {
      setError(`Ending page cannot exceed book's total pages (${plan.book.totalPages}).`);
      return;
    }

    setError('');
    setSavingProgress(true);
    try {
      const res = await axios.post('/api/reading/sessions/end', {
        readingSessionId: session.id,
        endPage: parseInt(endPage)
      });
      
      const pagesRead = parseInt(endPage) - plan.currentProgressPages;
      const xpGained = pagesRead * 2;
      setCelebrationMsg(`Session Logged! You read ${pagesRead} pages and earned ${xpGained} XP!`);
      
      setTimeout(() => {
        setCelebrationMsg('');
        setShowEndModal(false);
        navigate('/dashboard');
      }, 3000);
    } catch (e) {
      setError(e.response?.data?.message || "Failed to log progress.");
      setSavingProgress(false);
    }
  };

  const formatTimer = (totalSecs) => {
    const mins = Math.floor(totalSecs / 60);
    const secs = totalSecs % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  if (loading) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Active Reading" />
          <div className="skeleton-container">
            <div className="skeleton" style={{ height: '400px' }}></div>
          </div>
        </div>
      </div>
    );
  }

  if (!plan) {
    return (
      <div className="app-container">
        <Sidebar />
        <div className="main-content">
          <Navbar title="Active Reading" />
          <div className="glass-card text-center" style={{ padding: '4rem' }}>
            <h3>Reading Plan not found</h3>
            <button onClick={() => navigate('/dashboard')} className="btn-primary">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title={`Reading: ${plan.book.title}`} />

        <div className="active-reading-content fade-in">
          {/* Top reader controls */}
          <div className="reader-controls-bar glass-card">
            <div className="control-group">
              <MdFormatSize className="control-icon" />
              <label htmlFor="fontSizeSlider">Font Size:</label>
              <input 
                type="range" 
                id="fontSizeSlider" 
                min="12" 
                max="28" 
                value={fontSize} 
                onChange={(e) => setFontSize(parseInt(e.target.value))} 
                className="slider-input-mini"
              />
              <span className="control-val">{fontSize}px</span>
            </div>

            <div className="control-group">
              <MdFormatLineSpacing className="control-icon" />
              <label htmlFor="lineHeightSlider">Line Spacing:</label>
              <input 
                type="range" 
                id="lineHeightSlider" 
                min="1.2" 
                max="2.4" 
                step="0.1" 
                value={lineHeight} 
                onChange={(e) => setLineHeight(parseFloat(e.target.value))} 
                className="slider-input-mini"
              />
              <span className="control-val">{lineHeight}</span>
            </div>

            {/* Stopwatch layout */}
            <div className="session-timer-pills">
              {sessionActive ? (
                <div className="active-timer-box">
                  <div className="pulse-dot"></div>
                  <span className="timer-val">{formatTimer(timerSeconds)}</span>
                  <button onClick={handleStopSession} className="stop-session-btn btn-danger">
                    <MdStop />
                    Stop Session
                  </button>
                </div>
              ) : (
                <button onClick={handleStartSession} className="start-session-btn btn-primary">
                  <MdPlayArrow />
                  Start Timer
                </button>
              )}
            </div>
          </div>

          <div className="reader-body-layout">
            {/* Book Info Panel */}
            <div className="reader-book-details glass-card">
              <img src={plan.book.coverUrl} alt={plan.book.title} className="reader-book-cover" />
              <h3>{plan.book.title}</h3>
              <p className="author-name">by {plan.book.author}</p>
              
              <div className="reader-milestone-status">
                <div className="milestone-text">
                  <MdBookmark className="bookmark-icon" />
                  <span>Page {plan.currentProgressPages} of {plan.book.totalPages}</span>
                </div>
                <div className="milestone-bar-bg">
                  <div 
                    className="milestone-bar-fill" 
                    style={{ width: `${(plan.currentProgressPages / plan.book.totalPages) * 100}%` }}
                  ></div>
                </div>
              </div>
            </div>

            {/* Main Immersive Reader Text Panel */}
            <div className="reader-text-panel glass-card">
              <div 
                className="reader-text-content"
                style={{ fontSize: `${fontSize}px`, lineHeight: lineHeight }}
              >
                <h2 className="passage-title">Excerpt / Chapter</h2>
                <p>
                  You are now in Focus Mode. Use the sidebar controls to customize the typography to your comfort.
                </p>
                <p>
                  "The book was open, the pages waiting. A reader who is cataloged with goals, XP, and streaks lives 
                  ten times as many journeys. Track your progress daily, set your timer, and never miss a single page."
                </p>
                <p>
                  To log pages read and earn rewards, click the 'Start Timer' button in the toolbar above, read your physical 
                  book, or copy digital text here, and then click 'Stop Session' to report your final page.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Ending Session Logging Modal */}
        {showEndModal && (
          <div className="end-session-modal-overlay">
            <div className="end-session-modal glass-card fade-in">
              <h2>Save Reading Progress</h2>
              
              {celebrationMsg ? (
                <div className="congrats-celebration-alert">
                  <span className="celebration-emoji">🎉</span>
                  <p>{celebrationMsg}</p>
                </div>
              ) : (
                <>
                  <p className="modal-description">
                    Excellent job! You read for {formatTimer(timerSeconds)}. Enter the page you finished reading on.
                  </p>

                  {error && <div className="modal-error-alert">{error}</div>}

                  <div className="modal-form-group">
                    <label htmlFor="endPageInput">Finished at Page:</label>
                    <input 
                      type="number" 
                      id="endPageInput" 
                      className="form-input end-page-input"
                      value={endPage}
                      onChange={(e) => setEndPage(e.target.value)}
                      min={plan.currentProgressPages}
                      max={plan.book.totalPages}
                      disabled={savingProgress}
                    />
                    <span className="input-max-hint">Max Pages: {plan.book.totalPages} (Currently: {plan.currentProgressPages})</span>
                  </div>

                  <div className="modal-actions-row">
                    <button 
                      onClick={() => setShowEndModal(false)} 
                      className="btn-secondary"
                      disabled={savingProgress}
                    >
                      Cancel
                    </button>
                    <button 
                      onClick={handleSaveProgress} 
                      className="btn-primary"
                      disabled={savingProgress}
                    >
                      {savingProgress ? 'Saving...' : 'Save & Log XP'}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
export default ActiveReading;
