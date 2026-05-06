import api from './axiosInstance'
import type { CurrentUserResponse } from '../types/models'

export const MY_AVATAR_BLOB_QUERY_KEY = 'my-avatar-blob' as const

export const profileService = {
  getMe: () => api.get<CurrentUserResponse>('/me').then((r) => r.data),

  uploadAvatar: (file: File) => {
    const body = new FormData()
    body.append('file', file)
    return api.post<CurrentUserResponse>('/me/avatar', body).then((r) => r.data)
  },

  deleteAvatar: () => api.delete<void>('/me/avatar'),

  fetchAvatarBlob: () =>
    api.get('/me/avatar', { responseType: 'blob' }).then((r) => r.data),
}
