import { useCallback, useMemo, useState, type HTMLAttributes, type SyntheticEvent } from 'react'
import { Resizable, type ResizeCallbackData } from 'react-resizable'
import type { ColumnType, ColumnsType, TableProps } from 'antd/es/table'
import 'react-resizable/css/styles.css'

const MIN_COL_WIDTH = 56

function columnResizeKey<T>(col: ColumnType<T>): string | null {
  if (col.key !== undefined && col.key !== null) return String(col.key)
  const di = col.dataIndex
  if (di === undefined || di === null) return null
  if (Array.isArray(di)) return di.join('.')
  return String(di)
}

type ResizableTitleProps = HTMLAttributes<HTMLTableCellElement> & {
  onResize?: (e: SyntheticEvent, data: ResizeCallbackData) => void
  width?: number
}

const ResizableTitle = (props: ResizableTitleProps) => {
  const { onResize, width, style, ...restProps } = props
  if (width == null) {
    return <th {...restProps} style={style} />
  }
  return (
    <Resizable
      width={width}
      height={0}
      minConstraints={[MIN_COL_WIDTH, 0]}
      maxConstraints={[2000, 0]}
      handle={
        <span
          className="react-resizable-handle"
          onClick={(e) => e.stopPropagation()}
          aria-hidden
        />
      }
      onResize={onResize}
      draggableOpts={{ enableUserSelectHack: false }}
    >
      <th {...restProps} style={{ ...style, width, position: 'relative' }} />
    </Resizable>
  )
}

export const RESIZABLE_TABLE_COMPONENTS: TableProps<unknown>['components'] = {
  header: {
    cell: ResizableTitle,
  },
}

function defaultWidthFor<T>(
  col: ColumnType<T>,
  key: string,
  defaultWidths: Record<string, number>,
): number {
  if (typeof col.width === 'number') return col.width
  if (defaultWidths[key] != null) return defaultWidths[key]
  return 140
}

/**
 * Adds draggable column resize handles. Pass initial widths in `defaultWidths` keyed by column `key` or `dataIndex`.
 * Tables should set `tableLayout="fixed"` and spread `components` onto `<Table />`.
 */
export function useResizableColumns<T>(
  columns: ColumnsType<T>,
  defaultWidths: Record<string, number> = {},
): { columns: ColumnsType<T>; components: TableProps<T>['components'] } {
  const [widths, setWidths] = useState<Record<string, number>>({})

  const handleResize = useCallback((key: string) => {
    return (_e: SyntheticEvent, { size }: ResizeCallbackData) => {
      setWidths((prev) => ({ ...prev, [key]: Math.max(MIN_COL_WIDTH, size.width) }))
    }
  }, [])

  const merged = useMemo(() => {
    return columns.map((col) => {
      const key = columnResizeKey(col)
      if (!key) return col

      const base = defaultWidthFor(col, key, defaultWidths)
      const w = widths[key] ?? base

      return {
        ...col,
        width: w,
        onHeaderCell: () => ({
          width: widths[key] ?? base,
          onResize: handleResize(key),
        }),
      } as ColumnType<T>
    })
  }, [columns, widths, handleResize, defaultWidths])

  return { columns: merged, components: RESIZABLE_TABLE_COMPONENTS as TableProps<T>['components'] }
}
