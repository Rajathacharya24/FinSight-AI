import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

export const api = axios.create({
  baseURL,
  withCredentials: false,
  timeout: 30000,
});

// Request: attach JWT
api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('finsight.token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

// Response: on 401, clear token and redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('finsight.token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export const auth = {
  login:    (email, password) => api.post('/api/v1/auth/login',    { email, password }),
  register: (email, password) => api.post('/api/v1/auth/register', { email, password }),
  refresh:  (refreshToken)    => api.post('/api/v1/auth/refresh',  { refreshToken }),
};

export const workflows = {
  list:     ()       => api.get('/api/v1/workflows'),
  get:      (id)     => api.get(`/api/v1/workflows/${id}`),
  auditLog: (id)     => api.get(`/api/v1/workflows/${id}/audit-log`),
  upload:   (file, metadata) => {
    const form = new FormData();
    form.append('file', file);
    form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return api.post('/api/v1/workflows', form, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
};

export const analytics = {
  summary:    () => api.get('/api/v1/analytics/summary'),
  timeseries: () => api.get('/api/v1/analytics/timeseries'),
};

export const documents = {
  getContent: (id) => api.get(`/api/v1/documents/${id}/content`),
  get:        (id) => api.get(`/api/v1/documents/${id}`),
};
