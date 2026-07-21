import React from 'react';
import './ReadingHeatmap.css';

export const ReadingHeatmap = ({ data }) => {
  const getLevelClass = (pages) => {
    if (pages === 0) return 'heatmap-level-0';
    if (pages <= 5) return 'heatmap-level-1';
    if (pages <= 15) return 'heatmap-level-2';
    if (pages <= 30) return 'heatmap-level-3';
    return 'heatmap-level-4';
  };

  const formatDate = (dateStr) => {
    try {
      const options = { month: 'short', day: 'numeric', year: 'numeric' };
      return new Date(dateStr).toLocaleDateString(undefined, options);
    } catch (e) {
      return dateStr;
    }
  };

  return (
    <div className="reading-heatmap glass-card">
      <div className="heatmap-header">
        <h3>Reading Heatmap</h3>
        <span className="heatmap-subtitle">Daily activity over the last 30 days</span>
      </div>

      <div className="heatmap-grid-container">
        <div className="heatmap-grid">
          {data && data.map((day, idx) => (
            <div 
              key={day.date || idx} 
              className={`heatmap-cell ${getLevelClass(day.pagesRead)}`}
              title={`${day.pagesRead} pages read on ${formatDate(day.date)}`}
            ></div>
          ))}
        </div>
      </div>

      <div className="heatmap-legend">
        <span>Less</span>
        <div className="legend-cells">
          <div className="legend-cell heatmap-level-0"></div>
          <div className="legend-cell heatmap-level-1"></div>
          <div className="legend-cell heatmap-level-2"></div>
          <div className="legend-cell heatmap-level-3"></div>
          <div className="legend-cell heatmap-level-4"></div>
        </div>
        <span>More</span>
      </div>
    </div>
  );
};
export default ReadingHeatmap;
