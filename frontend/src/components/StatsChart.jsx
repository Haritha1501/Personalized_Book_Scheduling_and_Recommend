import React from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
} from 'chart.js';
import { Bar, Line } from 'react-chartjs-2';

// Register ChartJS modules
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

export const StatsChart = ({ labels, data, label = 'Reading activity', type = 'bar' }) => {
  const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
  const gridColor = isDark ? '#1E293B' : '#E2E8F0';
  const textColor = isDark ? '#94A3B8' : '#6C757D';

  const chartData = {
    labels: labels || ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        label: label,
        data: data || [0, 0, 0, 0, 0, 0, 0],
        backgroundColor: type === 'line' ? 'rgba(255, 107, 53, 0.15)' : '#FF6B35',
        borderColor: '#FF6B35',
        borderWidth: 2,
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#FF6B35',
        pointRadius: 4,
        pointHoverRadius: 6,
      }
    ]
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false, // Clean look, legend not needed for single series
      },
      tooltip: {
        backgroundColor: isDark ? '#1E222F' : '#FFFFFF',
        titleColor: isDark ? '#F8F9FA' : '#1A1D20',
        bodyColor: isDark ? '#94A3B8' : '#5E6977',
        borderColor: '#FF6B35',
        borderWidth: 1,
        padding: 10,
        boxPadding: 4,
        usePointStyle: true,
      }
    },
    scales: {
      x: {
        grid: {
          display: false,
        },
        ticks: {
          color: textColor,
          font: {
            family: 'Inter',
            size: 11
          }
        }
      },
      y: {
        grid: {
          color: gridColor,
        },
        ticks: {
          color: textColor,
          font: {
            family: 'Inter',
            size: 11
          }
        },
        beginAtZero: true
      }
    }
  };

  return (
    <div style={{ width: '100%', height: '100%', minHeight: '260px', position: 'relative' }}>
      {type === 'line' ? (
        <Line data={chartData} options={options} />
      ) : (
        <Bar data={chartData} options={options} />
      )}
    </div>
  );
};
export default StatsChart;
