import axios from 'axios'
import { notification } from 'antd'
import type { ApiErrorBody } from '../types/models'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('sems_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const status = err.response?.status
    const data = err.response?.data as ApiErrorBody | undefined
    const msg = data?.message || err.message || 'Request failed'

    if (status === 401) {
      localStorage.removeItem('sems_token')
      localStorage.removeItem('sems_user')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = '/login'
      }
    } else if (status && status !== 422) {
      notification.error({ message: 'Error', description: msg })
    }

    return Promise.reject(err)
  },
)

export default api
