import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, MessageSquare, Send } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { inquiryApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';

interface Inquiry {
  id: number; customerName?: string; name?: string; email?: string;
  subject: string; message: string; response?: string; status: string;
  vehicleInfo?: string; createdAt: string;
}

const statusColor: Record<string, string> = {
  OPEN: 'badge-open', IN_PROGRESS: 'badge-pending',
  RESOLVED: 'badge-completed', CLOSED: 'badge-inactive'
};

const replySchema = z.object({ response: z.string().min(5, 'Response required') });
type ReplyForm = z.infer<typeof replySchema>;

export default function InquiriesPage() {
  const { user } = useAuth();
  const dealerId = user?.dealerId ?? 0;
  const [inquiries, setInquiries] = useState<Inquiry[]>([]);
  const [total, setTotal]         = useState(0);
  const [page, setPage]           = useState(0);
  const [search, setSearch]       = useState('');
  const [loading, setLoading]     = useState(true);
  const [selected, setSelected]   = useState<Inquiry|null>(null);
  const [submitting, setSub]      = useState(false);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<ReplyForm>({
    resolver: zodResolver(replySchema)
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await inquiryApi.listByDealer(dealerId, page, 10);
      setInquiries(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load inquiries'); }
    finally { setLoading(false); }
  }, [dealerId, page]);

  useEffect(() => { load(); }, [load]);

  const onReply = async (data: ReplyForm) => {
    if (!selected) return;
    setSub(true);
    try {
      await inquiryApi.respond(selected.id, data.response);
      await inquiryApi.updateStatus(selected.id, 'RESOLVED');
      toast.success('Response sent & inquiry resolved');
      setSelected(null);
      reset();
      load();
    } catch { toast.error('Failed to send response'); }
    finally { setSub(false); }
  };

  const filtered = inquiries.filter(i =>
    `${i.name} ${i.subject} ${i.message}`.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);

  return (
    <AppLayout title="Inquiries">
      <div className="page-header">
        <div>
          <h1 className="page-title">Inquiries</h1>
          <p className="page-subtitle">{total} inquiry/inquiries received</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: selected ? '1fr 400px' : '1fr', gap: 20, transition: 'all 0.3s ease' }}>
        <div>
          <div className="filter-bar">
            <div className="search-input-wrapper">
              <Search size={16} />
              <input type="text" placeholder="Search inquiries..." value={search} onChange={e => setSearch(e.target.value)} />
            </div>
          </div>

          <div className="card">
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr><th>From</th><th>Subject</th><th>Status</th><th>Date</th><th>Action</th></tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={5}><div className="loading-center"><div className="spinner" /></div></td></tr>
                  ) : filtered.length === 0 ? (
                    <tr><td colSpan={5}>
                      <div className="empty-state"><MessageSquare /><h3>No inquiries yet</h3></div>
                    </td></tr>
                  ) : filtered.map(i => (
                    <tr key={i.id} style={{ cursor: 'pointer' }} onClick={() => { setSelected(i); reset(); }}>
                      <td>
                        <div style={{ fontWeight: 600 }}>{i.customerName || i.name || 'Anonymous'}</div>
                        <div className="td-muted">{i.email || ''}</div>
                      </td>
                      <td>
                        <div style={{ fontWeight:500, maxWidth:200 }} className="truncate">{i.subject || 'No subject'}</div>
                        <div className="td-muted truncate" style={{ maxWidth:200, fontSize:'0.78rem' }}>{i.message.slice(0,60)}...</div>
                      </td>
                      <td><span className={`badge ${statusColor[i.status] ?? 'badge-inactive'}`}>{i.status.replace('_',' ')}</span></td>
                      <td className="td-muted">{new Date(i.createdAt).toLocaleDateString('en-IN')}</td>
                      <td>
                        {i.status === 'OPEN' || i.status === 'IN_PROGRESS' ? (
                          <button className="btn btn-secondary btn-sm" onClick={(e) => { e.stopPropagation(); setSelected(i); reset(); }}>
                            <Send size={12} /> Reply
                          </button>
                        ) : (
                          <span className="td-muted text-xs">Resolved</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {totalPages > 1 && (
              <div className="pagination">
                <span className="pagination-info">{total} total</span>
                <div className="pagination-controls">
                  <button className="page-btn" disabled={page===0} onClick={() => setPage(p=>p-1)}>‹</button>
                  {Array.from({length:totalPages},(_,i) => (
                    <button key={i} className={`page-btn${page===i?' active':''}`} onClick={() => setPage(i)}>{i+1}</button>
                  ))}
                  <button className="page-btn" disabled={page>=totalPages-1} onClick={() => setPage(p=>p+1)}>›</button>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Reply panel */}
        {selected && (
          <div className="card animate-in" style={{ alignSelf:'start', position:'sticky', top: 80 }}>
            <div className="card-body">
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:20 }}>
                <h3 style={{ fontWeight:700, fontSize:'1rem' }}>Inquiry Detail</h3>
                <button className="btn btn-ghost btn-icon" onClick={() => { setSelected(null); reset(); }}>✕</button>
              </div>
              <div style={{ marginBottom:16 }}>
                <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginBottom:4 }}>FROM</div>
                <div style={{ fontWeight:600 }}>{selected.customerName || selected.name || 'Anonymous'}</div>
                <div className="td-muted">{selected.email}</div>
              </div>
              <div style={{ marginBottom:16 }}>
                <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginBottom:4 }}>SUBJECT</div>
                <div style={{ fontWeight:600 }}>{selected.subject || 'No subject'}</div>
              </div>
              <div style={{ marginBottom:20 }}>
                <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginBottom:8 }}>MESSAGE</div>
                <div style={{ background:'rgba(255,255,255,0.03)', borderRadius:8, padding:16, fontSize:'0.875rem', lineHeight:1.7, border:'1px solid var(--border-subtle)' }}>
                  {selected.message}
                </div>
              </div>
              {selected.response && (
                <div style={{ marginBottom:20 }}>
                  <div style={{ fontSize:'0.75rem', color:'var(--text-muted)', marginBottom:8 }}>PREVIOUS RESPONSE</div>
                  <div style={{ background:'rgba(99,102,241,0.08)', borderRadius:8, padding:16, fontSize:'0.875rem', lineHeight:1.7, border:'1px solid rgba(99,102,241,0.15)' }}>
                    {selected.response}
                  </div>
                </div>
              )}
              {(selected.status === 'OPEN' || selected.status === 'IN_PROGRESS') && (
                <form onSubmit={handleSubmit(onReply)}>
                  <div className="form-group" style={{ marginBottom:16 }}>
                    <label className="form-label">Your Response</label>
                    <textarea {...register('response')} className="form-textarea" placeholder="Type your response..." style={{ minHeight:120 }} />
                    {errors.response && <span className="form-error">{errors.response.message}</span>}
                  </div>
                  <button type="submit" className="btn btn-primary w-full" disabled={submitting}
                    style={{ width:'100%', justifyContent:'center' }}>
                    <Send size={15} />
                    {submitting ? 'Sending...' : 'Send & Resolve'}
                  </button>
                </form>
              )}
            </div>
          </div>
        )}
      </div>
    </AppLayout>
  );
}
