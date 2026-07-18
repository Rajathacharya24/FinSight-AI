import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflows } from '../api/client.js';

export default function Documents() {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    workflows.list()
      .then(({ data }) => setItems(Array.isArray(data) ? data : []))
      .catch((e) => setError(e?.response?.data?.message || e.message));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-semibold mb-6">Workflows</h1>
      {error && <div className="text-sm text-red-600 mb-3">{error}</div>}
      <div className="bg-white rounded-xl shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-slate-100 text-slate-700">
            <tr>
              <th className="text-left px-4 py-3">Workflow</th>
              <th className="text-left px-4 py-3">Document</th>
              <th className="text-left px-4 py-3">Status</th>
              <th className="text-left px-4 py-3">Step</th>
              <th className="text-left px-4 py-3">Created</th>
            </tr>
          </thead>
          <tbody>
            {items.length === 0 && (
              <tr><td colSpan="5" className="px-4 py-6 text-center text-slate-400">No workflows yet.</td></tr>
            )}
            {items.map((w) => (
              <tr
                key={w.workflowId}
                className="border-t border-slate-100 hover:bg-slate-50 cursor-pointer"
                onClick={() => navigate(`/documents/${w.workflowId}`)}
              >
                <td className="px-4 py-3 font-medium">#{w.workflowId}</td>
                <td className="px-4 py-3">{w.documentId ?? '—'}</td>
                <td className="px-4 py-3">
                  <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                    w.status === 'COMPLETED'   ? 'bg-green-100 text-green-700'  :
                    w.status === 'FAILED'      ? 'bg-red-100 text-red-700'      :
                    w.status === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700'   :
                    'bg-amber-100 text-amber-700'
                  }`}>{w.status}</span>
                </td>
                <td className="px-4 py-3">{w.currentStep ?? '—'}</td>
                <td className="px-4 py-3 text-slate-500">
                  {w.createdAt ? new Date(w.createdAt).toLocaleString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
