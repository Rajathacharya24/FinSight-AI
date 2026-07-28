import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { workflows } from '../api/client.js';

const STEPS = ['Upload', 'Extract', 'Validate', 'Recommend'];

function StepTimeline({ current }) {
  const stepOrder = ['UPLOAD', 'EXTRACT', 'VALIDATE', 'RECOMMEND'];
  const idx = stepOrder.indexOf(current?.toUpperCase?.() ?? '');
  return (
    <div className="flex items-center gap-0 mt-4">
      {STEPS.map((step, i) => {
        const done    = i < idx;
        const active  = i === idx;
        const pending = i > idx;
        return (
          <div key={step} className="flex items-center flex-1">
            <div className="flex flex-col items-center gap-1">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all duration-500 ${
                done    ? 'bg-success-500 text-white shadow-glow-success' :
                active  ? 'bg-gradient-brand text-white shadow-glow-brand animate-pulse-brand' :
                          'bg-white/10 text-slate-500'
              }`}>
                {done ? '✓' : i + 1}
              </div>
              <span className={`text-[10px] font-medium ${active ? 'text-brand-300' : done ? 'text-success-400' : 'text-slate-600'}`}>
                {step}
              </span>
            </div>
            {i < STEPS.length - 1 && (
              <div className={`flex-1 h-px mx-1 transition-all duration-700 ${done ? 'bg-success-500/60' : 'bg-white/10'}`} />
            )}
          </div>
        );
      })}
    </div>
  );
}

export default function Upload() {
  const [file, setFile]             = useState(null);
  const [title, setTitle]           = useState('');
  const [description, setDescription] = useState('');
  const [result, setResult]         = useState(null);
  const [busy, setBusy]             = useState(false);
  const [error, setError]           = useState('');
  const [dragOver, setDragOver]     = useState(false);
  const fileInputRef                = useRef();
  const navigate                    = useNavigate();

  async function submit(e) {
    e.preventDefault();
    if (!file) return;
    setBusy(true);
    setError('');
    setResult(null);
    try {
      const { data } = await workflows.upload(file, { title, description });
      setResult(data);
    } catch (err) {
      setError(err?.response?.data?.message || err.message);
    } finally {
      setBusy(false);
    }
  }

  function handleDrop(e) {
    e.preventDefault();
    setDragOver(false);
    const dropped = e.dataTransfer.files?.[0];
    if (dropped?.type === 'application/pdf') setFile(dropped);
    else setError('Only PDF files are accepted');
  }

  return (
    <div className="max-w-3xl space-y-6 animate-slide-up">
      {/* Header */}
      <div>
        <h1 className="section-title">Upload Document</h1>
        <p className="section-subtitle">Submit a PDF loan application to start the AI processing pipeline.</p>
      </div>

      {/* Upload Form */}
      <div className="glass-card-solid p-6 space-y-5">
        <form id="upload-form" onSubmit={submit} className="space-y-4">

          {/* Title */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Application Title <span className="text-danger-400">*</span>
            </label>
            <input
              id="input-title"
              className="form-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Q3 Mortgage Application"
              required
            />
          </div>

          {/* Description */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</label>
            <textarea
              id="input-description"
              className="form-input resize-none"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Optional — additional context about this application"
              rows={2}
            />
          </div>

          {/* Drag-and-drop zone */}
          <div
            id="drop-zone"
            className={`relative border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-all duration-300 ${
              dragOver
                ? 'border-brand-400 bg-brand-600/10 scale-[1.01]'
                : file
                ? 'border-success-400/50 bg-success-500/5'
                : 'border-white/15 hover:border-white/30 hover:bg-white/3'
            }`}
            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
            onDragLeave={() => setDragOver(false)}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
          >
            <input
              ref={fileInputRef}
              id="file-input"
              type="file"
              accept="application/pdf"
              className="hidden"
              onChange={(e) => { setFile(e.target.files?.[0] ?? null); setError(''); }}
            />
            {file ? (
              <div className="flex flex-col items-center gap-2 animate-fade-in">
                <div className="w-10 h-10 rounded-xl bg-success-500/20 flex items-center justify-center">
                  <svg className="w-5 h-5 text-success-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <p className="text-sm font-medium text-success-300">{file.name}</p>
                <p className="text-xs text-slate-500">{(file.size / 1024).toFixed(1)} KB · Click to replace</p>
              </div>
            ) : (
              <div className="flex flex-col items-center gap-2">
                <div className="w-10 h-10 rounded-xl bg-brand-600/20 flex items-center justify-center">
                  <svg className="w-5 h-5 text-brand-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12" />
                  </svg>
                </div>
                <p className="text-sm text-slate-300">Drag & drop your PDF here, or <span className="text-brand-300 font-medium">click to browse</span></p>
                <p className="text-xs text-slate-600">PDF only · max 10 MB</p>
              </div>
            )}
          </div>

          {error && (
            <div className="flex items-center gap-2 bg-danger-500/10 border border-danger-500/25 rounded-lg px-3 py-2 text-sm text-danger-300 animate-fade-in">
              <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              {error}
            </div>
          )}

          <button id="btn-start-workflow" type="submit" disabled={busy || !file} className="btn-primary">
            {busy ? (
              <>
                <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Processing…
              </>
            ) : '⚡ Start Workflow'}
          </button>
        </form>
      </div>

      {/* Result card */}
      {result && (
        <div className="glass-card-solid p-6 space-y-4 animate-slide-up border-success-500/20">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="font-semibold text-white">Workflow #{result.workflowId}</h2>
              <p className="text-xs text-slate-400 mt-0.5">Processing complete</p>
            </div>
            <span className="status-badge bg-success-500/15 text-success-300 border border-success-500/20">
              {result.status}
            </span>
          </div>

          {/* Step timeline */}
          <StepTimeline current={result.currentStep} />

          {/* Extraction summary */}
          {result.extraction && (
            <div className="grid grid-cols-2 gap-3 mt-2">
              {[
                ['Applicant', result.extraction.applicantName],
                ['Income',    result.extraction.income ? `$${parseInt(result.extraction.income).toLocaleString()}` : null],
                ['Loan Amt',  result.extraction.loanAmount ? `$${parseInt(result.extraction.loanAmount).toLocaleString()}` : null],
                ['Confidence', result.extraction.confidenceScore != null ? `${result.extraction.confidenceScore.toFixed(0)}%` : null],
              ].filter(([, v]) => v).map(([label, value]) => (
                <div key={label} className="bg-white/4 rounded-lg p-3">
                  <p className="text-[10px] text-slate-500 uppercase tracking-wide mb-1">{label}</p>
                  <p className="text-sm font-semibold text-white">{value}</p>
                </div>
              ))}
            </div>
          )}

          <button
            id="btn-view-workflow"
            className="btn-ghost w-full text-xs"
            onClick={() => navigate(`/documents/${result.workflowId}`)}
          >
            View full workflow detail →
          </button>
        </div>
      )}
    </div>
  );
}
