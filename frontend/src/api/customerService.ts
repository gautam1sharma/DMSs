import api from './axiosInstance'
import type { Customer, SpringPage } from '../types/models'

export const customerService = {
  adminList: (page: number, size: number, sort?: string, dealerId?: number, q?: string) =>
    api
      .get<SpringPage<Customer>>('/admin/customers', {
        params: { page, size, sort, dealerId, q },
      })
      .then((r) => r.data),

  adminGet: (id: number) => api.get<Customer>(`/admin/customers/${id}`).then((r) => r.data),

  adminCreate: (body: {
    dealerId?: number
    fullName: string
    phone?: string
    address?: string
    countryCode?: string
    stateCode?: string
    city?: string
    active?: boolean
  }) => api.post<Customer>('/admin/customers', body).then((r) => r.data),

  adminUpdate: (
    id: number,
    body: {
      fullName?: string
      phone?: string
      address?: string
      countryCode?: string
      stateCode?: string
      city?: string
      active?: boolean
      dealerId?: number
    },
  ) => api.put<Customer>(`/admin/customers/${id}`, body).then((r) => r.data),

  adminDelete: (id: number) => api.delete(`/admin/customers/${id}`),

  dealerList: (page: number, size: number, sort?: string, q?: string) =>
    api
      .get<SpringPage<Customer>>('/dealer/customers', { params: { page, size, sort, q } })
      .then((r) => r.data),

  dealerGet: (id: number) => api.get<Customer>(`/dealer/customers/${id}`).then((r) => r.data),

  dealerCreate: (body: {
    fullName: string
    phone?: string
    address?: string
    countryCode?: string
    stateCode?: string
    city?: string
    active?: boolean
  }) => api.post<Customer>('/dealer/customers', body).then((r) => r.data),

  dealerUpdate: (
    id: number,
    body: {
      fullName?: string
      phone?: string
      address?: string
      countryCode?: string
      stateCode?: string
      city?: string
      active?: boolean
    },
  ) => api.put<Customer>(`/dealer/customers/${id}`, body).then((r) => r.data),

  dealerDelete: (id: number) => api.delete(`/dealer/customers/${id}`),
}
