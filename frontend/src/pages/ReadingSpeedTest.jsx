import React, { useState, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { MdSpeed, MdCheckCircle, MdAssignmentTurnedIn, MdTimer } from 'react-icons/md';
import './ReadingSpeedTest.css';

const PASSAGE = `Alice was beginning to get very tired of sitting by her sister on the bank, and of having nothing to do: once or twice she had peeped into the book her sister was reading, but it had no pictures or conversations in it, 'and what is the use of a book,' thought Alice 'without pictures or conversations?' So she was considering in her own mind (as well as she could, for the hot day made her feel very sleepy and stupid), whether the pleasure of making a daisy-chain would be worth the trouble of getting up and picking the daisies, when suddenly a White Rabbit with pink eyes ran close by her. There was nothing so very remarkable in that; nor did Alice think it so very much out of the way to hear the Rabbit say to itself, 'Oh dear! Oh dear! I shall be late!' (when she thought it over afterwards, it occurred to her that she ought to have wondered at this, but at the time it all seemed quite natural); but when the Rabbit actually took a watch out of its waistcoat-pocket, and looked at it, and then hurried on, Alice started to her feet, for it flashed across her mind that she had never before seen a rabbit with either a waistcoat-pocket, or a watch to take out of it, and burning with curiosity, she ran across the field after it, and fortunately was just in time to see it pop down a large rabbit-hole under the hedge.`;

const WORD_COUNT = 240;

const QUESTIONS = [
  {
    id: 1,
    question: "Why did Alice peep into her sister's book?",
    options: [
      "To find out the ending",
      "She wanted to steal it",
      "To see if it had pictures or conversations",
      "Her sister told her to read it"
    ],
    correctAnswer: 2
  },
  {
    id: 2,
    question: "What color were the White Rabbit's eyes?",
    options: [
      "Blue",
      "Pink",
      "Red",
      "Brown"
    ],
    correctAnswer: 1
  },
  {
    id: 3,
    question: "Where did the White Rabbit take his watch from?",
    options: [
      "His backpack",
      "A tree hole",
      "His waistcoat-pocket",
      "His hat"
    ],
    correctAnswer: 2
  }
];

export const ReadingSpeedTest = () => {
  const { user, updateUserProfile } = useAuth();
  const navigate = useNavigate();
  
  const [phase, setPhase] = useState('intro'); // intro, reading, questions, results
  const [startTime, setStartTime] = useState(0);
  const [elapsedTime, setElapsedTime] = useState(0);
  const [answers, setAnswers] = useState({});
  const [wpm, setWpm] = useState(0);
  const [accuracy, setAccuracy] = useState(0);
  const [readingType, setReadingType] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const timerRef = useRef(null);

  const startTest = () => {
    setPhase('reading');
    setStartTime(Date.now());
  };

  const finishReading = () => {
    const elapsed = (Date.now() - startTime) / 1000; // in seconds
    setElapsedTime(elapsed);
    setPhase('questions');
  };

  const handleSelectOption = (qIdx, optIdx) => {
    setAnswers(prev => ({
      ...prev,
      [qIdx]: optIdx
    }));
  };

  const submitAnswers = async () => {
    if (Object.keys(answers).length < QUESTIONS.length) {
      alert("Please answer all questions first.");
      return;
    }

    let correctCount = 0;
    QUESTIONS.forEach((q, idx) => {
      if (answers[idx] === q.correctAnswer) {
        correctCount++;
      }
    });

    const calculatedAccuracy = (correctCount / QUESTIONS.length) * 100;
    const calculatedWpm = (WORD_COUNT / elapsedTime) * 60;

    let type = 'Average Reader';
    if (calculatedWpm < 110) type = 'Very Slow Reader';
    else if (calculatedWpm < 200) type = 'Slow Reader';
    else if (calculatedWpm < 300) type = 'Average Reader';
    else if (calculatedWpm < 450) type = 'Fast Reader';
    else type = 'Speed Reader';

    setWpm(calculatedWpm);
    setAccuracy(calculatedAccuracy);
    setReadingType(type);
    setPhase('results');

    setSubmitting(true);
    try {
      const res = await axios.post('/api/profile/speedtest', {
        wpm: Math.round(calculatedWpm),
        accuracy: Math.round(calculatedAccuracy),
        readingType: type
      });
      // Update global context user details
      updateUserProfile(res.data);
    } catch (e) {
      console.error("Failed to submit speed test", e);
    } finally {
      setSubmitting(false);
    }
  };

  const handleFinish = () => {
    navigate('/dashboard');
  };

  return (
    <div className="speed-test-page">
      <div className="speed-test-container glass-card fade-in">
        {phase === 'intro' && (
          <div className="speed-test-intro text-center">
            <div className="intro-icon"><MdSpeed /></div>
            <h2>Assess Your Reading Speed</h2>
            <p>
              We'll display a short passage. Read at your normal pace. 
              Once you finish reading, you'll answer 3 simple comprehension questions to determine your accuracy.
            </p>
            <button onClick={startTest} className="btn-primary start-btn">
              Start Speed Test
            </button>
          </div>
        )}

        {phase === 'reading' && (
          <div className="speed-test-reading">
            <div className="reading-header">
              <h3>Read the passage below</h3>
              <div className="reading-timer">
                <MdTimer />
                <span>Timer Running...</span>
              </div>
            </div>
            <div className="reading-passage-box">
              <p className="reading-passage">{PASSAGE}</p>
            </div>
            <button onClick={finishReading} className="btn-primary finish-btn">
              Finish Reading
            </button>
          </div>
        )}

        {phase === 'questions' && (
          <div className="speed-test-questions">
            <h2>Comprehension Check</h2>
            <p className="questions-intro">Answer these questions based on the passage you just read.</p>
            
            <div className="questions-list">
              {QUESTIONS.map((q, idx) => (
                <div key={q.id} className="question-item glass-card">
                  <h4>{q.question}</h4>
                  <div className="options-grid">
                    {q.options.map((opt, optIdx) => (
                      <button
                        key={optIdx}
                        onClick={() => handleSelectOption(idx, optIdx)}
                        className={`option-btn ${answers[idx] === optIdx ? 'selected' : ''}`}
                      >
                        {opt}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>

            <button onClick={submitAnswers} className="btn-primary submit-btn">
              <MdAssignmentTurnedIn />
              Submit Answers
            </button>
          </div>
        )}

        {phase === 'results' && (
          <div className="speed-test-results text-center">
            <div className="results-icon"><MdCheckCircle /></div>
            <h2>Your Reading Diagnostics</h2>
            
            <div className="results-metrics">
              <div className="metric-box">
                <span className="metric-val">{Math.round(wpm)}</span>
                <span className="metric-lbl">WPM Speed</span>
              </div>
              <div className="metric-box">
                <span className="metric-val">{Math.round(accuracy)}%</span>
                <span className="metric-lbl">Comprehension</span>
              </div>
            </div>

            <div className="results-type-badge">
              <span>Reader Type: </span>
              <strong>{readingType}</strong>
            </div>

            <p className="results-desc">
              Your results have been permanently logged. Your reading schedulers will calculate target days and hours 
              using your personalized {Math.round(wpm)} words per minute speed.
            </p>

            <button onClick={handleFinish} className="btn-primary continue-btn" disabled={submitting}>
              {submitting ? 'Logging results...' : 'Enter Dashboard'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
export default ReadingSpeedTest;
