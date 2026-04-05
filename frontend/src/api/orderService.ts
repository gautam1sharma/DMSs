import api from './axiosInstance'
import type { Order, OrderStatus, SpringPage } from '../types/models'

export const orderService = {
  adminList: (page: number, size: number, sort?: string, dealerId?: number, status?: OrderStatus) =>
    api
      .get<SpringPage<Order>>('/admin/orders', {
        params: { page, size, sort, dealerId, status },
      })
      .then((r) => r.data),

  adminGet: (id: number) => api.get<Order>(`/admin/orders/${id}`).then((r) => r.data),

  adminCreate: (body: {
    dealerId?: number
    customerId: number
    items: { productId: number; quantity: number }[]
  }) => api.post<Order>('/admin/orders', body).then((r) => r.data),

  adminUpdateStatus: (id: number, status: OrderStatus) =>
    api.patch<Order>(`/admin/orders/${id}/status`, { status }).then((r) => r.data),

  dealerList: (page: number, size: number, sort?: string, status?: OrderStatus) =>
    api
      .get<SpringPage<Order>>('/dealer/orders', { params: { page, size, sort, status } })
      .then((r) => r.data),

  dealerGet: (id: number) => api.get<Order>(`/dealer/orders/${id}`).then((r) => r.data),

  dealerCreate: (body: {
    customerId: number
    items: { productId: number; quantity: number }[]
  }) => api.post<Order>('/dealer/orders', body).then((r) => r.data),

  dealerUpdateStatus: (id: number, status: OrderStatus) =>
    api.patch<Order>(`/dealer/orders/${id}/status`, { status }).then((r) => r.data),
}
