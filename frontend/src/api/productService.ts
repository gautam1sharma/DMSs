import api from './axiosInstance'
import type { Product, SpringPage } from '../types/models'

export const productService = {
  list: (page: number, size: number, sort?: string, category?: string, q?: string) =>
    api
      .get<SpringPage<Product>>('/products', { params: { page, size, sort, category, q } })
      .then((r) => r.data),

  get: (id: number) => api.get<Product>(`/products/${id}`).then((r) => r.data),

  adminCreate: (body: {
    name: string
    description?: string
    price: number
    stockQty: number
    category?: string
    active?: boolean
  }) => api.post<Product>('/admin/products', body).then((r) => r.data),

  adminUpdate: (
    id: number,
    body: {
      name?: string
      description?: string
      price?: number
      stockQty?: number
      category?: string
      active?: boolean
    },
  ) => api.put<Product>(`/admin/products/${id}`, body).then((r) => r.data),

  adminDelete: (id: number) => api.delete(`/admin/products/${id}`),
}
