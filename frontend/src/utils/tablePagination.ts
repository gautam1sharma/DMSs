import type { SpringPage } from '../types/models'

export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100] as const

export interface ServerPaginationHandlers {
  page: number
  size: number
  setPage: (n: number) => void
  setSize: (n: number) => void
}

/** Ant Design Table `pagination` prop for Spring Data `Page` responses. */
export function serverPaginationProps<T>(
  data: SpringPage<T> | undefined,
  handlers: ServerPaginationHandlers,
) {
  const { size, setPage, setSize } = handlers
  return {
    current: (data?.number ?? 0) + 1,
    pageSize: size,
    total: data?.totalElements ?? 0,
    pageSizeOptions: [...PAGE_SIZE_OPTIONS],
    showSizeChanger: true,
    showTotal: (total: number) => `Total ${total} items`,
    onChange: (newPage: number, newPageSize?: number) => {
      const ps = newPageSize ?? size
      if (ps !== size) {
        setSize(ps)
        setPage(0)
      } else {
        setPage(newPage - 1)
      }
    },
  }
}
