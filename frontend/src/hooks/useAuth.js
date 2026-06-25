import { useState, useEffect } from 'react';

export function useAuth() {
  const [token, setToken] = useState(() => localStorage.getItem('finsight.token'));

  useEffect(() => {
    const handler = () => setToken(localStorage.getItem('finsight.token'));
    window.addEventListener('storage', handler);
    return () => window.removeEventListener('storage', handler);
  }, []);

  const login = (t) => {
    localStorage.setItem('finsight.token', t);
    setToken(t);
  };
  const logout = () => {
    localStorage.removeItem('finsight.token');
    setToken(null);
  };

  return { isAuthenticated: Boolean(token), token, login, logout };
}
