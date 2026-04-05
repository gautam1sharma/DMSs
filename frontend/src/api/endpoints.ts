import api from './axios';

export interface LoginRequest { email: string; password: string; }

export interface UserInfo {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  roles: string[];
  dealerId?: number;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserInfo;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ---- Auth ----
export const authApi = {
  login: (data: LoginRequest) => api.post<AuthResponse>('/auth/login', data),
};

// ---- Dashboard ----
export const dashboardApi = {
  getAdminStats:  ()                => api.get('/dashboard/admin'),
  getDealerStats: (dealerId: number) => api.get(`/dashboard/dealer/${dealerId}`),
};

// ---- Users ----
export const userApi = {
  list:   (page = 0, size = 10) => api.get(`/users?page=${page}&size=${size}&sort=createdAt,desc`),
  create: (data: any)           => api.post('/users', data),
  get:    (id: number)          => api.get(`/users/${id}`),
  lock:   (id: number)          => api.patch(`/users/${id}/lock`),
  unlock: (id: number)          => api.patch(`/users/${id}/unlock`),
  delete: (id: number)          => api.delete(`/users/${id}`),
};

// ---- Dealers ----
export const dealerApi = {
  list:         (page = 0, size = 10) => api.get(`/dealers?page=${page}&size=${size}`),
  create:       (data: any)           => api.post('/dealers', data),
  get:          (id: number)          => api.get(`/dealers/${id}`),
  update:       (id: number, data: any) => api.put(`/dealers/${id}`, data),
  updateStatus: (id: number, status: string) => api.patch(`/dealers/${id}/status?status=${status}`),
  delete:       (id: number)          => api.delete(`/dealers/${id}`),
};

// ---- Customers ----
export const customerApi = {
  listByDealer: (dealerId: number, page = 0, size = 10) =>
    api.get(`/customers/dealer/${dealerId}?page=${page}&size=${size}&sort=createdAt,desc`),
  listAll:  (page = 0, size = 10) => api.get(`/customers?page=${page}&size=${size}&sort=createdAt,desc`),
  create:   (data: any)           => api.post('/customers', data),
  get:      (id: number)          => api.get(`/customers/${id}`),
  update:   (id: number, data: any) => api.put(`/customers/${id}`, data),
  delete:   (id: number)          => api.delete(`/customers/${id}`),
};

// ---- Vehicles ----
export const vehicleApi = {
  listByDealer: (dealerId: number, page = 0, size = 10) =>
    api.get(`/vehicles/dealer/${dealerId}?page=${page}&size=${size}`),
  create: (data: any)           => api.post('/vehicles', data),
  update: (id: number, data: any) => api.put(`/vehicles/${id}`, data),
  delete: (id: number)          => api.delete(`/vehicles/${id}`),
};

// ---- Orders ----
export const orderApi = {
  listByDealer: (dealerId: number, page = 0, size = 10) =>
    api.get(`/orders/dealer/${dealerId}?page=${page}&size=${size}&sort=createdAt,desc`),
  listAll: (page = 0, size = 10) => api.get(`/orders?page=${page}&size=${size}&sort=createdAt,desc`),
  create:  (data: any)           => api.post('/orders', data),
  updateStatus: (id: number, status: string) => api.patch(`/orders/${id}/status?status=${status}`),
};

// ---- Inquiries ----
export const inquiryApi = {
  listByDealer: (dealerId: number, page = 0, size = 10) =>
    api.get(`/inquiries/dealer/${dealerId}?page=${page}&size=${size}&sort=createdAt,desc`),
  create:  (data: any)           => api.post('/inquiries', data),
  respond: (id: number, response: string) => api.patch(`/inquiries/${id}/respond`, { response }),
  updateStatus: (id: number, status: string) => api.patch(`/inquiries/${id}/status?status=${status}`),
};
