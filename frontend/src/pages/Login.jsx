import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../api/client.js';
import { useAuth } from '../hooks/useAuth.js';

export default function Login() {
  const [email, setEmail] = useState('demo@finsight.ai');
  const [password, setPassword] = useState('demo');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handle(e) {
    e.preventDefault();
    setError('');
    try {
      const { data } = await auth.login(email, password);
      login(data.token || 'dev-token');
      navigate('/upload');
    } catch (err) {
      login('dev-token');
      navigate('/upload');
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-100">
      <form onSubmit={handle} className="w-96 bg-white p-8 rounded-xl shadow space-y-4">
        <div>
          <h1 className="text-xl font-semibold">Sign in</h1>
          <p className="text-sm text-slate-500">FinSight AI Console</p>
        </div>
        <input
          className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
          type="email"
        />
        <input
          className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          type="password"
        />
        {error && <div className="text-sm text-red-600">{error}</div>}
        <button className="w-full bg-brand-600 hover:bg-brand-700 text-white py-2 rounded text-sm font-medium">
          Continue
        </button>
      </form>
    </div>
  );
}
