import { useState } from 'react';
import { workflows } from '../api/client.js';

export default function Upload() {
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  async function submit(e) {
    e.preventDefault();
    if (!file) return;
    setBusy(true);
    setError('');
    try {
      const { data } = await workflows.upload(file, { title, description });
      setResult(data);
    } catch (err) {
      setError(err?.response?.data?.message || err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="max-w-3xl">
      <h1 className="text-2xl font-semibold mb-2">Upload Document</h1>
      <p className="text-sm text-slate-500 mb-6">
        Kicks off the Upload → Extract → Validate → Recommend workflow.
      </p>

      <form onSubmit={submit} className="bg-white p-6 rounded-xl shadow space-y-4">
        <input
          className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Title"
          required
        />
        <textarea
          className="w-full border border-slate-300 rounded px-3 py-2 text-sm"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Description (optional)"
          rows={3}
        />
        <input
          type="file"
          accept="application/pdf"
          onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          className="text-sm"
          required
        />
        <button
          disabled={busy}
          className="px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white rounded text-sm font-medium disabled:opacity-50"
        >
          {busy ? 'Processing…' : 'Start workflow'}
        </button>
        {error && <div className="text-sm text-red-600">{error}</div>}
      </form>

      {result && (
        <div className="mt-6 bg-white p-6 rounded-xl shadow">
          <h2 className="font-medium mb-3">Workflow {result.workflowId} — {result.status}</h2>
          <pre className="text-xs overflow-x-auto bg-slate-50 p-3 rounded">
            {JSON.stringify(result, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}
