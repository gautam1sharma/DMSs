import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Form, Input, InputNumber, Modal, Space, Switch, Table, Typography } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
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

export default function ManageProducts() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [category, setCategory] = useState('')
  const [q, setQ] = useState('')
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [form] = Form.useForm()

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')

  const { data, isLoading } = useQuery({
    queryKey: ['admin-products', page, size, category, q, sortField, sortOrder],
    queryFn: () => productService.list(page, size, sortParam, category || undefined, q || undefined),
  })

  const createMut = useMutation({
    mutationFn: productService.adminCreate,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-products'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Parameters<typeof productService.adminUpdate>[1] }) =>
      productService.adminUpdate(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-products'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: productService.adminDelete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-products'] }),
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record: Product) => {
    setEditing(record)
    form.setFieldsValue({
      name: record.name,
      description: record.description,
      price: record.price,
      stockQty: record.stockQty,
      category: record.category,
      active: record.active,
    })
    setModalOpen(true)
  }

  const submit = async () => {
    const v = await form.validateFields()
    if (editing) {
      await updateMut.mutateAsync({
        id: editing.id,
        body: {
          name: v.name,
          description: v.description,
          price: v.price,
          stockQty: v.stockQty,
          category: v.category,
          active: v.active,
        },
      })
    } else {
      await createMut.mutateAsync({
        name: v.name,
        description: v.description,
        price: v.price,
        stockQty: v.stockQty,
        category: v.category,
        active: v.active ?? true,
      })
    }
  }

  const baseColumns: ColumnsType<Product> = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'name'),
    },
    {
      title: 'Category',
      dataIndex: 'category',
      key: 'category',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'category'),
    },
    {
      title: 'Price',
      dataIndex: 'price',
      key: 'price',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'price'),
      render: (p: number) => formatRupee(p),
    },
    {
      title: 'Stock',
      dataIndex: 'stockQty',
      key: 'stockQty',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'stockQty'),
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'active'),
      render: (a: boolean) => <ActiveBadge active={a} />,
    },
    {
      title: 'Actions',
      key: 'a',
      render: (_, r) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(r)}>
            Edit
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() =>
              Modal.confirm({
                title: 'Delete product?',
                onOk: () => deleteMut.mutateAsync(r.id),
              })
            }
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    name: 200,
    category: 140,
    price: 120,
    stockQty: 96,
    active: 100,
    a: 220,
  })

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
        <Title level={4} style={{ margin: 0 }}>
          Products
        </Title>
        <Space wrap>
          <Input
            allowClear
            placeholder="Category"
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            style={{ width: 140 }}
          />
          <Input.Search
            allowClear
            placeholder="Search name"
            onSearch={(v) => {
              setQ(v)
              setPage(0)
            }}
            style={{ width: 220 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Add product
          </Button>
        </Space>
      </Space>
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
      <Modal
        title={editing ? 'Edit product' : 'Create product'}
        open={modalOpen}
        onCancel={() => {
          setModalOpen(false)
          setEditing(null)
          form.resetFields()
        }}
        onOk={submit}
        confirmLoading={createMut.isPending || updateMut.isPending}
        destroyOnClose
        width={560}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="price" label="Price (INR)" rules={[{ required: true }]}>
            <InputNumber min={0} step={1000} addonBefore="₹" style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="stockQty" label="Stock qty" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="category" label="Category">
            <Input />
          </Form.Item>
          <Form.Item name="active" label="Active" valuePropName="checked" initialValue>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
