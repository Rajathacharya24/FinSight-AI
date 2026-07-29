import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';

export const api = axios.create({
  baseURL,
  withCredentials: false,
  timeout: 30000,
});

// ── Request: attach JWT ────────────────────────────────────────────────────
api.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('finsight.token');
  if (token) cfg.headers.Authorization = `Bearer ${token}`;
  return cfg;
});

// ── Response: 401 → try silent refresh, then redirect ─────────────────────
let isRefreshing = false;
let refreshQueue = [];

api.interceptors.response.use(
  (res) => res,
  async (err) => {
    const original = err.config;

    if (err.response?.status === 401 && !original._retried) {
      original._retried = true;

      if (isRefreshing) {
        // Queue requests while refresh is in flight
        return new Promise((resolve, reject) => {
          refreshQueue.push({ resolve, reject });
        }).then((token) => {
          original.headers.Authorization = `Bearer ${token}`;
          return api(original);
        });
      }

      isRefreshing = true;
      const refreshToken = localStorage.getItem('finsight.refreshToken');

      if (!refreshToken) {
        _redirectToLogin();
        return Promise.reject(err);
      }

      try {
        const { data } = await axios.post(`${baseURL}/api/v1/auth/refresh`, { refreshToken });
        const newToken = data.accessToken;
        localStorage.setItem('finsight.token', newToken);
        if (data.refreshToken) localStorage.setItem('finsight.refreshToken', data.refreshToken);

        // Flush queue
        refreshQueue.forEach(({ resolve }) => resolve(newToken));
        refreshQueue = [];
        isRefreshing = false;

        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      } catch {
        refreshQueue.forEach(({ reject }) => reject(err));
        refreshQueue = [];
        isRefreshing = false;
        _redirectToLogin();
        return Promise.reject(err);
      }
    }

    return Promise.reject(err);
  }
);

function _redirectToLogin() {
  localStorage.removeItem('finsight.token');
  localStorage.removeItem('finsight.refreshToken');
  window.location.href = '/login';
}

// ── Auth ────────────────────────────────────────────────────────────────────
export const auth = {
  // NOTE: backend LoginRequest uses 'username' not 'email'
  login:    (username, password) => api.post('/api/v1/auth/login',    { username, password }),
  register: (username, email, password) => api.post('/api/v1/auth/register', { username, email, password }),
  refresh:  (refreshToken)       => api.post('/api/v1/auth/refresh',  { refreshToken }),
  logout:   (refreshToken)       => api.post('/api/v1/auth/logout',   { refreshToken }),
  me:       ()                   => api.get('/api/v1/auth/me'),
};

// ── Workflows ────────────────────────────────────────────────────────────────
export const workflows = {
  list:     ()       => api.get('/api/v1/workflows'),
  get:      (id)     => api.get(`/api/v1/workflows/${id}`),
  auditLog: (id)     => api.get(`/api/v1/workflows/${id}/audit-log`),
  review:   (id, action, reason) => api.post(`/api/v1/workflows/${id}/review`, { action, reason }),
  upload:   (file, metadata) => {
    const form = new FormData();
    form.append('file', file);
    form.append('metadata', new Blob([JSON.stringify(metadata)], { type: 'application/json' }));
    return api.post('/api/v1/workflows', form, { headers: { 'Content-Type': 'multipart/form-data' } });
  },
};

// ── Analytics ────────────────────────────────────────────────────────────────
export const analytics = {
  summary:    () => api.get('/api/v1/analytics/summary'),
  timeseries: () => api.get('/api/v1/analytics/timeseries'),
};

// ── Documents ────────────────────────────────────────────────────────────────
export const documents = {
  getContent:  (id) => api.get(`/api/v1/documents/${id}/content`),
  get:         (id) => api.get(`/api/v1/documents/${id}`),
  getDownload: (id) => api.get(`/api/v1/documents/${id}/download`),
};

// ── Notifications ─────────────────────────────────────────────────────────────
export const notifications = {
  getByUser:  (userId, page = 0, size = 20) =>
    api.get(`/api/v1/notifications/${userId}`, { params: { page, size } }),
  markRead:   (id) => api.patch(`/api/v1/notifications/${id}/read`),
  markAllRead: (userId) => api.patch(`/api/v1/notifications/${userId}/read-all`),
};
