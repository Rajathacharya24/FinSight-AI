import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import client from '../api/client';
import {
  FileText,
  UploadCloud,
  Search,
  Download,
  Eye,
  GitMerge,
  CheckCircle2,
  AlertCircle,
  FileCheck2,
  X,
  Loader2,
} from 'lucide-react';

const statusStyles = (status = '') => {
  const value = status.toUpperCase();
  if (value.includes('FAIL') || value.includes('ERROR')) {
    return 'border-danger-500/25 bg-danger-500/10 text-danger-300';
  }
  if (value.includes('PROCESS') || value.includes('PEND') || value.includes('REVIEW')) {
    return 'border-warning-500/25 bg-warning-500/10 text-warning-300';
  }
  return 'border-success-500/25 bg-success-500/10 text-success-400';
};

export const DocumentsPage = () => {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [search, setSearch] = useState('');
  const [selectedDocText, setSelectedDocText] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchDocuments = async () => {
    try {
      setLoading(true);
      const res = await client.get('/documents');
      setDocuments(res.data || []);
    } catch (err) {
      console.error('Failed to fetch documents', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDocuments();
  }, []);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.pdf')) {
      setError('Only PDF files are supported.');
      return;
    }

    const formData = new FormData();
    formData.append('file', file);

    setUploading(true);
    setError(null);

    try {
      await client.post('/documents/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      fetchDocuments();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document.');
    } finally {
      setUploading(false);
    }
  };

  const viewContent = async (docId) => {
    try {
      const res = await client.get(`/documents/${docId}/content`);
      setSelectedDocText(res.data);
    } catch (err) {
      setError('Could not load document text content.');
    }
  };

  const downloadDoc = (docId, fileName) => {
    client.get(`/documents/${docId}/download`, { responseType: 'blob' })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', fileName);
        document.body.appendChild(link);
        link.click();
      });
  };

  const filteredDocs = documents.filter(d =>
    d.fileName.toLowerCase().includes(search.toLowerCase()) ||
    d.id.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-end">
        <div>
          <p className="eyebrow mb-1.5 flex items-center gap-1.5">
            <FileCheck2 className="h-3.5 w-3.5 text-brand-400" /> Document workspace
          </p>
          <h1 className="section-title">Financial Document Portal</h1>
          <p className="section-subtitle">
            Upload loan application PDFs to trigger Kafka microservice pipelines.
          </p>
        </div>

        <label className="btn-primary cursor-pointer self-start text-xs sm:self-auto">
          {uploading ? <Loader2 className="h-4 w-4 animate-spin" /> : <UploadCloud className="h-4 w-4" />}
          <span>{uploading ? 'Processing PDF…' : 'Upload Financial PDF'}</span>
          <input type="file" accept=".pdf" onChange={handleFileUpload} className="hidden" disabled={uploading} />
        </label>
      </div>

      {error && (
        <div className="flex items-center gap-2 rounded-xl border border-danger-500/30 bg-danger-500/10 px-4 py-3 text-xs text-danger-300">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span className="flex-1">{error}</span>
          <button onClick={() => setError(null)} className="text-danger-400 hover:text-white">
            <X className="h-4 w-4" />
          </button>
        </div>
      )}

      {/* Toolbar */}
      <div className="flex flex-col items-stretch justify-between gap-4 rounded-2xl border border-white/[0.08] bg-dark-850/60 p-4 sm:flex-row sm:items-center">
        <div className="relative w-full sm:w-96">
          <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-500" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by file name or document ID…"
            className="form-input py-2 pl-10 text-xs"
          />
        </div>
        <div className="flex items-center gap-2 text-xs text-slate-400">
          <span className="chip border-white/[0.08] bg-white/[0.03] text-slate-400">
            Total documents
            <span className="font-bold text-brand-300">{documents.length}</span>
          </span>
        </div>
      </div>

      {/* Table */}
      <div className="table-shell">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="table-head">
              <tr>
                <th className="p-4 font-semibold">Document</th>
                <th className="p-4 font-semibold">Document ID</th>
                <th className="p-4 font-semibold">File Size</th>
                <th className="p-4 font-semibold">Status</th>
                <th className="p-4 font-semibold">Uploaded At</th>
                <th className="p-4 text-right font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/[0.04] text-slate-300">
              {loading ? (
                <tr>
                  <td colSpan="6" className="p-10 text-center text-slate-500">
                    <Loader2 className="mx-auto mb-2 h-5 w-5 animate-spin text-brand-400" />
                    Loading documents…
                  </td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-10 text-center text-slate-500">
                    No documents found. Upload a financial PDF to begin.
                  </td>
                </tr>
              ) : (
                filteredDocs.map((doc) => (
                  <tr key={doc.id} className="trow">
                    <td className="p-4">
                      <div className="flex items-center gap-3">
                        <div className="icon-tile h-10 w-10 shrink-0 border-brand-400/20 bg-brand-500/10 text-brand-300">
                          <FileText className="h-5 w-5" />
                        </div>
                        <div className="min-w-0">
                          <p className="max-w-xs truncate font-semibold text-white">{doc.fileName}</p>
                          <p className="mt-0.5 text-[10px] text-slate-500">{doc.contentType}</p>
                        </div>
                      </div>
                    </td>
                    <td className="p-4 font-mono text-[11px] text-slate-400">{doc.id.substring(0, 12)}…</td>
                    <td className="p-4 font-medium">{(doc.fileSize / 1024).toFixed(1)} KB</td>
                    <td className="p-4">
                      <span className={`chip ${statusStyles(doc.status)}`}>
                        <CheckCircle2 className="h-3 w-3" />
                        {doc.status}
                      </span>
                    </td>
                    <td className="p-4 text-slate-400">{new Date(doc.createdAt).toLocaleString()}</td>
                    <td className="p-4">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => viewContent(doc.id)} title="View extracted text" className="btn-icon">
                          <Eye className="h-4 w-4" />
                        </button>
                        <button onClick={() => downloadDoc(doc.id, doc.fileName)} title="Download PDF" className="btn-icon">
                          <Download className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => navigate(`/workflows?docId=${doc.id}`)}
                          title="Track workflow pipeline"
                          className="btn-icon border-brand-400/30 bg-brand-500/15 text-brand-300 hover:bg-brand-500/25"
                        >
                          <GitMerge className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Extracted text modal */}
      {selectedDocText && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">
          <div className="flex max-h-[80vh] w-full max-w-2xl flex-col overflow-hidden rounded-2xl border border-white/10 bg-dark-850 shadow-glass">
            <div className="flex items-center justify-between border-b border-white/[0.08] p-4">
              <div>
                <h3 className="text-sm font-bold text-white">Extracted Document Text</h3>
                <p className="text-[11px] text-slate-400">
                  {selectedDocText.wordCount} words · {selectedDocText.charCount} characters
                </p>
              </div>
              <button onClick={() => setSelectedDocText(null)} className="btn-icon">
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="overflow-y-auto whitespace-pre-wrap bg-dark-950/70 p-4 font-mono text-xs leading-relaxed text-slate-300">
              {selectedDocText.extractedText || 'No text extracted.'}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
