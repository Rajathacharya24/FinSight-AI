import React, { createContext, useContext, useState, useEffect } from 'react';
import client from '../api/client';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('finsight_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('finsight_token'));
  const [loading, setLoading] = useState(false);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const response = await client.post('/auth/login', { username, password });
      const { accessToken, id, email, roles } = response.data;
      const userData = { id, username, email, roles };
      
      setToken(accessToken);
      setUser(userData);
      localStorage.setItem('finsight_token', accessToken);
      localStorage.setItem('finsight_user', JSON.stringify(userData));
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        message: error.response?.data?.message || 'Login failed. Please check credentials.' 
      };
    } finally {
      setLoading(false);
    }
  };

  const register = async (username, email, password, firstName, lastName) => {
    setLoading(true);
    try {
      const response = await client.post('/auth/register', {
        username, email, password, firstName, lastName
      });
      const { accessToken, id, roles } = response.data;
      const userData = { id, username, email, roles };

      setToken(accessToken);
      setUser(userData);
      localStorage.setItem('finsight_token', accessToken);
      localStorage.setItem('finsight_user', JSON.stringify(userData));
      return { success: true };
    } catch (error) {
      return {
        success: false,
        message: error.response?.data?.message || 'Registration failed.'
      };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('finsight_token');
    localStorage.removeItem('finsight_user');
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
