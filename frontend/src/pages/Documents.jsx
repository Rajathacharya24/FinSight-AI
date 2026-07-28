import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflows } from '../api/client.js';

const STATUS_CONFIG = {
  COMPLETED:   { label: 'Completed',   cls: 'bg-success-500/15 text-success-300 border-success-500/25' },
  FAILED:      { label: 'Failed',      cls: 'bg-danger-500/15 text-danger-300 border-danger-500/25' },
  IN_PROGRESS: { label: 'In Progress', cls: 'bg-brand-500/15 text-brand-300 border-brand-500/25' },
  CREATED:     { label: 'Created',     cls: 'bg-warning-500/10 text-warning-400 border-warning-500/20' },
};

function StatusBadge({ status }) {
  const cfg = STATUS_CONFIG[status] || STATUS_CONFIG.CREATED;
  return (
    <span className={`status-badge border ${cfg.cls}`}>{cfg.label}</span>
  );
}

export default function Documents() {
  const [items, setItems]   = useState([]);
  const [error, setError]   = useState('');
  const [loading, setLoading] = useState(true);
  const navigate            = useNavigate();

  useEffect(() => {
    workflows.list()
      .then(({ data }) => setItems(Array.isArray(data) ? data : []))
      .catch((e) => setError(e?.response?.data?.message || e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-6 animate-slide-up">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="section-title">Workflows</h1>
          <p className="section-subtitle">{items.length} application{items.length !== 1 ? 's' : ''} processed</p>
        </div>
        <button
          id="btn-new-workflow"
          className="btn-primary text-xs px-4 py-2"
          onClick={() => navigate('/upload')}
        >
          + New Upload
        </button>
      </div>

      {error && (
        <div className="flex items-center gap-2 bg-danger-500/10 border border-danger-500/25 rounded-xl px-4 py-3 text-sm text-danger-300">
          <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {error}
        </div>
      )}

      {/* Table */}
      <div className="glass-card-solid overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/8">
              {['Workflow', 'Document', 'Status', 'Step', 'Created'].map((h) => (
                <th key={h} className="text-left px-5 py-3.5 text-[11px] font-semibold text-slate-500 uppercase tracking-wider">
                  {h}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan="5" className="px-5 py-8 text-center">
                  <div className="flex items-center justify-center gap-2 text-slate-500 text-sm">
                    <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                    </svg>
                    Loading workflows…
                  </div>
                </td>
              </tr>
            )}
            {!loading && items.length === 0 && (
              <tr>
                <td colSpan="5" className="px-5 py-12 text-center">
                  <div className="flex flex-col items-center gap-3">
                    <div className="w-12 h-12 rounded-2xl bg-brand-600/15 flex items-center justify-center">
                      <svg className="w-6 h-6 text-brand-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                    </div>
                    <p className="text-slate-400 text-sm font-medium">No workflows yet</p>
                    <p className="text-slate-600 text-xs">Upload a PDF to start your first workflow</p>
                  </div>
                </td>
              </tr>
            )}
            {items.map((w) => (
              <tr
                id={`row-workflow-${w.workflowId}`}
                key={w.workflowId}
                className="trow"
                onClick={() => navigate(`/documents/${w.workflowId}`)}
              >
                <td className="px-5 py-3.5">
                  <span className="font-semibold text-white">#{w.workflowId}</span>
                </td>
                <td className="px-5 py-3.5 text-slate-400">
                  {w.documentId ?? <span className="text-slate-600">—</span>}
                </td>
                <td className="px-5 py-3.5">
                  <StatusBadge status={w.status} />
                </td>
                <td className="px-5 py-3.5 text-slate-400 capitalize text-xs">
                  {w.currentStep?.toLowerCase() ?? '—'}
                </td>
                <td className="px-5 py-3.5 text-slate-500 text-xs">
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
