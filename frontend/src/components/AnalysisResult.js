import React, { useState } from 'react';
import './AnalysisResult.css';

const AnalysisResult = ({ analysis, loading }) => {
  const [showFullText, setShowFullText] = useState(false);

  if (loading) {
    return (
      <div className="card">
        <h2>📊 Analysis Results</h2>
        <div className="loading-placeholder">
          <div className="loading-skeleton"></div>
          <div className="loading-skeleton"></div>
          <div className="loading-skeleton"></div>
        </div>
      </div>
    );
  }

  if (!analysis) {
    return (
      <div className="card">
        <h2>📊 Analysis Results</h2>
        <div className="empty-state">
          <div className="empty-icon">📋</div>
          <p>Upload a document to see analysis results here</p>
        </div>
      </div>
    );
  }

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="card">
      <h2>📊 Analysis Results</h2>

      <div className="file-info-section">
        <h3>📝 File Information</h3>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Filename:</span>
            <span className="info-value">{analysis.filename}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Type:</span>
            <span className="info-value">{analysis.fileType.toUpperCase()}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Size:</span>
            <span className="info-value">{formatFileSize(analysis.fileSize)}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Analyzed:</span>
            <span className="info-value">{new Date(analysis.analyzedAt).toLocaleString()}</span>
          </div>
        </div>
      </div>

      <div className="metrics-section">
        <h3>📈 Content Metrics</h3>
        <div className="metrics-grid">
          <div className="metric-card">
            <div className="metric-number">{analysis.wordCount.toLocaleString()}</div>
            <div className="metric-label">Words</div>
          </div>
          <div className="metric-card">
            <div className="metric-number">{analysis.characterCount.toLocaleString()}</div>
            <div className="metric-label">Characters</div>
          </div>
          <div className="metric-card">
            <div className="metric-number">{analysis.readingTime}</div>
            <div className="metric-label">Reading Time</div>
          </div>
        </div>
      </div>

      {analysis.summary && (
        <div className="summary-section">
          <h3>📋 Summary</h3>
          <div className="summary-content">{analysis.summary}</div>
        </div>
      )}

      {analysis.extractedText && (
        <div className="text-section">
          <h3>📄 Extracted Text</h3>
          <div className="text-content">
            <div className={`text-preview ${showFullText ? 'expanded' : ''}`}>
              {showFullText ? analysis.extractedText : analysis.extractedText.substring(0, 500)}
              {!showFullText && analysis.extractedText.length > 500 && '...'}
            </div>
            {analysis.extractedText.length > 500 && (
              <button className="toggle-text-btn" onClick={() => setShowFullText(!showFullText)}>
                {showFullText ? '🔼 Show Less' : '🔽 Show More'}
              </button>
            )}
          </div>
        </div>
      )}

      <div className="action-section mt-4">
        <div className="action-note">
          💡 <strong>Coming Soon:</strong> Sentiment analysis, keyword extraction, and document categorization using AWS
          AI services
        </div>
      </div>
    </div>
  );
};

export default AnalysisResult;
