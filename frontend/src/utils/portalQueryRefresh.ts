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
  return Promise.all([
    queryClient.invalidateQueries({ predicate: isAdminPortalQuery }),
    queryClient.invalidateQueries({ queryKey: ['me-menus'] }),
    queryClient.invalidateQueries({ queryKey: ['admin-menus'] }),
  ])
}

export function invalidateDealerPortalQueries(queryClient: QueryClient) {
  return Promise.all([
    queryClient.invalidateQueries({ predicate: isDealerPortalQuery }),
    queryClient.invalidateQueries({ queryKey: ['me-menus'] }),
  ])
}
