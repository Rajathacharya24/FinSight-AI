import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from '../pages/Login.jsx';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('../hooks/useAuth.js', () => ({
  useAuth: () => ({
    login: vi.fn(),
    isAuthenticated: false,
    token: null
  })
}));

vi.mock('../api/client.js', () => ({
  auth: {
    login: vi.fn().mockResolvedValue({ data: { token: 'mock-token' } }),
    register: vi.fn()
  }
}));

describe('Login page', () => {
  beforeEach(() => {
    mockNavigate.mockReset();
  });

  it('renders sign in form', () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
    expect(screen.getByText('Sign in')).toBeDefined();
    expect(screen.getByPlaceholderText('Email')).toBeDefined();
    expect(screen.getByPlaceholderText('Password')).toBeDefined();
  });

  it('has pre-filled demo credentials', () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );
    const emailInput = screen.getByPlaceholderText('Email');
    expect(emailInput.value).toBe('demo@finsight.ai');
  });
});
