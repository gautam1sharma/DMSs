import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Form, Input, Modal, Space, Switch, Table, Typography } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { dealerService } from '../../api/dealerService'
import { ActiveBadge } from '../../components/StatusBadge'
import { CustomerLocationFields } from '../../components/CustomerLocationFields'
import { formatIsoLocation } from '../../utils/formatCustomerLocation'
import { serverPaginationProps } from '../../utils/tablePagination'
import {
  columnSortOrder,
  serverTableOnChange,
  springSortParam,
  TABLE_SORT_ASC_DESC,
  useServerTableSortRefs,
} from '../../utils/serverTableSort'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import type { Dealer } from '../../types/models'

const { Title } = Typography

const DEFAULT_SORT_FIELD = 'companyName'

export default function ManageDealers() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [q, setQ] = useState('')
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Dealer | null>(null)
  const [form] = Form.useForm()

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')

  const { data, isLoading } = useQuery({
    queryKey: ['admin-dealers', page, size, q, sortField, sortOrder],
    queryFn: () => dealerService.adminList(page, size, sortParam, q || undefined),
  })

  const createMut = useMutation({
    mutationFn: dealerService.adminCreate,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-dealers'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Parameters<typeof dealerService.adminUpdate>[1] }) =>
      dealerService.adminUpdate(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-dealers'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: dealerService.adminDelete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-dealers'] }),
  })

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record: Dealer) => {
    setEditing(record)
    form.setFieldsValue({
      email: record.email,
      companyName: record.companyName,
      phone: record.phone,
      address: record.address,
      countryCode: record.countryCode,
      stateCode: record.stateCode,
      city: record.city,
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
          email: v.email,
          companyName: v.companyName,
          phone: v.phone,
          address: v.address,
          countryCode: v.countryCode,
          stateCode: v.stateCode,
          city: v.city,
          active: v.active,
        },
      })
    } else {
      await createMut.mutateAsync({
        username: v.username,
        email: v.email,
        password: v.password,
        companyName: v.companyName,
        phone: v.phone,
        address: v.address,
        countryCode: v.countryCode,
        stateCode: v.stateCode,
        city: v.city,
        active: v.active ?? true,
      })
    }
  }

  const columns: ColumnsType<Dealer> = [
    {
      title: 'Dealer name',
      dataIndex: 'companyName',
      key: 'companyName',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'companyName'),
      showSorterTooltip: { title: 'Sort by dealer name' },
    },
    {
      title: 'Username',
      dataIndex: 'username',
      key: 'user.username',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'user.username'),
      showSorterTooltip: { title: 'Sort by username' },
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'user.email',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'user.email'),
      showSorterTooltip: { title: 'Sort by email' },
    },
    {
      title: 'Location',
      key: 'city',
      ellipsis: true,
      render: (_, r) => formatIsoLocation(r),
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'city'),
      showSorterTooltip: { title: 'Sort by city' },
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'active'),
      render: (a: boolean) => <ActiveBadge active={a} />,
      showSorterTooltip: { title: 'Sort by status' },
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, r) => (
        <Space>
          <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(r)}>
            Edit
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined />}
            onClick={() => {
              Modal.confirm({
                title: 'Delete dealer?',
                onOk: () => deleteMut.mutateAsync(r.id),
              })
            }}
          >
            Delete
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} align="center">
        <Title level={4} style={{ margin: 0 }}>
          Dealers
        </Title>
        <Space>
          <Input.Search
            allowClear
            placeholder="Search dealer name"
            onSearch={(v) => {
              setQ(v)
              setPage(0)
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>
            Add dealer
          </Button>
        </Space>
      </Space>
      <Table<Dealer>
        rowKey="id"
        sortDirections={TABLE_SORT_ASC_DESC}
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<Dealer>({
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
        title={editing ? 'Edit dealer' : 'Create dealer'}
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
          {!editing && (
            <>
              <Form.Item name="username" label="Username" rules={[{ required: true }]}>
                <Input />
              </Form.Item>
              <Form.Item name="password" label="Password" rules={[{ required: true, min: 6 }]}>
                <Input.Password />
              </Form.Item>
            </>
          )}
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="companyName" label="Dealer name" rules={[{ required: true }]}>
            <Input placeholder="Outlet or business name" />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input />
          </Form.Item>
          <CustomerLocationFields form={form} locationOptional={!!editing} />
          <Form.Item name="address" label="Street address">
            <Input placeholder="Street, building, unit" />
          </Form.Item>
          {editing && (
            <Form.Item name="active" label="Active" valuePropName="checked">
              <Switch />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  )
}
