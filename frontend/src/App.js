import React, { useState } from 'react';
import DocumentUpload from './components/DocumentUpload';
import AnalysisResult from './components/AnalysisResult';
import './App.css';

function App() {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleAnalysisComplete = (result) => {
    setAnalysis(result);
    setLoading(false);
  };

  const handleUploadStart = () => {
    setLoading(true);
    setAnalysis(null);
  };

  const handleReset = () => {
    setAnalysis(null);
    setLoading(false);
  };

  return (
    <div className="App">
      <header className="app-header">
        <div className="container">
          <h1>🧠 Smart Document Analyzer</h1>
          <p className="subtitle">Upload your documents and get instant AI-powered insights</p>
        </div>
      </header>

      <main className="container">
        <div className="grid-2">
          <div>
            <DocumentUpload
              onAnalysisComplete={handleAnalysisComplete}
              onUploadStart={handleUploadStart}
              loading={loading}
              onReset={handleReset}
            />
          </div>

          <div>
            <AnalysisResult analysis={analysis} loading={loading} />
          </div>
        </div>
      </main>

      <footer className="app-footer">
        <div className="container text-center">
          <p className="text-muted">Built with React + Spring Boot + AI • Created for portfolio demonstration</p>
        </div>
      </footer>
    </div>
  );
}

export default App;
