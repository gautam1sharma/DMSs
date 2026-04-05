import type { Query, QueryClient } from '@tanstack/react-query'

function portalPrefix(queryKey: readonly unknown[]): string | undefined {
  const k = queryKey[0]
  return typeof k === 'string' ? k : undefined
}

export function isAdminPortalQuery(query: Query): boolean {
  const p = portalPrefix(query.queryKey)
  return p != null && p.startsWith('admin')
}

export function isDealerPortalQuery(query: Query): boolean {
  const p = portalPrefix(query.queryKey)
  return p != null && p.startsWith('dealer')
}

export function invalidateAdminPortalQueries(queryClient: QueryClient) {
  return queryClient.invalidateQueries({ predicate: isAdminPortalQuery })
}

export function invalidateDealerPortalQueries(queryClient: QueryClient) {
  return queryClient.invalidateQueries({ predicate: isDealerPortalQuery })
}
