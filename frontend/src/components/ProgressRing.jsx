import React from 'react';
import './ProgressRing.css';

export const ProgressRing = ({ radius = 60, stroke = 8, progress = 0, text }) => {
  const normalizedRadius = radius - stroke * 2;
  const circumference = normalizedRadius * 2 * Math.PI;
  // Cap progress between 0 and 100
  const cleanProgress = Math.max(0, Math.min(progress, 100));
  const strokeDashoffset = circumference - (cleanProgress / 100) * circumference;

  return (
    <div className="progress-ring-wrapper" style={{ width: radius * 2, height: radius * 2 }}>
      <svg
        height={radius * 2}
        width={radius * 2}
        className="progress-ring-svg"
      >
        {/* Background track circle */}
        <circle
          stroke="var(--bg-tertiary)"
          fill="transparent"
          strokeWidth={stroke}
          r={normalizedRadius}
          cx={radius}
          cy={radius}
        />
        {/* Foreground progress circle */}
        <circle
          stroke="var(--accent)"
          fill="transparent"
          strokeWidth={stroke}
          strokeDasharray={circumference + ' ' + circumference}
          style={{ strokeDashoffset }}
          r={normalizedRadius}
          cx={radius}
          cy={radius}
          className="progress-ring-circle"
        />
      </svg>
      <div className="progress-ring-inner-text">
        {text !== undefined ? text : `${Math.round(cleanProgress)}%`}
      </div>
    </div>
  );
};
export default ProgressRing;
