import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Edit2, Trash2, MoreVertical, Users } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { customerApi, dealerApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface Customer {
  id: number; dealerId: number; dealerName: string;
  firstName: string; lastName: string; email: string;
  phone: string; city: string; state: string; status: string;
  createdAt: string;
}

const schema = z.object({
  dealerId:  z.coerce.number().positive('Dealer required'),
  firstName: z.string().min(1, 'Required'),
  lastName:  z.string().min(1, 'Required'),
  email:     z.string().email().optional().or(z.literal('')),
  phone:     z.string().optional(),
  city:      z.string().optional(),
  state:     z.string().optional(),
  address:   z.string().optional(),
  notes:     z.string().optional(),
});
type FormData = z.infer<typeof schema>;

export default function CustomersPage() {
  const { user, isAdmin } = useAuth();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [dealers, setDealers]     = useState<any[]>([]);
  const [total, setTotal]         = useState(0);
  const [page, setPage]           = useState(0);
  const [search, setSearch]       = useState('');
  const [loading, setLoading]     = useState(true);
  const [modal, setModal]         = useState<'create'|'edit'|null>(null);
  const [editing, setEditing]     = useState<Customer|null>(null);
  const [openMenu, setOpenMenu]   = useState<number|null>(null);
  const [submitting, setSub]      = useState(false);

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const dealerId = isAdmin ? undefined : user?.dealerId;

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = dealerId
        ? await customerApi.listByDealer(dealerId, page, 10)
        : await customerApi.listAll(page, 10);
      setCustomers(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load customers'); }
    finally { setLoading(false); }
  }, [dealerId, page]);

  const loadDealers = useCallback(async () => {
    if (isAdmin) {
      try {
        const res = await dealerApi.list(0, 100);
        setDealers(res.data.content);
      } catch {}
    }
  }, [isAdmin]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { loadDealers(); }, [loadDealers]);

  const openCreate = () => {
    reset({ dealerId: dealerId ?? 0, firstName:'', lastName:'', email:'', phone:'', city:'', state:'', address:'', notes:'' });
    setEditing(null);
    setModal('create');
  };
  const openEdit = (c: Customer) => {
    reset({ dealerId: c.dealerId, firstName: c.firstName, lastName: c.lastName,
      email: c.email, phone: c.phone, city: c.city, state: c.state });
    setEditing(c);
    setModal('edit');
  };

  const onSubmit = async (data: FormData) => {
    setSub(true);
    try {
      if (modal === 'create') {
        await customerApi.create(data);
        toast.success('Customer added');
      } else if (editing) {
        await customerApi.update(editing.id, data);
        toast.success('Customer updated');
      }
      setModal(null);
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Error saving customer');
    } finally { setSub(false); }
  };

  const deleteCustomer = async (id: number) => {
    if (!window.confirm('Delete this customer?')) return;
    try { await customerApi.delete(id); toast.success('Customer deleted'); load(); }
    catch { toast.error('Delete failed'); }
    setOpenMenu(null);
  };

  const filtered = customers.filter(c =>
    `${c.firstName} ${c.lastName} ${c.email}`.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);

  return (
    <AppLayout title="Customers">
      <div className="page-header">
        <div>
          <h1 className="page-title">Customers</h1>
          <p className="page-subtitle">{total} customer{total !== 1 ? 's' : ''} managed</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Customer</button>
        </div>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={16} />
          <input type="text" placeholder="Search by name or email..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Customer</th>
                <th>Contact</th>
                <th>Location</th>
                {isAdmin && <th>Dealer</th>}
                <th>Status</th>
                <th>Added</th>
                <th style={{ width: 48 }}></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7}><div className="loading-center"><div className="spinner" /></div></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7}>
                  <div className="empty-state">
                    <Users />
                    <h3>No customers yet</h3>
                    <p>Add your first customer to get started</p>
                  </div>
                </td></tr>
              ) : filtered.map(c => (
                <tr key={c.id}>
                  <td>
                    <div style={{ display:'flex', alignItems:'center', gap: 10 }}>
                      <div className="avatar" style={{ width:32, height:32, fontSize:'0.75rem', background:'var(--brand-700)' }}>
                        {c.firstName[0]}{c.lastName[0]}
                      </div>
                      <div>
                        <div style={{ fontWeight: 600 }}>{c.firstName} {c.lastName}</div>
                      </div>
                    </div>
                  </td>
                  <td><div className="td-muted">{c.email || '—'}</div><div className="td-muted">{c.phone || '—'}</div></td>
                  <td className="td-muted">{[c.city, c.state].filter(Boolean).join(', ') || '—'}</td>
                  {isAdmin && <td className="td-muted">{c.dealerName}</td>}
                  <td><span className={`badge badge-${c.status.toLowerCase()}`}>{c.status}</span></td>
                  <td className="td-muted">{new Date(c.createdAt).toLocaleDateString('en-IN')}</td>
                  <td style={{ position: 'relative' }}>
                    <button className="btn btn-ghost btn-icon" onClick={() => setOpenMenu(openMenu === c.id ? null : c.id)}>
                      <MoreVertical size={16} />
                    </button>
                    {openMenu === c.id && (
                      <div className="dropdown-menu">
                        <div className="dropdown-item" onClick={() => { openEdit(c); setOpenMenu(null); }}>
                          <Edit2 size={14} /> Edit
                        </div>
                        <div className="dropdown-divider" />
                        <div className="dropdown-item dropdown-item-danger" onClick={() => deleteCustomer(c.id)}>
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

        {totalPages > 1 && (
          <div className="pagination">
            <span className="pagination-info">Showing {filtered.length} of {total}</span>
            <div className="pagination-controls">
              <button className="page-btn" disabled={page===0} onClick={() => setPage(p=>p-1)}>‹</button>
              {Array.from({ length: totalPages }, (_,i) => (
                <button key={i} className={`page-btn${page===i?' active':''}`} onClick={() => setPage(i)}>{i+1}</button>
              ))}
              <button className="page-btn" disabled={page>=totalPages-1} onClick={() => setPage(p=>p+1)}>›</button>
            </div>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <h2 className="modal-title">{modal === 'create' ? '+ Add Customer' : 'Edit Customer'}</h2>
            <form onSubmit={handleSubmit(onSubmit)} style={{ display:'flex', flexDirection:'column', gap:16 }}>
              {isAdmin && (
                <div className="form-group">
                  <label className="form-label">Dealer *</label>
                  <select {...register('dealerId')} className="form-select">
                    <option value="">Select dealer...</option>
                    {dealers.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
                  </select>
                  {errors.dealerId && <span className="form-error">{errors.dealerId.message}</span>}
                </div>
              )}
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">First Name *</label>
                  <input {...register('firstName')} className="form-input" placeholder="Ravi" />
                  {errors.firstName && <span className="form-error">{errors.firstName.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name *</label>
                  <input {...register('lastName')} className="form-input" placeholder="Kumar" />
                  {errors.lastName && <span className="form-error">{errors.lastName.message}</span>}
                </div>
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Email</label>
                  <input {...register('email')} type="email" className="form-input" placeholder="ravi@example.com" />
                </div>
                <div className="form-group">
                  <label className="form-label">Phone</label>
                  <input {...register('phone')} className="form-input" placeholder="+91 98765 43210" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Address</label>
                <input {...register('address')} className="form-input" placeholder="Street address" />
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">City</label>
                  <input {...register('city')} className="form-input" placeholder="Mumbai" />
                </div>
                <div className="form-group">
                  <label className="form-label">State</label>
                  <input {...register('state')} className="form-input" placeholder="Maharashtra" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Notes</label>
                <textarea {...register('notes')} className="form-textarea" placeholder="Any notes about this customer..." style={{ minHeight: 80 }} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : modal === 'create' ? 'Add Customer' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
