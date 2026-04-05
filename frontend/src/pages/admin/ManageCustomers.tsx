import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Button, Form, Input, Modal, Select, Space, Switch, Table, Typography } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { customerService } from '../../api/customerService'
import { dealerService } from '../../api/dealerService'
import { ActiveBadge } from '../../components/StatusBadge'
import { CustomerLocationFields } from '../../components/CustomerLocationFields'
import { formatCustomerLocation } from '../../utils/formatCustomerLocation'
import { PAGE_SIZE_OPTIONS } from '../../utils/tablePagination'
import {
  columnSortOrder,
  serverTableOnChange,
  springSortParam,
  TABLE_SORT_ASC_DESC,
  useServerTableSortRefs,
} from '../../utils/serverTableSort'
import type { ColumnsType } from 'antd/es/table'
import type { SortOrder } from 'antd/es/table/interface'
import type { Customer, Dealer } from '../../types/models'

const { Title } = Typography

const DEFAULT_SORT_FIELD = 'fullName'

export default function ManageCustomers() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [dealerId, setDealerId] = useState<number | undefined>()
  const [q, setQ] = useState('')
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('ascend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState<Customer | null>(null)
  const [form] = Form.useForm()

  const sortParam = springSortParam(sortField, sortOrder ?? 'ascend')

  const { data: dealersData } = useQuery({
    queryKey: ['admin-dealers-dd'],
    queryFn: () => dealerService.adminList(0, 500, 'companyName,asc'),
  })

  const { data, isLoading } = useQuery({
    queryKey: ['admin-customers', page, size, dealerId, q, sortField, sortOrder],
    queryFn: () => customerService.adminList(page, size, sortParam, dealerId, q || undefined),
  })

  const createMut = useMutation({
    mutationFn: customerService.adminCreate,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-customers'] })
      setModalOpen(false)
      form.resetFields()
    },
  })

  const updateMut = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Parameters<typeof customerService.adminUpdate>[1] }) =>
      customerService.adminUpdate(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-customers'] })
      setModalOpen(false)
      setEditing(null)
      form.resetFields()
    },
  })

  const deleteMut = useMutation({
    mutationFn: customerService.adminDelete,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-customers'] }),
  })

  const dealerOptions =
    dealersData?.content?.map((d: Dealer) => ({ label: d.companyName, value: d.id })) ?? []

  const openCreate = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record: Customer) => {
    setEditing(record)
    form.setFieldsValue({
      dealerId: record.dealerId,
      fullName: record.fullName,
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
    const loc = {
      countryCode: v.countryCode,
      stateCode: v.stateCode,
      city: v.city,
    }
    if (editing) {
      await updateMut.mutateAsync({
        id: editing.id,
        body: {
          fullName: v.fullName,
          phone: v.phone,
          address: v.address,
          ...loc,
          active: v.active,
          dealerId: v.dealerId,
        },
      })
    } else {
      await createMut.mutateAsync({
        ...(v.dealerId != null ? { dealerId: v.dealerId } : {}),
        fullName: v.fullName,
        phone: v.phone,
        address: v.address,
        ...loc,
        active: v.active ?? true,
      })
    }
  }

  const columns: ColumnsType<Customer> = [
    {
      title: 'Name',
      dataIndex: 'fullName',
      key: 'fullName',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'fullName'),
      showSorterTooltip: { title: 'Sort by name' },
    },
    {
      title: 'Dealer',
      dataIndex: 'dealerCompanyName',
      key: 'dealer.companyName',
      ellipsis: true,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'dealer.companyName'),
      showSorterTooltip: { title: 'Sort by dealer' },
    },
    {
      title: 'Location',
      key: 'city',
      ellipsis: true,
      render: (_, r) => formatCustomerLocation(r),
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'city'),
      showSorterTooltip: { title: 'Sort by city' },
    },
    {
      title: 'Phone',
      dataIndex: 'phone',
      key: 'phone',
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'phone'),
      showSorterTooltip: { title: 'Sort by phone' },
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      render: (a: boolean) => <ActiveBadge active={a} />,
      sorter: true,
      sortDirections: ['ascend', 'descend'],
      sortOrder: columnSortOrder(sortField, sortOrder, 'active'),
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
            onClick={() =>
              Modal.confirm({
                title: 'Delete customer?',
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

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
        <Title level={4} style={{ margin: 0 }}>
          Customers
        </Title>
        <Space wrap>
          <Select
            allowClear
            placeholder="Filter by dealer"
            style={{ width: 220 }}
            options={dealerOptions}
            value={dealerId}
            onChange={(v) => {
              setDealerId(v)
              setPage(0)
            }}
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
            Add customer
          </Button>
        </Space>
      </Space>
      <Table<Customer>
        rowKey="id"
        sortDirections={TABLE_SORT_ASC_DESC}
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={{
          current: (data?.number ?? 0) + 1,
          pageSize: size,
          total: data?.totalElements ?? 0,
          pageSizeOptions: [...PAGE_SIZE_OPTIONS],
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} items`,
        }}
        onChange={serverTableOnChange<Customer>({
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
        title={editing ? 'Edit customer' : 'Create customer'}
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
          <Form.Item
            name="dealerId"
            label="Dealer"
            extra={
              editing
                ? undefined
                : 'Optional. Leave empty to auto-assign the active dealer for the selected city (country, state, city required).'
            }
          >
            <Select
              allowClear
              placeholder="Auto by city"
              options={dealerOptions}
              disabled={!!editing}
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>
          <Form.Item name="fullName" label="Full name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input />
          </Form.Item>
          <CustomerLocationFields form={form} locationOptional={!!editing} />
          <Form.Item name="address" label="Street address">
            <Input placeholder="Street, building, unit" />
          </Form.Item>
          <Form.Item name="active" label="Active" valuePropName="checked" initialValue>
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
