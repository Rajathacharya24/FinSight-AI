import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { workflows, documents } from '../api/client.js';

const STATUS_COLORS = {
  COMPLETED: 'bg-green-100 text-green-700',
  FAILED:    'bg-red-100 text-red-700',
  PENDING:   'bg-amber-100 text-amber-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
};

export default function WorkflowDetail() {
  const { id } = useParams();
  const [workflow, setWorkflow]   = useState(null);
  const [auditLog, setAuditLog]   = useState([]);
  const [extraction, setExtraction] = useState(null);
  const [error, setError]         = useState('');
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [wfRes, auditRes] = await Promise.all([
          workflows.get(id),
          workflows.auditLog(id).catch(() => ({ data: [] })),
        ]);
        setWorkflow(wfRes.data);
        setAuditLog(auditRes.data || []);

        // Load extracted content if document exists
        if (wfRes.data?.documentId) {
          documents.getContent(wfRes.data.documentId)
            .then(r => setExtraction(r.data))
            .catch(() => {});
        }
      } catch (e) {
        setError(e?.response?.data?.message || e.message);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [id]);

  if (loading) return <div className="text-sm text-slate-500 p-6">Loading…</div>;
  if (error)   return <div className="text-sm text-red-600 p-6">{error}</div>;
  if (!workflow) return null;

  const statusClass = STATUS_COLORS[workflow.status] || STATUS_COLORS.PENDING;

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center gap-3">
        <Link to="/documents" className="text-sm text-slate-400 hover:text-slate-600">← Workflows</Link>
        <h1 className="text-2xl font-semibold">Workflow #{workflow.workflowId}</h1>
        <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${statusClass}`}>
          {workflow.status}
        </span>
      </div>

      {/* Summary Card */}
      <div className="bg-white rounded-xl shadow p-6 grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          ['Step',        workflow.currentStep ?? '—'],
          ['Document ID', workflow.documentId  ?? '—'],
          ['Created',     workflow.createdAt   ? new Date(workflow.createdAt).toLocaleString() : '—'],
          ['Updated',     workflow.updatedAt   ? new Date(workflow.updatedAt).toLocaleString() : '—'],
        ].map(([label, value]) => (
          <div key={label}>
            <p className="text-xs text-slate-400 mb-1">{label}</p>
            <p className="text-sm font-medium truncate">{value}</p>
          </div>
        ))}
        {workflow.errorMessage && (
          <div className="col-span-2 sm:col-span-4 mt-2 p-3 bg-red-50 rounded text-xs text-red-700">
            <strong>Error:</strong> {workflow.errorMessage}
          </div>
        )}
      </div>

      {/* Extracted Data */}
      {extraction && (
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="font-semibold mb-3 text-sm">Extracted Data</h2>
          <div className="grid grid-cols-2 gap-3 text-sm">
            {[
              ['Applicant', extraction.applicantName],
              ['Income',    extraction.income ? `$${parseInt(extraction.income).toLocaleString()}` : null],
              ['Address',   extraction.address],
              ['Loan Amount', extraction.loanAmount ? `$${parseInt(extraction.loanAmount).toLocaleString()}` : null],
            ].map(([label, val]) => val ? (
              <div key={label} className="bg-slate-50 rounded p-3">
                <p className="text-xs text-slate-400 mb-0.5">{label}</p>
                <p className="font-medium">{val}</p>
              </div>
            ) : null)}
          </div>
        </div>
      )}

      {/* Audit Log */}
      {auditLog.length > 0 && (
        <div className="bg-white rounded-xl shadow p-6">
          <h2 className="font-semibold mb-3 text-sm">Audit Log</h2>
          <ol className="space-y-2 text-xs text-slate-600">
            {auditLog.map((entry, i) => (
              <li key={i} className="flex gap-3">
                <span className="text-slate-300 shrink-0">{new Date(entry.timestamp).toLocaleTimeString()}</span>
                <span><strong>{entry.step}</strong> — {entry.message}</span>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
}
