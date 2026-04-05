import type { MutableRefObject } from 'react'
import type { SortOrder } from 'antd/es/table/interface'
import type { TableProps } from 'antd'

/** Spring Data `sort` query param, e.g. `fullName,asc`. */
export function springSortParam(field: string, order: SortOrder | null | undefined): string {
  const dir = order === 'descend' ? 'desc' : 'asc'
  return `${field},${dir}`
}

export function columnSortOrder(
  sortField: string,
  sortOrder: SortOrder | null | undefined,
  columnKey: string,
): SortOrder | undefined {
  return sortField === columnKey ? sortOrder ?? undefined : undefined
}

const DEFAULT_NON_SORTABLE = new Set(['actions', 'a'])

export type ServerTableSortConfig = {
  size: number
  setPage: (n: number) => void
  setSize: (n: number) => void
  defaultSortField: string
  /** Used when switching away from an ambiguous sort state. */
  defaultSortOrder?: SortOrder
  sortFieldRef: MutableRefObject<string>
  /** Must track `sortOrder` via useEffect so the handler sees the last committed direction when Ant clears sort on the 3rd click. */
  sortOrderRef: MutableRefObject<SortOrder | null | undefined>
  setSortField: (f: string) => void
  setSortOrder: (o: SortOrder) => void
  /** Extra column keys that should not trigger server sort (e.g. composite UI-only columns). */
  nonSortableColumnKeys?: Set<string>
}

export function serverTableOnChange<T>(cfg: ServerTableSortConfig): NonNullable<TableProps<T>['onChange']> {
  const skip = new Set([...DEFAULT_NON_SORTABLE, ...(cfg.nonSortableColumnKeys ?? [])])
  return (pag, _filters, sorter, extra) => {
    const newSize = pag.pageSize ?? cfg.size
    if (newSize !== cfg.size) {
      cfg.setSize(newSize)
      cfg.setPage(0)
    } else {
      cfg.setPage((pag.current ?? 1) - 1)
    }
    if (extra.action === 'sort') {
      const s = Array.isArray(sorter) ? sorter[0] : sorter
      if (s && typeof s.columnKey === 'string' && !skip.has(s.columnKey)) {
        if (s.order) {
          cfg.setSortField(s.columnKey)
          cfg.setSortOrder(s.order)
        } else if (s.columnKey === cfg.sortFieldRef.current) {
          // Ant Design’s 3rd click “clears” sort — keep a strict ascend ↔ descend cycle (no unsorted).
          cfg.setSortField(s.columnKey)
          const prev = cfg.sortOrderRef.current
          cfg.setSortOrder(prev === 'descend' ? 'ascend' : 'descend')
        } else {
          cfg.setSortField(cfg.defaultSortField)
          cfg.setSortOrder(cfg.defaultSortOrder ?? 'ascend')
        }
      }
    }
  }
}
