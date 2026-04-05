import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, MoreVertical, Building2, Edit2, Trash2, CheckCircle, XCircle, AlertCircle } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { dealerApi } from '../api/endpoints';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface Dealer {
  id: number; name: string; code: string; address: string;
  city: string; state: string; phone: string; email: string;
  status: string; ownerName?: string; customerCount: number;
  vehicleCount: number; createdAt: string;
}

const schema = z.object({
  name:  z.string().min(2, 'Name required'),
  code:  z.string().min(3).max(20).regex(/^[A-Z0-9_-]+$/, 'Uppercase alphanumeric only'),
  email: z.string().email().optional().or(z.literal('')),
  phone: z.string().optional(),
  city:  z.string().optional(),
  state: z.string().optional(),
  address: z.string().optional(),
  userId: z.coerce.number().optional(),
});
type FormData = z.infer<typeof schema>;

const statusColor: Record<string, string> = {
  ACTIVE: 'badge-active', INACTIVE: 'badge-inactive', SUSPENDED: 'badge-suspended'
};

export default function DealersPage() {
  const [dealers, setDealers]   = useState<Dealer[]>([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [search, setSearch]     = useState('');
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState<'create'|'edit'|null>(null);
  const [editing, setEditing]   = useState<Dealer|null>(null);
  const [openMenu, setOpenMenu] = useState<number|null>(null);
  const [submitting, setSub]    = useState(false);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await dealerApi.list(page, 10);
      setDealers(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load dealers'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => {
    reset({ name:'', code:'', email:'', phone:'', city:'', state:'', address:'', userId: undefined });
    setEditing(null);
    setModal('create');
  };
  const openEdit = (d: Dealer) => {
    reset({ name: d.name, code: d.code, email: d.email, phone: d.phone, city: d.city, state: d.state, address: d.address });
    setEditing(d);
    setModal('edit');
  };

  const onSubmit = async (data: FormData) => {
    setSub(true);
    try {
      if (modal === 'create') {
        await dealerApi.create(data);
        toast.success('Dealer created');
      } else if (editing) {
        await dealerApi.update(editing.id, data);
        toast.success('Dealer updated');
      }
      setModal(null);
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Error saving dealer');
    } finally { setSub(false); }
  };

  const deleteDealer = async (id: number) => {
    if (!window.confirm('Delete this dealer? This cannot be undone.')) return;
    try {
      await dealerApi.delete(id);
      toast.success('Dealer deleted');
      load();
    } catch { toast.error('Failed to delete dealer'); }
    setOpenMenu(null);
  };

  const changeStatus = async (id: number, status: string) => {
    try {
      await dealerApi.updateStatus(id, status);
      toast.success('Status updated');
      load();
    } catch { toast.error('Failed to update status'); }
    setOpenMenu(null);
  };

  const filtered = dealers.filter(d =>
    d.name.toLowerCase().includes(search.toLowerCase()) ||
    d.code.toLowerCase().includes(search.toLowerCase()) ||
    d.city?.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);

  return (
    <AppLayout title="Dealers">
      <div className="page-header">
        <div>
          <h1 className="page-title">Dealers</h1>
          <p className="page-subtitle">{total} dealer{total !== 1 ? 's' : ''} registered on Serene platform</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={openCreate}>
            <Plus size={16} /> New Dealer
          </button>
        </div>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={16} />
          <input type="text" placeholder="Search dealers..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Dealer</th>
                <th>Code</th>
                <th>Location</th>
                <th>Contact</th>
                <th>Customers</th>
                <th>Vehicles</th>
                <th>Status</th>
                <th style={{ width: 48 }}></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={8}><div className="loading-center"><div className="spinner" /></div></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={8}>
                  <div className="empty-state">
                    <Building2 />
                    <h3>No dealers yet</h3>
                    <p>Create your first dealer to get started</p>
                  </div>
                </td></tr>
              ) : filtered.map(d => (
                <tr key={d.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>{d.name}</div>
                    {d.ownerName && <div className="td-muted">{d.ownerName}</div>}
                  </td>
                  <td><span style={{ fontFamily: 'monospace', fontSize: '0.82rem', color: 'var(--brand-400)' }}>{d.code}</span></td>
                  <td className="td-muted">{[d.city, d.state].filter(Boolean).join(', ') || '—'}</td>
                  <td className="td-muted">{d.phone || d.email || '—'}</td>
                  <td style={{ fontWeight: 600 }}>{d.customerCount}</td>
                  <td style={{ fontWeight: 600 }}>{d.vehicleCount}</td>
                  <td><span className={`badge ${statusColor[d.status] ?? 'badge-inactive'}`}>{d.status}</span></td>
                  <td style={{ position: 'relative' }}>
                    <button className="btn btn-ghost btn-icon" onClick={() => setOpenMenu(openMenu === d.id ? null : d.id)}>
                      <MoreVertical size={16} />
                    </button>
                    {openMenu === d.id && (
                      <div className="dropdown-menu" style={{ right: 0, top: '100%', width: 190 }}>
                        <div className="dropdown-item" onClick={() => { openEdit(d); setOpenMenu(null); }}>
                          <Edit2 size={14} /> Edit
                        </div>
                        <div className="dropdown-item" onClick={() => changeStatus(d.id, d.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE')}>
                          {d.status === 'ACTIVE' ? <><XCircle size={14} /> Deactivate</> : <><CheckCircle size={14} /> Activate</>}
                        </div>
                        <div className="dropdown-item" onClick={() => changeStatus(d.id, 'SUSPENDED')}>
                          <AlertCircle size={14} /> Suspend
                        </div>
                        <div className="dropdown-divider" />
                        <div className="dropdown-item dropdown-item-danger" onClick={() => deleteDealer(d.id)}>
                          <Trash2 size={14} /> Delete
                        </div>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <span className="pagination-info">Showing {filtered.length} of {total}</span>
            <div className="pagination-controls">
              <button className="page-btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>‹</button>
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i} className={`page-btn${page === i ? ' active' : ''}`} onClick={() => setPage(i)}>{i+1}</button>
              ))}
              <button className="page-btn" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>›</button>
            </div>
          </div>
        )}
      </div>

      {/* Modal */}
      {modal && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2 className="modal-title">{modal === 'create' ? '+ New Dealer' : 'Edit Dealer'}</h2>
            <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Dealer Name *</label>
                  <input {...register('name')} className="form-input" placeholder="Serene Auto Delhi" />
                  {errors.name && <span className="form-error">{errors.name.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Code *</label>
                  <input {...register('code')} className="form-input" placeholder="DEL-001"
                    style={{ textTransform: 'uppercase' }} />
                  {errors.code && <span className="form-error">{errors.code.message}</span>}
                </div>
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input {...register('email')} type="email" className="form-input" placeholder="delhi@serene.com" />
                  {errors.email && <span className="form-error">{errors.email.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Phone</label>
                  <input {...register('phone')} className="form-input" placeholder="+91 98765 43210" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Address</label>
                <input {...register('address')} className="form-input" placeholder="123 Main Street" />
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">City</label>
                  <input {...register('city')} className="form-input" placeholder="New Delhi" />
                </div>
                <div className="form-group">
                  <label className="form-label">State</label>
                  <input {...register('state')} className="form-input" placeholder="Delhi" />
                </div>
              </div>
              {modal === 'create' && (
                <div className="form-group">
                  <label className="form-label">Owner User ID (optional)</label>
                  <input {...register('userId')} type="number" className="form-input" placeholder="Link to existing user" />
                </div>
              )}
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : modal === 'create' ? 'Create Dealer' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
