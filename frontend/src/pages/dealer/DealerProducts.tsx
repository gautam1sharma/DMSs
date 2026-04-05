import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Input, Table, Typography } from 'antd'
import { productService } from '../../api/productService'
import { ActiveBadge } from '../../components/StatusBadge'
import { formatRupee } from '../../utils/formatCurrency'
import { serverPaginationProps } from '../../utils/tablePagination'
import {
  columnSortOrder,
  serverTableOnChange,
  springSortParam,
  TABLE_SORT_ASC_DESC,
  useServerTableSortRefs,
} from '../../utils/serverTableSort'
import { useResizableColumns } from '../../utils/resizableTable'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import type { Product } from '../../types/models'

const { Title } = Typography

const DEFAULT_SORT_FIELD = 'name'

export default function DealerProducts() {
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [category, setCategory] = useState('')
  const [q, setQ] = useState('')
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')

  const { data, isLoading } = useQuery({
    queryKey: ['dealer-products', page, size, category, q, sortField, sortOrder],
    queryFn: () => productService.list(page, size, sortParam, category || undefined, q || undefined),
  })

  const baseColumns: ColumnsType<Product> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'name'),
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'category'),
    },
    {
      title: 'Price',
      dataIndex: 'price',
      key: 'price',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'price'),
      render: (p: number) => formatRupee(p),
    },
    {
      title: 'Stock',
      dataIndex: 'stockQty',
      key: 'stockQty',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'stockQty'),
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'active'),
      render: (a: boolean) => <ActiveBadge active={a} />,
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    name: 200,
    category: 140,
    price: 120,
    stockQty: 96,
    active: 100,
  })

  return (
    <div>
      <div style={{ marginBottom: 16, display: 'flex', gap: 12, flexWrap: 'wrap', alignItems: 'center' }}>
        <Title level={4} style={{ margin: 0, flex: 1 }}>
          Product catalog
        </Title>
        <Input
          allowClear
          placeholder="Category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          style={{ width: 160 }}
        />
        <Input.Search
          allowClear
          placeholder="Search"
          onSearch={(v) => {
            setQ(v)
            setPage(0)
          }}
          style={{ width: 220 }}
        />
      </div>
      <Table<Product>
        rowKey="id"
        tableLayout="fixed"
        components={components}
        sortDirections={TABLE_SORT_ASC_DESC}
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<Product>({
          size,
          setPage,
          setSize,
          defaultSortField: DEFAULT_SORT_FIELD,
          sortFieldRef,
          sortOrderRef,
          setSortField,
          setSortOrder,
        })}
      />
    </div>
  )
}
