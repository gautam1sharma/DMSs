import { useRef } from 'react'
import type { Key, MutableRefObject } from 'react'
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

/** Keeps refs aligned with state on every render (no useEffect lag before the next click). */
export function useServerTableSortRefs(sortField: string, sortOrder: SortOrder | null | undefined) {
  const sortFieldRef = useRef(sortField)
  const sortOrderRef = useRef(sortOrder)
  sortFieldRef.current = sortField
  sortOrderRef.current = sortOrder
  return { sortFieldRef, sortOrderRef }
}

/**
 * Ant Design picks the next tooltip from {@code sortDirections[index + 1]}. With only two entries,
 * after {@code descend} the next index is undefined so the UI shows “cancel sorting” while our
 * server handler toggles to ascending. The trailing {@code ascend} fixes the tooltip only; when
 * already ascending, {@code indexOf} matches the first entry so the cycle stays ascend ↔ descend.
 */
export const TABLE_SORT_ASC_DESC: SortOrder[] = ['ascend', 'descend', 'ascend']

const DEFAULT_NON_SORTABLE = new Set(['actions', 'a'])

type SorterCell = {
  columnKey?: Key
  column?: { key?: Key } | null
  field?: Key | readonly Key[]
  order?: SortOrder
}

function resolveSorterColumnKey(s: SorterCell): string | null {
  if (s.columnKey != null && s.columnKey !== '') {
    return String(s.columnKey)
  }
  const ck = s.column?.key
  if (ck != null && ck !== '') {
    return String(ck)
  }
  const f = s.field
  if (typeof f === 'string' && f) {
    return f
  }
  if (Array.isArray(f) && f.length > 0) {
    return f.map(String).join('.')
  }
  return null
}

export type ServerTableSortConfig = {
  size: number
  setPage: (n: number) => void
  setSize: (n: number) => void
  defaultSortField: string
  /** Used when switching away from an ambiguous sort state. */
  defaultSortOrder?: SortOrder
  sortFieldRef: MutableRefObject<string>
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
    if (extra.action !== 'sort') {
      return
    }

    const s = (Array.isArray(sorter) ? sorter[0] : sorter) as SorterCell | undefined
    if (!s) {
      return
    }

    const colKey = resolveSorterColumnKey(s)
    if (colKey != null && skip.has(colKey)) {
      return
    }

    const activeField = cfg.sortFieldRef.current

    if (s.order === 'ascend' || s.order === 'descend') {
      const field = colKey ?? activeField
      cfg.setSortField(field)
      cfg.setSortOrder(s.order)
      return
    }

    // Third click: Ant clears `order`; `columnKey` is often missing — treat null key as “same column”.
    const sameColumn = colKey == null || colKey === activeField

    if (sameColumn) {
      cfg.setSortField(activeField)
      const prev = cfg.sortOrderRef.current
      cfg.setSortOrder(prev === 'descend' ? 'ascend' : 'descend')
      return
    }

    cfg.setSortField(cfg.defaultSortField)
    cfg.setSortOrder(cfg.defaultSortOrder ?? 'ascend')
  }
}
