import React from 'react';
import { MdStar, MdMenuBook, MdAccessTime } from 'react-icons/md';
import './BookCard.css';

export const BookCard = ({ book, progress, onAction, actionText, subText }) => {
  const renderStars = (rating) => {
    const stars = [];
    const floor = Math.floor(rating || 0);
    for (let i = 1; i <= 5; i++) {
      if (i <= floor) {
        stars.push(<MdStar key={i} className="star-icon active" />);
      } else {
        stars.push(<MdStar key={i} className="star-icon" />);
      }
    }
    return stars;
  };

  const getCoverImage = (url) => {
    return url || 'https://images.unsplash.com/photo-1543002588-bfa74002ed7e?w=400&auto=format&fit=crop&q=60';
  };

  return (
    <div className="book-card glass-card">
      <div className="book-card-cover-container">
        <img 
          src={getCoverImage(book.coverUrl)} 
          alt={book.title} 
          className="book-card-cover" 
          loading="lazy" 
        />
        {book.categories && (
          <span className="book-card-badge">{book.categories.split(',')[0]}</span>
        )}
      </div>

      <div className="book-card-details">
        <h3 className="book-card-title" title={book.title}>{book.title}</h3>
        <p className="book-card-author">by {book.author || 'Unknown Author'}</p>

        {book.averageRating !== undefined && book.averageRating > 0 && (
          <div className="book-card-rating">
            {renderStars(book.averageRating)}
            <span className="rating-num">({book.averageRating.toFixed(1)})</span>
          </div>
        )}

        {progress !== undefined && (
          <div className="book-card-progress-container">
            <div className="progress-meta">
              <span className="progress-pct">{Math.round(progress)}% Complete</span>
              <span className="progress-pages">
                {book.currentProgressPages || 0}/{book.totalPages} Pages
              </span>
            </div>
            <div className="progress-bar-bg">
              <div 
                className="progress-bar-fill" 
                style={{ width: `${Math.min(progress, 100)}%` }}
              ></div>
            </div>
          </div>
        )}

        {subText && (
          <div className="book-card-subtext">
            <MdAccessTime className="subtext-icon" />
            <span>{subText}</span>
          </div>
        )}
      </div>

      <div className="book-card-actions">
        <button onClick={() => onAction(book)} className="btn-primary card-action-btn">
          <MdMenuBook />
          <span>{actionText || 'Read'}</span>
        </button>
      </div>
    </div>
  );
};
export default BookCard;
