import api from './axiosInstance'
import type { DashboardSummary } from '../types/models'

export const dashboardService = {
  adminSummary: () =>
    api.get<DashboardSummary>('/admin/dashboard/summary').then((r) => r.data),

  dealerSummary: () =>
    api.get<DashboardSummary>('/dealer/dashboard/summary').then((r) => r.data),
}
