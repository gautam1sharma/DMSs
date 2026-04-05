import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Car, Edit2, Trash2, MoreVertical } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { vehicleApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface Vehicle {
  id: number; model: string; variant: string; vin: string; year: number;
  color: string; price: number; fuelType: string; transmission: string;
  status: string; dealerName: string;
}

const schema = z.object({
  dealerId:     z.coerce.number().positive(),
  model:        z.string().min(1, 'Model required'),
  variant:      z.string().optional(),
  vin:          z.string().max(17).optional().or(z.literal('')),
  year:         z.coerce.number().min(2000).max(2030),
  color:        z.string().optional(),
  price:        z.coerce.number().positive('Price required'),
  fuelType:     z.string().optional(),
  transmission: z.string().optional(),
  description:  z.string().optional(),
});
type FormData = z.infer<typeof schema>;

const statusColor: Record<string, string> = {
  AVAILABLE: 'badge-active', RESERVED: 'badge-pending', SOLD: 'badge-completed'
};

const FUEL_TYPES = ['Petrol', 'Diesel', 'Electric', 'Hybrid', 'CNG'];
const TRANSMISSIONS = ['Automatic', 'Manual', 'AMT', 'CVT'];

export default function VehiclesPage() {
  const { user } = useAuth();
  const dealerId = user?.dealerId ?? 0;
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [search, setSearch]     = useState('');
  const [loading, setLoading]   = useState(true);
  const [modal, setModal]       = useState<'create'|'edit'|null>(null);
  const [editing, setEditing]   = useState<Vehicle|null>(null);
  const [openMenu, setOpenMenu] = useState<number|null>(null);
  const [submitting, setSub]    = useState(false);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { dealerId, year: new Date().getFullYear(), price: 0 }
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await vehicleApi.listByDealer(dealerId, page, 10);
      setVehicles(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load vehicles'); }
    finally { setLoading(false); }
  }, [dealerId, page]);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => {
    reset({ dealerId, model:'', year: new Date().getFullYear(), price: 0 });
    setEditing(null);
    setModal('create');
  };
  const openEdit = (v: Vehicle) => {
    reset({ dealerId, model: v.model, variant: v.variant, vin: v.vin,
      year: v.year, color: v.color, price: v.price,
      fuelType: v.fuelType, transmission: v.transmission });
    setEditing(v);
    setModal('edit');
  };

  const onSubmit = async (data: FormData) => {
    setSub(true);
    try {
      if (modal === 'create') { await vehicleApi.create(data); toast.success('Vehicle added'); }
      else if (editing)        { await vehicleApi.update(editing.id, data); toast.success('Vehicle updated'); }
      setModal(null); load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Error saving vehicle');
    } finally { setSub(false); }
  };

  const deleteVehicle = async (id: number) => {
    if (!window.confirm('Remove this vehicle from inventory?')) return;
    try { await vehicleApi.delete(id); toast.success('Vehicle removed'); load(); }
    catch { toast.error('Delete failed'); }
    setOpenMenu(null);
  };

  const filtered = vehicles.filter(v =>
    `${v.model} ${v.variant} ${v.vin} ${v.color}`.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);
  const fmtPrice = (p: number) => `₹${(p/100000).toFixed(1)}L`;

  return (
    <AppLayout title="Vehicles">
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicle Inventory</h1>
          <p className="page-subtitle">{total} vehicle{total!==1?'s':''} in stock</p>
        </div>
        <button className="btn btn-primary" onClick={openCreate}><Plus size={16} /> Add Vehicle</button>
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={16} />
          <input type="text" placeholder="Search model, VIN, color..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Vehicle</th><th>VIN</th><th>Year</th><th>Fuel / Trans.</th>
                <th>Price</th><th>Status</th><th style={{ width:48 }}></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7}><div className="loading-center"><div className="spinner" /></div></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7}>
                  <div className="empty-state"><Car /><h3>No vehicles</h3><p>Add vehicles to your inventory</p></div>
                </td></tr>
              ) : filtered.map(v => (
                <tr key={v.id}>
                  <td>
                    <div style={{ fontWeight: 600 }}>Serene {v.model}</div>
                    {v.variant && <div className="td-muted">{v.variant} · {v.color}</div>}
                  </td>
                  <td><span style={{ fontFamily:'monospace', fontSize:'0.8rem', color:'var(--text-secondary)' }}>{v.vin || '—'}</span></td>
                  <td style={{ fontWeight:600 }}>{v.year}</td>
                  <td className="td-muted">{[v.fuelType, v.transmission].filter(Boolean).join(' / ') || '—'}</td>
                  <td style={{ fontWeight:700, color:'var(--brand-400)' }}>{fmtPrice(v.price)}</td>
                  <td><span className={`badge ${statusColor[v.status] ?? 'badge-inactive'}`}>{v.status}</span></td>
                  <td style={{ position:'relative' }}>
                    <button className="btn btn-ghost btn-icon" onClick={() => setOpenMenu(openMenu===v.id?null:v.id)}><MoreVertical size={16} /></button>
                    {openMenu === v.id && (
                      <div className="dropdown-menu">
                        <div className="dropdown-item" onClick={() => { openEdit(v); setOpenMenu(null); }}><Edit2 size={14} /> Edit</div>
                        <div className="dropdown-divider" />
                        <div className="dropdown-item dropdown-item-danger" onClick={() => deleteVehicle(v.id)}><Trash2 size={14} /> Remove</div>
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
              {Array.from({length:totalPages},(_,i)=>(
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
            <h2 className="modal-title">{modal==='create' ? '+ Add Vehicle' : 'Edit Vehicle'}</h2>
            <form onSubmit={handleSubmit(onSubmit)} style={{ display:'flex', flexDirection:'column', gap:16 }}>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Model *</label>
                  <input {...register('model')} className="form-input" placeholder="Aura" />
                  {errors.model && <span className="form-error">{errors.model.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Variant</label>
                  <input {...register('variant')} className="form-input" placeholder="S+ Turbo" />
                </div>
              </div>
              <div className="form-grid-3">
                <div className="form-group">
                  <label className="form-label">Year *</label>
                  <input {...register('year')} type="number" className="form-input" />
                  {errors.year && <span className="form-error">{errors.year.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Price (₹) *</label>
                  <input {...register('price')} type="number" className="form-input" placeholder="1200000" />
                  {errors.price && <span className="form-error">{errors.price.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Color</label>
                  <input {...register('color')} className="form-input" placeholder="Midnight Black" />
                </div>
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Fuel Type</label>
                  <select {...register('fuelType')} className="form-select">
                    <option value="">Select...</option>
                    {FUEL_TYPES.map(f => <option key={f} value={f}>{f}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Transmission</label>
                  <select {...register('transmission')} className="form-select">
                    <option value="">Select...</option>
                    {TRANSMISSIONS.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">VIN</label>
                <input {...register('vin')} className="form-input" placeholder="17-character VIN (optional)" />
              </div>
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea {...register('description')} className="form-textarea" placeholder="Additional vehicle details..." style={{ minHeight:80 }} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(null)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : modal==='create' ? 'Add to Inventory' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
