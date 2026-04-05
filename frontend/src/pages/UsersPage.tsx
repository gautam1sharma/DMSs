import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Lock, Unlock, Trash2, Users, MoreVertical } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { userApi } from '../api/endpoints';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface User {
  id: number; firstName: string; lastName: string; email: string;
  phone: string; roles: string[]; enabled: boolean;
  accountLocked: boolean; failedAttempts: number; createdAt: string;
}

const schema = z.object({
  firstName: z.string().min(1,'Required'),
  lastName:  z.string().min(1,'Required'),
  email:     z.string().email('Valid email required'),
  password:  z.string().min(8,'Min 8 chars').regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])/, 'Must include uppercase, digit, special char'),
  phone:     z.string().optional(),
  roles:     z.array(z.string()).min(1,'Select at least one role'),
});
type FormData = z.infer<typeof schema>;

const ROLES = ['ADMIN','DEALER','CUSTOMER'];

export default function UsersPage() {
  const [users, setUsers]       = useState<User[]>([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [search, setSearch]     = useState('');
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState(false);
  const [openMenu, setOpenMenu] = useState<number|null>(null);
  const [submitting, setSub]    = useState(false);

  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { roles: [] }
  });
  const selectedRoles = watch('roles') ?? [];

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await userApi.list(page, 10);
      setUsers(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load users'); }
    finally { setLoading(false); }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  const toggleRole = (role: string) => {
    const curr = selectedRoles;
    setValue('roles', curr.includes(role) ? curr.filter(r => r !== role) : [...curr, role]);
  };

  const onSubmit = async (data: FormData) => {
    setSub(true);
    try {
      await userApi.create(data);
      toast.success('User created');
      setModal(false);
      reset({ roles: [] });
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Error creating user');
    } finally { setSub(false); }
  };

  const toggleLock = async (u: User) => {
    try {
      if (u.accountLocked) { await userApi.unlock(u.id); toast.success('User unlocked'); }
      else { await userApi.lock(u.id); toast.success('User locked'); }
      load();
    } catch { toast.error('Failed to update lock'); }
    setOpenMenu(null);
  };

  const deleteUser = async (id: number) => {
    if (!window.confirm('Delete this user permanently?')) return;
    try { await userApi.delete(id); toast.success('User deleted'); load(); }
    catch { toast.error('Delete failed'); }
    setOpenMenu(null);
  };

  const filtered = users.filter(u =>
    `${u.firstName} ${u.lastName} ${u.email}`.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);

  return (
    <AppLayout title="Users">
      <div className="page-header">
        <div>
          <h1 className="page-title">User Management</h1>
          <p className="page-subtitle">{total} registered user{total!==1?'s':''}</p>
        </div>
        <button className="btn btn-primary" onClick={() => { reset({ roles:[] }); setModal(true); }}>
          <Plus size={16} /> New User
        </button>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={16} />
          <input type="text" placeholder="Search users..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr><th>User</th><th>Email</th><th>Roles</th><th>Status</th><th>Login Attempts</th><th>Joined</th><th style={{width:48}}></th></tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7}><div className="loading-center"><div className="spinner" /></div></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7}><div className="empty-state"><Users /><h3>No users</h3></div></td></tr>
              ) : filtered.map(u => (
                <tr key={u.id}>
                  <td>
                    <div style={{ display:'flex', alignItems:'center', gap:10 }}>
                      <div className="avatar" style={{ width:32, height:32, fontSize:'0.75rem' }}>
                        {u.firstName[0]}{u.lastName[0]}
                      </div>
                      <div style={{ fontWeight:600 }}>{u.firstName} {u.lastName}</div>
                    </div>
                  </td>
                  <td className="td-muted">{u.email}</td>
                  <td>
                    <div style={{ display:'flex', gap:4, flexWrap:'wrap' }}>
                      {u.roles.map(r => <span key={r} className={`badge badge-${r.toLowerCase()}`}>{r}</span>)}
                    </div>
                  </td>
                  <td>
                    {u.accountLocked
                      ? <span className="badge badge-cancelled">Locked</span>
                      : u.enabled
                        ? <span className="badge badge-active">Active</span>
                        : <span className="badge badge-inactive">Disabled</span>
                    }
                  </td>
                  <td>{u.failedAttempts > 0 ? <span style={{ color:'var(--warning)' }}>{u.failedAttempts}/5</span> : '0/5'}</td>
                  <td className="td-muted">{new Date(u.createdAt).toLocaleDateString('en-IN')}</td>
                  <td style={{ position:'relative' }}>
                    <button className="btn btn-ghost btn-icon" onClick={() => setOpenMenu(openMenu===u.id?null:u.id)}><MoreVertical size={16} /></button>
                    {openMenu === u.id && (
                      <div className="dropdown-menu">
                        <div className="dropdown-item" onClick={() => toggleLock(u)}>
                          {u.accountLocked ? <><Unlock size={14} /> Unlock</> : <><Lock size={14} /> Lock</>}
                        </div>
                        <div className="dropdown-divider" />
                        <div className="dropdown-item dropdown-item-danger" onClick={() => deleteUser(u.id)}><Trash2 size={14} /> Delete</div>
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
              {Array.from({length:totalPages},(_,i) => (
                <button key={i} className={`page-btn${page===i?' active':''}`} onClick={() => setPage(i)}>{i+1}</button>
              ))}
              <button className="page-btn" disabled={page>=totalPages-1} onClick={() => setPage(p=>p+1)}>›</button>
            </div>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-backdrop" onClick={() => setModal(false)}>
          <div className="modal" onClick={e => e.stopPropagation()}>
            <h2 className="modal-title">+ Create User</h2>
            <form onSubmit={handleSubmit(onSubmit)} style={{ display:'flex', flexDirection:'column', gap:16 }}>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">First Name *</label>
                  <input {...register('firstName')} className="form-input" placeholder="John" />
                  {errors.firstName && <span className="form-error">{errors.firstName.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Last Name *</label>
                  <input {...register('lastName')} className="form-input" placeholder="Doe" />
                  {errors.lastName && <span className="form-error">{errors.lastName.message}</span>}
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Email *</label>
                <input {...register('email')} type="email" className="form-input" placeholder="john@serene.com" />
                {errors.email && <span className="form-error">{errors.email.message}</span>}
              </div>
              <div className="form-group">
                <label className="form-label">Password *</label>
                <input {...register('password')} type="password" className="form-input" placeholder="Min 8 chars, uppercase, digit, special char" />
                {errors.password && <span className="form-error">{errors.password.message}</span>}
              </div>
              <div className="form-group">
                <label className="form-label">Phone</label>
                <input {...register('phone')} className="form-input" placeholder="+91 98765 43210" />
              </div>
              <div className="form-group">
                <label className="form-label">Roles *</label>
                <div style={{ display:'flex', gap:8 }}>
                  {ROLES.map(r => (
                    <button type="button" key={r}
                      className={selectedRoles.includes(r) ? 'btn btn-primary btn-sm' : 'btn btn-secondary btn-sm'}
                      onClick={() => toggleRole(r)}>{r}</button>
                  ))}
                </div>
                {errors.roles && <span className="form-error">{(errors.roles as any).message}</span>}
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Create User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
