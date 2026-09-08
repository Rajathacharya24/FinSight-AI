import React, { useState, useEffect } from 'react';
import client from '../api/client';
import { Bot, Send, FileText, Sparkles, Database, ArrowRight } from 'lucide-react';

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
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Bot className="w-7 h-7 text-indigo-400" />
          RAG Vector Intelligence Assistant
        </h1>
        <p className="text-xs text-slate-400 mt-1">Semantic search & context-augmented document Q&A powered by pgvector & Spring AI</p>
      </div>

      {/* Document Selector Header */}
      <div className="p-4 bg-slate-900/80 border border-slate-800 rounded-2xl flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-indigo-600/20 border border-indigo-500/30 rounded-xl text-indigo-400">
            <FileText className="w-5 h-5" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-300">Target Document for Query</label>
            <p className="text-[11px] text-slate-500">Vector embeddings generated and stored in pgvector</p>
          </div>
        </div>
        <select
          value={selectedDocId}
          onChange={(e) => setSelectedDocId(e.target.value)}
          className="w-full sm:w-72 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-indigo-500"
        >
          {documents.map((doc) => (
            <option key={doc.id} value={doc.id}>
              {doc.fileName} ({doc.id.substring(0, 8)}...)
            </option>
          ))}
        </select>
      </div>

      {/* Chat Conversation Box */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 h-[500px] flex flex-col justify-between shadow-2xl">
        <div className="overflow-y-auto space-y-4 pr-2 flex-1">
          {chatHistory.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-center p-8 text-slate-500">
              <Sparkles className="w-10 h-10 text-indigo-500/40 mb-3 animate-pulse" />
              <p className="text-sm font-semibold text-slate-400">Ask any question about the selected document</p>
              <p className="text-xs text-slate-500 mt-1 max-w-sm">
                Example: "What is the borrower's annual income and requested loan amount?" or "Are there compliance risk factors?"
              </p>
            </div>
          ) : (
            chatHistory.map((item, idx) => (
              <div key={idx} className={`flex flex-col ${item.sender === 'user' ? 'items-end' : 'items-start'}`}>
                <div
                  className={`max-w-2xl rounded-2xl p-4 text-xs leading-relaxed ${
                    item.sender === 'user'
                      ? 'bg-indigo-600 text-white rounded-br-none shadow-lg shadow-indigo-600/20'
                      : 'bg-slate-950 border border-slate-800 text-slate-200 rounded-bl-none shadow-md'
                  }`}
                >
                  {item.sender === 'user' ? (
                    <p>{item.text}</p>
                  ) : (
                    <div>
                      <p className="font-medium whitespace-pre-wrap">{item.answer}</p>
                      {item.sources && item.sources.length > 0 && (
                        <div className="mt-3 pt-3 border-t border-slate-800/80">
                          <p className="text-[10px] font-bold text-indigo-400 uppercase tracking-wider mb-1 flex items-center gap-1">
                            <Database className="w-3 h-3" /> pgvector Context Chunks
                          </p>
                          <div className="space-y-1">
                            {item.sources.map((src, sIdx) => (
                              <p key={sIdx} className="text-[10px] text-slate-400 bg-slate-900 p-1.5 rounded border border-slate-800 truncate">
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
            <div className="flex items-center space-x-2 text-xs text-indigo-400 p-2">
              <Sparkles className="w-4 h-4 animate-spin" />
              <span>Retrieving vector embeddings & executing Groq LLM RAG prompt...</span>
            </div>
          )}
        </div>

        {/* Input Form */}
        <form onSubmit={handleQuery} className="mt-4 pt-4 border-t border-slate-800 flex gap-2">
          <input
            type="text"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="Ask a question about loan terms, borrower income, compliance..."
            className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-4 py-3 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
          <button
            type="submit"
            disabled={loading || !question.trim()}
            className="px-5 py-3 bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-semibold rounded-xl shadow-lg shadow-indigo-600/30 flex items-center space-x-1 disabled:opacity-50 transition-all"
          >
            <span>Ask RAG</span>
            <Send className="w-3.5 h-3.5" />
          </button>
        </form>
      </div>
    </div>
  );
};
