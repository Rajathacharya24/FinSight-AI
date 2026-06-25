import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

export const api = axios.create({
  baseURL,
  withCredentials: false
});

api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('finsight.token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

export const auth = {
  login: (email, password) => api.post('/api/v1/auth/login', { email, password }),
  register: (email, password) => api.post('/api/v1/auth/register', { email, password })
};

export const workflows = {
  list: () => api.get('/api/v1/workflows'),
  get: (id) => api.get(`/api/v1/workflows/${id}`),
  upload: (file, metadata) => {
    const form = new FormData();
    form.append('file', file);
    form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return api.post('/api/v1/workflows', form, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
  auditLog: (id) => api.get(`/api/v1/workflows/${id}/audit-log`)
};

export const analytics = {
  summary: () => api.get('/api/v1/analytics/summary'),
  timeseries: () => api.get('/api/v1/analytics/timeseries')
};
