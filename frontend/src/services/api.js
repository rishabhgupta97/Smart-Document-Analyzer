import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    console.log(`Making ${config.method.toUpperCase()} request to ${config.url}`);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    console.error('API Error:', error);

    if (error.response) {
      // Server responded with error status
      const message =
        error.response.data?.error || error.response.data?.message || `Server error: ${error.response.status}`;
      throw new Error(message);
    } else if (error.request) {
      // Request was made but no response received
      throw new Error('No response from server. Please check if the backend is running.');
    } else {
      // Something else happened
      throw new Error('Request failed: ' + error.message);
    }
  }
);

export const uploadDocument = async (file) => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post('/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

export const getAnalysis = async (documentId) => {
  const response = await api.get(`/documents/${documentId}/analysis`);
  return response.data;
};

export const getAllAnalyses = async () => {
  const response = await api.get('/documents/all');
  return response.data;
};

export const healthCheck = async () => {
  const response = await api.get('/documents/health');
  return response.data;
};

export default api;
