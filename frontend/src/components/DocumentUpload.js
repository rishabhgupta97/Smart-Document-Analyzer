import React, { useState, useRef } from 'react';
import { uploadDocument } from '../services/api';
import './DocumentUpload.css';

const DocumentUpload = ({ onAnalysisComplete, onUploadStart, loading, onReset }) => {
  const [dragActive, setDragActive] = useState(false);
  const [error, setError] = useState(null);
  const fileInputRef = useRef(null);

  const handleFiles = async (files) => {
    if (files && files[0]) {
      const file = files[0];

      // Validate file type
      const allowedTypes = [
        'application/pdf',
        'text/plain',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      ];
      if (!allowedTypes.includes(file.type)) {
        setError('Please upload a PDF, TXT, or DOCX file.');
        return;
      }

      // Validate file size (50MB max)
      if (file.size > 50 * 1024 * 1024) {
        setError('File size must be less than 50MB.');
        return;
      }

      setError(null);
      onUploadStart();

      try {
        const result = await uploadDocument(file);
        onAnalysisComplete(result);
      } catch (err) {
        setError(err.message || 'An error occurred while processing the file.');
        onAnalysisComplete(null);
      }
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const handleChange = (e) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      handleFiles(e.target.files);
    }
  };

  const onButtonClick = () => {
    fileInputRef.current.click();
  };

  return (
    <div className="card">
      <h2>📄 Upload Document</h2>
      <p className="text-muted">Upload a PDF, Word document, or text file to analyze</p>

      <div
        className={`upload-area ${dragActive ? 'drag-active' : ''} ${loading ? 'loading' : ''}`}
        onDragEnter={handleDrag}
        onDragLeave={handleDrag}
        onDragOver={handleDrag}
        onDrop={handleDrop}
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".pdf,.txt,.docx"
          onChange={handleChange}
          style={{ display: 'none' }}
          disabled={loading}
        />

        {loading ? (
          <div className="upload-loading">
            <div className="spinner"></div>
            <p>Analyzing document...</p>
          </div>
        ) : (
          <>
            <div className="upload-icon">📎</div>
            <p>
              <strong>Drag and drop</strong> your file here, or{' '}
              <button type="button" className="upload-button" onClick={onButtonClick} disabled={loading}>
                browse
              </button>
            </p>
            <p className="file-info">Supports PDF, DOCX, and TXT files up to 50MB</p>
          </>
        )}
      </div>

      {error && <div className="error-message">⚠️ {error}</div>}

      {onReset && (
        <div className="mt-4">
          <button className="btn btn-secondary" onClick={onReset} disabled={loading}>
            Reset
          </button>
        </div>
      )}
    </div>
  );
};

export default DocumentUpload;
