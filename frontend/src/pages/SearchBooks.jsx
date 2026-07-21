import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Sidebar from '../components/Sidebar';
import Navbar from '../components/Navbar';
import BookCard from '../components/BookCard';
import { MdSearch, MdOutlineBook } from 'react-icons/md';
import './SearchBooks.css';

export const SearchBooks = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setSearched(true);
    try {
      const res = await axios.get(`/api/books/search?query=${encodeURIComponent(query)}`);
      setResults(res.data || []);
    } catch (e) {
      console.error("Failed book search query", e);
    } finally {
      setLoading(false);
    }
  };

  const handleSelectBook = (book) => {
    navigate(`/books/${book.googleBookId}`);
  };

  return (
    <div className="app-container">
      <Sidebar />
      <div className="main-content">
        <Navbar title="Search Catalog" />
        
        <div className="search-page-content fade-in">
          <div className="search-bar-row glass-card">
            <form onSubmit={handleSearch} className="search-form-row">
              <div className="search-input-wrapper">
                <MdSearch className="search-bar-icon" />
                <input 
                  type="text" 
                  className="form-input search-input" 
                  placeholder="Search by Title, Author, or ISBN..." 
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                />
              </div>
              <button type="submit" className="btn-primary search-submit-btn" disabled={loading}>
                {loading ? 'Searching...' : 'Search'}
              </button>
            </form>
          </div>

          <div className="search-results-section">
            {loading ? (
              <div className="search-loading-skeleton">
                <div className="skeleton skeleton-card"></div>
                <div className="skeleton skeleton-card"></div>
                <div className="skeleton skeleton-card"></div>
              </div>
            ) : results.length > 0 ? (
              <div className="search-results-grid">
                {results.map(book => (
                  <BookCard 
                    key={book.googleBookId} 
                    book={book} 
                    actionText="Select Book"
                    onAction={handleSelectBook} 
                  />
                ))}
              </div>
            ) : searched ? (
              <div className="search-empty-state glass-card text-center">
                <MdOutlineBook className="search-empty-icon" />
                <h3>No books found</h3>
                <p>We couldn't find any books matching your query. Try different keywords.</p>
              </div>
            ) : (
              <div className="search-intro-state glass-card text-center">
                <MdOutlineBook className="search-empty-icon" />
                <h3>Find your next reading quest</h3>
                <p>Use the search bar above to look up millions of books indexed via the Google Books Catalog.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
export default SearchBooks;
