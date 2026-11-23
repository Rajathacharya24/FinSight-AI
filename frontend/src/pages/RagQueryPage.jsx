import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Bot, Send, FileText, Sparkles, Database, Loader2 } from 'lucide-react';

export const RagQueryPage = () => {
  const [documents, setDocuments] = useState([]);
  const [selectedDocId, setSelectedDocId] = useState('');
  const [question, setQuestion] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    client.get('/documents')
      .then((res) => {
        const docs = res.data || [];
        setDocuments(docs);
        if (docs.length > 0) {
          setSelectedDocId(docs[0].id);
        }
      })
      .catch(console.error);
  }, []);

  const handleQuery = async (e) => {
    e.preventDefault();
    if (!question.trim() || !selectedDocId) return;

    const userQ = question;
    setQuestion('');
    setChatHistory((prev) => [...prev, { sender: 'user', text: userQ }]);
    setLoading(true);

    try {
      const res = await client.post(`/ai/documents/${selectedDocId}/query`, { question: userQ });
      setChatHistory((prev) => [
        ...prev,
        {
          sender: 'ai',
          answer: res.data.answer,
          sources: res.data.relevantSources || [],
        },
      ]);
    } catch (err) {
      setChatHistory((prev) => [
        ...prev,
        { sender: 'ai', answer: 'Failed to process RAG query against document vector embeddings.', sources: [] },
      ]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-auto max-w-5xl space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <p className="eyebrow mb-1.5 flex items-center gap-1.5">
          <Bot className="h-3.5 w-3.5 text-brand-400" /> Retrieval augmented generation
        </p>
        <h1 className="section-title">RAG Vector Intelligence Assistant</h1>
        <p className="section-subtitle">
          Semantic search and context-augmented document Q&amp;A powered by pgvector and Spring AI.
        </p>
      </div>

      {/* Document selector */}
      <div className="panel flex flex-col items-stretch justify-between gap-4 p-4 sm:flex-row sm:items-center">
        <div className="flex items-center gap-3">
          <div className="icon-tile h-10 w-10 border-brand-400/20 bg-brand-500/10 text-brand-300">
            <FileText className="h-5 w-5" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-200">Target document</label>
            <p className="text-[11px] text-slate-500">Vector embeddings generated and stored in pgvector</p>
          </div>
        </div>
        <select
          value={selectedDocId}
          onChange={(e) => setSelectedDocId(e.target.value)}
          className="form-select sm:w-72"
        >
          {documents.map((doc) => (
            <option key={doc.id} value={doc.id}>
              {doc.fileName} ({doc.id.substring(0, 8)}…)
            </option>
          ))}
        </select>
      </div>

      {/* Chat */}
      <div className="panel flex h-[540px] flex-col justify-between p-5 shadow-glass">
        <div className="flex-1 space-y-4 overflow-y-auto pr-2">
          {chatHistory.length === 0 ? (
            <div className="flex h-full flex-col items-center justify-center p-8 text-center">
              <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl border border-brand-400/25 bg-brand-500/10">
                <Sparkles className="h-6 w-6 animate-pulse text-brand-300" />
              </div>
              <p className="text-sm font-semibold text-slate-200">Ask anything about the selected document</p>
              <p className="mt-1 max-w-sm text-xs leading-relaxed text-slate-500">
                Example: “What is the borrower's annual income and requested loan amount?” or
                “Are there compliance risk factors?”
              </p>
            </div>
          ) : (
            chatHistory.map((item, idx) => (
              <div
                key={idx}
                className={`flex flex-col ${item.sender === 'user' ? 'items-end' : 'items-start'}`}
              >
                {item.sender === 'ai' && (
                  <div className="mb-1 flex items-center gap-1.5 text-[10px] font-semibold uppercase tracking-wider text-brand-300">
                    <Bot className="h-3 w-3" /> FinSight AI
                  </div>
                )}
                <div
                  className={`max-w-2xl rounded-2xl px-4 py-3 text-xs leading-relaxed ${
                    item.sender === 'user'
                      ? 'rounded-br-md bg-gradient-brand text-white shadow-glow-brand'
                      : 'rounded-bl-md border border-white/[0.08] bg-dark-950/70 text-slate-200'
                  }`}
                >
                  {item.sender === 'user' ? (
                    <p>{item.text}</p>
                  ) : (
                    <div>
                      <p className="whitespace-pre-wrap font-medium">{item.answer}</p>
                      {item.sources && item.sources.length > 0 && (
                        <div className="mt-3 border-t border-white/[0.08] pt-3">
                          <p className="mb-1.5 flex items-center gap-1 text-[10px] font-bold uppercase tracking-wider text-brand-300">
                            <Database className="h-3 w-3" /> pgvector context chunks
                          </p>
                          <div className="space-y-1">
                            {item.sources.map((src, sIdx) => (
                              <p
                                key={sIdx}
                                className="truncate rounded-md border border-white/[0.06] bg-dark-900/80 p-1.5 text-[10px] text-slate-400"
                              >
                                {src}
                              </p>
                            ))}
                          </div>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </div>
            ))
          )}

          {loading && (
            <div className="flex items-center gap-2 rounded-xl border border-brand-400/20 bg-brand-500/[0.06] p-3 text-xs text-brand-300">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Retrieving vector embeddings and executing the Groq LLM RAG prompt…</span>
            </div>
          )}
        </div>

        {/* Composer */}
        <form onSubmit={handleQuery} className="mt-4 flex gap-2 border-t border-white/[0.08] pt-4">
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask a question about loan terms, borrower income, compliance…"
            className="form-input flex-1 text-xs"
          />
          <button
            type="submit"
            disabled={loading || !question.trim()}
            className="btn-primary px-5 py-2.5 text-xs"
          >
            <span>Ask RAG</span>
            <Send className="h-3.5 w-3.5" />
          </button>
        </form>
      </div>
    </div>
  );
};
