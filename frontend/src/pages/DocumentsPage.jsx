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
  Clock, 
  AlertCircle,
  FileCheck2
} from 'lucide-react';

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
      alert('Could not load document text content.');
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
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <FileCheck2 className="w-7 h-7 text-indigo-400" />
            Financial Document Portal
          </h1>
          <p className="text-xs text-slate-400 mt-1">Upload loan application PDFs to trigger Kafka microservice pipelines</p>
        </div>

        {/* Upload Button */}
        <label className="cursor-pointer inline-flex items-center space-x-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-indigo-600/30 transition-all hover:scale-[1.02]">
          <UploadCloud className="w-4 h-4" />
          <span>{uploading ? 'Processing PDF...' : 'Upload Financial PDF'}</span>
          <input type="file" accept=".pdf" onChange={handleFileUpload} className="hidden" disabled={uploading} />
        </label>
      </div>

      {error && (
        <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-rose-400 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4" />
          <span>{error}</span>
        </div>
      )}

      {/* Search Bar & Stats */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 bg-slate-900/60 p-4 border border-slate-800 rounded-2xl">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by file name or document ID..."
            className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-xs text-white focus:outline-none focus:border-indigo-500"
          />
        </div>
        <div className="text-xs text-slate-400 font-medium">
          Total Processed Documents: <span className="text-indigo-400 font-bold">{documents.length}</span>
        </div>
      </div>

      {/* Documents Table */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950 text-slate-400 border-b border-slate-800 uppercase font-semibold">
              <tr>
                <th className="p-4">Document</th>
                <th className="p-4">Document ID</th>
                <th className="p-4">File Size</th>
                <th className="p-4">Status</th>
                <th className="p-4">Uploaded At</th>
                <th className="p-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60 text-slate-300">
              {loading ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-slate-500">Loading documents...</td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan="6" className="p-8 text-center text-slate-500">No documents found. Upload a financial PDF to begin.</td>
                </tr>
              ) : (
                filteredDocs.map((doc) => (
                  <tr key={doc.id} className="hover:bg-slate-850/50 transition-colors">
                    <td className="p-4 flex items-center space-x-3">
                      <div className="p-2 bg-indigo-500/10 border border-indigo-500/20 rounded-lg text-indigo-400">
                        <FileText className="w-5 h-5" />
                      </div>
                      <div>
                        <p className="font-semibold text-white truncate max-w-xs">{doc.fileName}</p>
                        <p className="text-[10px] text-slate-500">{doc.contentType}</p>
                      </div>
                    </td>
                    <td className="p-4 font-mono text-[11px] text-slate-400">{doc.id.substring(0, 12)}...</td>
                    <td className="p-4 font-medium">{(doc.fileSize / 1024).toFixed(1)} KB</td>
                    <td className="p-4">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        <CheckCircle2 className="w-3 h-3 mr-1" />
                        {doc.status}
                      </span>
                    </td>
                    <td className="p-4 text-slate-400">{new Date(doc.createdAt).toLocaleString()}</td>
                    <td className="p-4 text-right space-x-2">
                      <button
                        onClick={() => viewContent(doc.id)}
                        title="View Extracted Text"
                        className="p-1.5 bg-slate-800 hover:bg-slate-750 text-slate-300 rounded-lg transition-colors"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => downloadDoc(doc.id, doc.fileName)}
                        title="Download PDF"
                        className="p-1.5 bg-slate-800 hover:bg-slate-750 text-slate-300 rounded-lg transition-colors"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => navigate(`/workflows?docId=${doc.id}`)}
                        title="Track Workflow Pipeline"
                        className="p-1.5 bg-indigo-600/20 hover:bg-indigo-600/40 text-indigo-300 border border-indigo-500/30 rounded-lg transition-colors"
                      >
                        <GitMerge className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Raw Extracted Text Viewer Modal */}
      {selectedDocText && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-2xl max-h-[80vh] flex flex-col shadow-2xl">
            <div className="p-4 border-b border-slate-800 flex justify-between items-center">
              <div>
                <h3 className="text-sm font-bold text-white">Extracted Document Text (Apache PDFBox)</h3>
                <p className="text-[11px] text-slate-400">{selectedDocText.wordCount} words | {selectedDocText.charCount} characters</p>
              </div>
              <button
                onClick={() => setSelectedDocText(null)}
                className="text-slate-400 hover:text-white text-xs px-2 py-1 bg-slate-800 rounded-lg"
              >
                Close
              </button>
            </div>
            <div className="p-4 overflow-y-auto font-mono text-xs text-slate-300 bg-slate-950 rounded-b-2xl whitespace-pre-wrap">
              {selectedDocText.extractedText || 'No text extracted.'}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
