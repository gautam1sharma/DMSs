import { useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '../auth/AuthContext'
import { MY_AVATAR_BLOB_QUERY_KEY, profileService } from '../api/profileService'

export function useUserAvatarUrl(): string | undefined {
  const { user, token } = useAuth()
  const enabled = Boolean(token) && user?.hasAvatar === true

  const { data: src } = useQuery({
    queryKey: [MY_AVATAR_BLOB_QUERY_KEY, user?.userId, user?.hasAvatar],
    queryFn: async () => {
      const blob = await profileService.fetchAvatarBlob()
      return URL.createObjectURL(blob)
    },
    enabled,
    staleTime: 5 * 60_000,
  })

  useEffect(() => {
    return () => {
      if (src) URL.revokeObjectURL(src)
    }
  }, [src])

  return enabled ? src : undefined
}
