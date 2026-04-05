import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Search, ShoppingCart, ChevronRight } from 'lucide-react';
import AppLayout from '../components/AppLayout';
import { orderApi, customerApi, vehicleApi } from '../api/endpoints';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

interface Order {
  id: number; orderNumber: string; dealerName: string;
  customerName: string; vehicleInfo?: string; amount: number;
  discount: number; finalAmount: number; status: string; createdAt: string;
}

const schema = z.object({
  dealerId:   z.coerce.number().positive(),
  customerId: z.coerce.number().positive('Customer required'),
  vehicleId:  z.coerce.number().optional(),
  amount:     z.coerce.number().positive('Amount required'),
  discount:   z.coerce.number().min(0).optional(),
  notes:      z.string().optional(),
});
type FormData = z.infer<typeof schema>;

const statusColor: Record<string, string> = {
  PENDING: 'badge-pending', CONFIRMED: 'badge-confirmed',
  PROCESSING: 'badge-pending', COMPLETED: 'badge-completed', CANCELLED: 'badge-cancelled'
};

const statusFlow = ['PENDING','CONFIRMED','PROCESSING','COMPLETED'];

export default function OrdersPage() {
  const { user, isAdmin } = useAuth();
  const dealerId = user?.dealerId ?? 0;
  const [orders, setOrders]       = useState<Order[]>([]);
  const [customers, setCustomers] = useState<any[]>([]);
  const [vehicles, setVehicles]   = useState<any[]>([]);
  const [total, setTotal]         = useState(0);
  const [page, setPage]           = useState(0);
  const [search, setSearch]       = useState('');
  const [loading, setLoading]     = useState(true);
  const [modal, setModal]         = useState(false);
  const [submitting, setSub]      = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order|null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { dealerId, discount: 0 }
  });

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = isAdmin
        ? await orderApi.listAll(page, 10)
        : await orderApi.listByDealer(dealerId, page, 10);
      setOrders(res.data.content);
      setTotal(res.data.totalElements);
    } catch { toast.error('Failed to load orders'); }
    finally { setLoading(false); }
  }, [dealerId, page, isAdmin]);

  const loadCustomersVehicles = useCallback(async () => {
    if (!dealerId) return;
    try {
      const [cr, vr] = await Promise.all([
        customerApi.listByDealer(dealerId, 0, 100),
        vehicleApi.listByDealer(dealerId, 0, 100)
      ]);
      setCustomers(cr.data.content);
      setVehicles(vr.data.content.filter((v: any) => v.status === 'AVAILABLE'));
    } catch {}
  }, [dealerId]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { loadCustomersVehicles(); }, [loadCustomersVehicles]);

  const onSubmit = async (data: FormData) => {
    setSub(true);
    try {
      await orderApi.create(data);
      toast.success('Order created');
      setModal(false);
      reset({ dealerId, discount: 0 });
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message ?? 'Error creating order');
    } finally { setSub(false); }
  };

  const advance = async (order: Order) => {
    const curr = statusFlow.indexOf(order.status);
    if (curr < 0 || curr >= statusFlow.length - 1) return;
    const next = statusFlow[curr + 1];
    try {
      await orderApi.updateStatus(order.id, next);
      toast.success(`Order → ${next}`);
      load();
    } catch { toast.error('Status update failed'); }
  };

  const cancel = async (order: Order) => {
    if (!window.confirm('Cancel this order?')) return;
    try { await orderApi.updateStatus(order.id, 'CANCELLED'); toast.success('Order cancelled'); load(); }
    catch { toast.error('Failed to cancel'); }
  };

  const filtered = orders.filter(o =>
    `${o.orderNumber} ${o.customerName}`.toLowerCase().includes(search.toLowerCase())
  );

  const totalPages = Math.ceil(total / 10);
  const fmtPrice = (p: number) => `₹${(p/100000).toFixed(2)}L`;

  return (
    <AppLayout title="Orders">
      <div className="page-header">
        <div>
          <h1 className="page-title">Orders</h1>
          <p className="page-subtitle">{total} order{total!==1?'s':''} total</p>
        </div>
        {!isAdmin && (
          <button className="btn btn-primary" onClick={() => { reset({ dealerId, discount:0 }); setModal(true); }}>
            <Plus size={16} /> New Order
          </button>
        )}
      </div>

      <div className="filter-bar">
        <div className="search-input-wrapper">
          <Search size={16} />
          <input type="text" placeholder="Search by order number, customer..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
      </div>

      <div className="card">
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Order #</th><th>Customer</th><th>Vehicle</th>
                <th>Amount</th><th>Status</th><th>Date</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7}><div className="loading-center"><div className="spinner" /></div></td></tr>
              ) : filtered.length === 0 ? (
                <tr><td colSpan={7}>
                  <div className="empty-state"><ShoppingCart /><h3>No orders yet</h3><p>Create your first order</p></div>
                </td></tr>
              ) : filtered.map(o => (
                <tr key={o.id}>
                  <td>
                    <span style={{ fontFamily:'monospace', fontSize:'0.82rem', color:'var(--brand-400)', fontWeight:600 }}>
                      {o.orderNumber}
                    </span>
                    {isAdmin && <div className="td-muted">{o.dealerName}</div>}
                  </td>
                  <td style={{ fontWeight:600 }}>{o.customerName}</td>
                  <td className="td-muted">{o.vehicleInfo || '—'}</td>
                  <td>
                    <div style={{ fontWeight:700 }}>{fmtPrice(o.finalAmount)}</div>
                    {o.discount > 0 && <div className="td-muted">Discount: {fmtPrice(o.discount)}</div>}
                  </td>
                  <td><span className={`badge ${statusColor[o.status] ?? 'badge-inactive'}`}>{o.status}</span></td>
                  <td className="td-muted">{new Date(o.createdAt).toLocaleDateString('en-IN')}</td>
                  <td>
                    <div style={{ display:'flex', gap:6 }}>
                      {!isAdmin && o.status !== 'COMPLETED' && o.status !== 'CANCELLED' && (
                        <button className="btn btn-secondary btn-sm" onClick={() => advance(o)}>
                          Advance <ChevronRight size={13} />
                        </button>
                      )}
                      {!isAdmin && o.status !== 'COMPLETED' && o.status !== 'CANCELLED' && (
                        <button className="btn btn-danger btn-sm" onClick={() => cancel(o)}>Cancel</button>
                      )}
                    </div>
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
          <div className="modal modal-lg" onClick={e => e.stopPropagation()}>
            <h2 className="modal-title">+ New Order</h2>
            <form onSubmit={handleSubmit(onSubmit)} style={{ display:'flex', flexDirection:'column', gap:16 }}>
              <div className="form-group">
                <label className="form-label">Customer *</label>
                <select {...register('customerId')} className="form-select">
                  <option value="">Select customer...</option>
                  {customers.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName} — {c.phone || c.email}</option>)}
                </select>
                {errors.customerId && <span className="form-error">{errors.customerId.message}</span>}
              </div>
              <div className="form-group">
                <label className="form-label">Vehicle (Available only)</label>
                <select {...register('vehicleId')} className="form-select">
                  <option value="">No specific vehicle</option>
                  {vehicles.map(v => <option key={v.id} value={v.id}>Serene {v.model} {v.variant} ({v.year}) — ₹{(v.price/100000).toFixed(1)}L</option>)}
                </select>
              </div>
              <div className="form-grid">
                <div className="form-group">
                  <label className="form-label">Amount (₹) *</label>
                  <input {...register('amount')} type="number" className="form-input" placeholder="1200000" />
                  {errors.amount && <span className="form-error">{errors.amount.message}</span>}
                </div>
                <div className="form-group">
                  <label className="form-label">Discount (₹)</label>
                  <input {...register('discount')} type="number" className="form-input" placeholder="0" />
                </div>
              </div>
              <div className="form-group">
                <label className="form-label">Notes</label>
                <textarea {...register('notes')} className="form-textarea" placeholder="Order notes..." style={{ minHeight:80 }} />
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-secondary" onClick={() => setModal(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Create Order'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
