import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useAuth } from '../hooks/useAuth.js';

const localStorageMock = (() => {
  let store = {};
  return {
    getItem: (key) => store[key] ?? null,
    setItem: (key, value) => { store[key] = value; },
    removeItem: (key) => { delete store[key]; },
    clear: () => { store = {}; }
  };
})();

Object.defineProperty(window, 'localStorage', { value: localStorageMock });

describe('useAuth', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('returns not authenticated when no token', () => {
    const { result } = renderHook(() => useAuth());
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.token).toBeNull();
  });

  it('returns authenticated after login', () => {
    const { result } = renderHook(() => useAuth());
    act(() => result.current.login('test-token'));
    expect(result.current.isAuthenticated).toBe(true);
    expect(result.current.token).toBe('test-token');
  });

  it('clears token on logout', () => {
    const { result } = renderHook(() => useAuth());
    act(() => result.current.login('test-token'));
    act(() => result.current.logout());
    expect(result.current.isAuthenticated).toBe(false);
    expect(result.current.token).toBeNull();
  });

  it('reads existing token from localStorage', () => {
    localStorage.setItem('finsight.token', 'stored-token');
    const { result } = renderHook(() => useAuth());
    expect(result.current.token).toBe('stored-token');
    expect(result.current.isAuthenticated).toBe(true);
  });
});
