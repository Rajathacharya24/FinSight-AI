import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { workflows, documents } from '../api/client.js';

const STEPS_ORDER = ['UPLOAD', 'EXTRACT', 'VALIDATE', 'RECOMMEND'];

const STATUS_CONFIG = {
  COMPLETED:   { cls: 'bg-success-500/15 text-success-300 border-success-500/25' },
  FAILED:      { cls: 'bg-danger-500/15 text-danger-300 border-danger-500/25' },
  IN_PROGRESS: { cls: 'bg-brand-500/15 text-brand-300 border-brand-500/25' },
  CREATED:     { cls: 'bg-warning-500/10 text-warning-400 border-warning-500/20' },
};

function StepTimeline({ currentStep, status }) {
  const current = currentStep?.toUpperCase?.() ?? '';
  const idx = STEPS_ORDER.indexOf(current);
  const failed = status === 'FAILED';

  return (
    <div className="flex items-start gap-0 py-2">
      {STEPS_ORDER.map((step, i) => {
        const done    = !failed && i < idx;
        const active  = i === idx;
        const isFailed = failed && active;

        return (
          <div key={step} className="flex items-center flex-1">
            <div className="flex flex-col items-center gap-1.5">
              <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-500 ${
                done       ? 'bg-success-500 text-white shadow-glow-success' :
                isFailed   ? 'bg-danger-500 text-white shadow-glow-danger' :
                active     ? 'bg-gradient-brand text-white shadow-glow-brand animate-pulse-brand' :
                             'bg-white/8 text-slate-600'
              }`}>
                {done ? '✓' : isFailed ? '✕' : i + 1}
              </div>
              <span className={`text-[10px] font-semibold uppercase tracking-wide ${
                done      ? 'text-success-400' :
                isFailed  ? 'text-danger-400' :
                active    ? 'text-brand-300' :
                            'text-slate-600'
              }`}>
                {step.charAt(0) + step.slice(1).toLowerCase()}
              </span>
            </div>
            {i < STEPS_ORDER.length - 1 && (
              <div className={`flex-1 h-px mx-2 mt-[-16px] transition-all duration-700 ${
                done ? 'bg-success-500/50' : 'bg-white/8'
              }`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

function ConfidenceBar({ score }) {
  if (score == null) return null;
  const pct = Math.round(score);
  const color = pct >= 80 ? 'bg-success-500' : pct >= 50 ? 'bg-warning-500' : 'bg-danger-500';
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs">
        <span className="text-slate-400">AI Confidence</span>
        <span className="font-semibold text-white">{pct}%</span>
      </div>
      <div className="w-full h-2 bg-white/8 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-1000 ${color}`}
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  );
}

export default function WorkflowDetail() {
  const { id }                        = useParams();
  const [workflow, setWorkflow]       = useState(null);
  const [auditLog, setAuditLog]       = useState([]);
  const [docContent, setDocContent]   = useState(null);
  const [error, setError]             = useState('');
  const [loading, setLoading]         = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [wfRes, auditRes] = await Promise.all([
          workflows.get(id),
          workflows.auditLog(id).catch(() => ({ data: [] })),
        ]);
        setWorkflow(wfRes.data);
        setAuditLog(auditRes.data || []);

        if (wfRes.data?.documentId) {
          documents.getContent(wfRes.data.documentId)
            .then((r) => setDocContent(r.data))
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

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64 gap-3 text-slate-400">
        <svg className="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
        Loading workflow…
      </div>
    );
  }
  if (error) return <div className="text-danger-300 p-6 text-sm">{error}</div>;
  if (!workflow) return null;

  const statusCfg = STATUS_CONFIG[workflow.status] || STATUS_CONFIG.CREATED;
  const extraction = workflow.extraction;
  const decision   = workflow.decision;
  const compliance = workflow.compliance;

  return (
    <div className="space-y-5 max-w-4xl animate-slide-up">

      {/* Header */}
      <div className="flex items-center gap-3 flex-wrap">
        <Link
          to="/documents"
          className="text-xs text-slate-500 hover:text-slate-300 flex items-center gap-1 transition-colors"
        >
          ← Workflows
        </Link>
        <h1 className="section-title">Workflow #{workflow.workflowId}</h1>
        <span className={`status-badge border ${statusCfg.cls}`}>{workflow.status}</span>
      </div>

      {/* Step timeline */}
      <div className="glass-card-solid p-5">
        <StepTimeline currentStep={workflow.currentStep} status={workflow.status} />
      </div>

      {/* Meta grid */}
      <div className="glass-card-solid p-5 grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          ['Step',        workflow.currentStep?.toLowerCase() ?? '—'],
          ['Document ID', workflow.documentId ?? '—'],
          ['Created',     workflow.createdAt   ? new Date(workflow.createdAt).toLocaleString()  : '—'],
          ['Updated',     workflow.updatedAt   ? new Date(workflow.updatedAt).toLocaleString()  : '—'],
        ].map(([label, value]) => (
          <div key={label}>
            <p className="text-[10px] font-semibold text-slate-500 uppercase tracking-wider mb-0.5">{label}</p>
            <p className="text-sm font-medium text-white truncate capitalize">{value}</p>
          </div>
        ))}
        {workflow.errorMessage && (
          <div className="col-span-2 sm:col-span-4 mt-1 p-3 bg-danger-500/10 border border-danger-500/25 rounded-lg text-xs text-danger-300">
            <strong>Error:</strong> {workflow.errorMessage}
          </div>
        )}
      </div>

      {/* Extracted Data + Confidence */}
      {extraction && (
        <div className="glass-card-solid p-5 space-y-4">
          <h2 className="text-sm font-semibold text-white">Extracted Data</h2>
          <div className="grid grid-cols-2 gap-3">
            {[
              ['Applicant',   extraction.applicantName],
              ['Income',      extraction.income      ? `$${parseInt(extraction.income).toLocaleString()}`      : null],
              ['Address',     extraction.address],
              ['Loan Amount', extraction.loanAmount  ? `$${parseInt(extraction.loanAmount).toLocaleString()}`  : null],
            ].map(([label, val]) => val ? (
              <div key={label} className="bg-white/4 rounded-xl p-3">
                <p className="text-[10px] text-slate-500 uppercase tracking-wider mb-1">{label}</p>
                <p className="text-sm font-semibold text-white">{val}</p>
              </div>
            ) : null)}
          </div>
          {extraction.confidenceScore != null && (
            <ConfidenceBar score={extraction.confidenceScore} />
          )}
        </div>
      )}

      {/* Compliance & Decision */}
      {(compliance || decision) && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {compliance && (
            <div className="glass-card-solid p-5 space-y-2">
              <h2 className="text-sm font-semibold text-white">Compliance</h2>
              <p className={`text-xs font-medium px-2 py-1 rounded-full inline-block ${
                compliance.passed ? 'bg-success-500/15 text-success-300' : 'bg-danger-500/15 text-danger-300'
              }`}>
                {compliance.passed ? '✓ Passed' : '✕ Issues Found'}
              </p>
              {compliance.issues?.length > 0 && (
                <ul className="text-xs text-slate-400 space-y-1 mt-2">
                  {compliance.issues.map((issue, i) => (
                    <li key={i} className="flex items-start gap-1.5">
                      <span className="text-warning-400 mt-0.5">⚠</span>{issue}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
          {decision && (
            <div className="glass-card-solid p-5 space-y-2">
              <h2 className="text-sm font-semibold text-white">Decision</h2>
              <p className={`text-xs font-medium px-2 py-1 rounded-full inline-block ${
                decision.recommendation === 'APPROVE'
                  ? 'bg-success-500/15 text-success-300'
                  : 'bg-danger-500/15 text-danger-300'
              }`}>
                {decision.recommendation}
              </p>
              {decision.reason && (
                <p className="text-xs text-slate-400 mt-1">{decision.reason}</p>
              )}
            </div>
          )}
        </div>
      )}

      {/* Audit Log */}
      {auditLog.length > 0 && (
        <div className="glass-card-solid p-5 space-y-3">
          <h2 className="text-sm font-semibold text-white">Audit Log</h2>
          <ol className="space-y-2.5">
            {auditLog.map((entry, i) => (
              <li key={i} className="flex gap-3 text-xs">
                <span className="text-slate-600 shrink-0 font-mono">
                  {new Date(entry.timestamp).toLocaleTimeString()}
                </span>
                <div>
                  <span className="font-semibold text-brand-300">{entry.step}</span>
                  <span className="text-slate-400"> — {entry.message}</span>
                </div>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
}
