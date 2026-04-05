import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Button,
  Drawer,
  Form,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Typography,
} from 'antd'
import { PlusOutlined, EyeOutlined } from '@ant-design/icons'
import { orderService } from '../../api/orderService'
import { customerService } from '../../api/customerService'
import { productService } from '../../api/productService'
import { dealerService } from '../../api/dealerService'
import { OrderStatusBadge } from '../../components/StatusBadge'
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
import type { Customer, Dealer, Order, OrderItem, OrderStatus, Product } from '../../types/models'
import dayjs from 'dayjs'

const { Title } = Typography

const statuses: OrderStatus[] = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED']

const DEFAULT_SORT_FIELD = 'orderDate'

export default function ManageOrders() {
  const qc = useQueryClient()
  const [page, setPage] = useState(0)
  const [size, setSize] = useState(10)
  const [dealerId, setDealerId] = useState<number | undefined>()
  const [status, setStatus] = useState<OrderStatus | undefined>()
  const [sortField, setSortField] = useState(DEFAULT_SORT_FIELD)
  const [sortOrder, setSortOrder] = useState<SortOrder>('descend')
  const { sortFieldRef, sortOrderRef } = useServerTableSortRefs(sortField, sortOrder)
  const [drawerOrder, setDrawerOrder] = useState<Order | null>(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [form] = Form.useForm()

  const { data: dealersData } = useQuery({
    queryKey: ['admin-dealers-dd2'],
    queryFn: () => dealerService.adminList(0, 500, 'companyName,asc'),
  })

  const sortParam = springSortParam(sortField, sortOrder ?? 'descend')

  const { data, isLoading } = useQuery({
    queryKey: ['admin-orders', page, size, dealerId, status, sortField, sortOrder],
    queryFn: () => orderService.adminList(page, size, sortParam, dealerId, status),
  })

  const createMut = useMutation({
    mutationFn: orderService.adminCreate,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['admin-orders'] })
      setCreateOpen(false)
      form.resetFields()
    },
  })

  const statusMut = useMutation({
    mutationFn: ({ id, s }: { id: number; s: OrderStatus }) => orderService.adminUpdateStatus(id, s),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-orders'] }),
  })

  const watchedDealer = Form.useWatch('dealerId', form)

  const { data: customersData } = useQuery({
    queryKey: ['admin-customers-for-order', watchedDealer],
    queryFn: () => customerService.adminList(0, 500, 'fullName,asc', watchedDealer),
    enabled: createOpen && !!watchedDealer,
  })

  const { data: productsData } = useQuery({
    queryKey: ['products-for-order'],
    queryFn: () => productService.list(0, 200, 'name,asc'),
    enabled: createOpen,
  })

  const baseColumns: ColumnsType<Order> = [
    {
      title: 'Order #',
      dataIndex: 'orderNumber',
      key: 'orderNumber',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'orderNumber'),
    },
    {
      title: 'Customer',
      dataIndex: 'customerName',
      key: 'customer.fullName',
      ellipsis: true,
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'customer.fullName'),
    },
    {
      title: 'Dealer',
      dataIndex: 'dealerCompanyName',
      key: 'dealer.companyName',
      ellipsis: true,
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'dealer.companyName'),
    },
    {
      title: 'Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'totalAmount'),
      render: (v: number) => formatRupee(v),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'status'),
      render: (s: OrderStatus) => <OrderStatusBadge status={s} />,
    },
    {
      title: 'Date',
      dataIndex: 'orderDate',
      key: 'orderDate',
      sorter: true,
      sortDirections: TABLE_SORT_ASC_DESC,
      sortOrder: columnSortOrder(sortField, sortOrder, 'orderDate'),
      render: (d: string) => dayjs(d).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: 'Actions',
      key: 'a',
      render: (_, r) => (
        <Space>
          <Button type="link" icon={<EyeOutlined />} onClick={() => setDrawerOrder(r)}>
            View
          </Button>
          <Select
            size="small"
            style={{ width: 130 }}
            value={r.status}
            options={statuses.map((s) => ({ label: s, value: s }))}
            onChange={(s) => statusMut.mutate({ id: r.id, s })}
          />
        </Space>
      ),
    },
  ]

  const { columns, components } = useResizableColumns(baseColumns, {
    orderNumber: 140,
    'customer.fullName': 160,
    'dealer.companyName': 160,
    totalAmount: 120,
    status: 120,
    orderDate: 170,
    a: 280,
  })

  const drawerLineBase: ColumnsType<OrderItem> = [
    { title: 'Product', dataIndex: 'productName', key: 'productName' },
    { title: 'Qty', dataIndex: 'quantity', key: 'quantity', width: 72 },
    {
      title: 'Price',
      dataIndex: 'unitPrice',
      key: 'unitPrice',
      render: (p: number) => formatRupee(p),
    },
  ]
  const { columns: drawerLineColumns, components: drawerLineComponents } = useResizableColumns(
    drawerLineBase,
    { productName: 200, quantity: 72, unitPrice: 120 },
  )

  const submitCreate = async () => {
    const v = await form.validateFields()
    await createMut.mutateAsync({
      dealerId: v.dealerId,
      customerId: v.customerId,
      items: v.items.map((i: { productId: number; quantity: number }) => ({
        productId: i.productId,
        quantity: i.quantity,
      })),
    })
  }

  const dealerOptions =
    dealersData?.content?.map((d: Dealer) => ({ label: d.companyName, value: d.id })) ?? []
  const customerOptions =
    customersData?.content?.map((c: Customer) => ({ label: c.fullName, value: c.id })) ?? []
  const productOptions =
    productsData?.content?.map((p: Product) => ({
      label: `${p.name} (${formatRupee(p.price)})`,
      value: p.id,
    })) ??
    []

  return (
    <div>
      <Space style={{ marginBottom: 16, width: '100%', justifyContent: 'space-between' }} wrap>
        <Title level={4} style={{ margin: 0 }}>
          Orders
        </Title>
        <Space wrap>
          <Select
            allowClear
            placeholder="Dealer"
            style={{ width: 200 }}
            options={dealerOptions}
            value={dealerId}
            onChange={(v) => {
              setDealerId(v)
              setPage(0)
            }}
          />
          <Select
            allowClear
            placeholder="Status"
            style={{ width: 160 }}
            options={statuses.map((s) => ({ label: s, value: s }))}
            value={status}
            onChange={(v) => {
              setStatus(v)
              setPage(0)
            }}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            New order
          </Button>
        </Space>
      </Space>
      <Table<Order>
        rowKey="id"
        tableLayout="fixed"
        components={components}
        sortDirections={TABLE_SORT_ASC_DESC}
        loading={isLoading}
        columns={columns}
        dataSource={data?.content ?? []}
        pagination={serverPaginationProps(data, { page, size, setPage, setSize })}
        onChange={serverTableOnChange<Order>({
          size,
          setPage,
          setSize,
          defaultSortField: DEFAULT_SORT_FIELD,
          defaultSortOrder: 'descend',
          sortFieldRef,
          sortOrderRef,
          setSortField,
          setSortOrder,
        })}
      />
      <Drawer
        title={drawerOrder?.orderNumber}
        open={!!drawerOrder}
        onClose={() => setDrawerOrder(null)}
        width={480}
      >
        {drawerOrder && (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Typography.Paragraph>
              <strong>Customer:</strong> {drawerOrder.customerName}
            </Typography.Paragraph>
            <Typography.Paragraph>
              <strong>Dealer:</strong> {drawerOrder.dealerCompanyName}
            </Typography.Paragraph>
            <Typography.Paragraph>
              <strong>Total:</strong> {formatRupee(drawerOrder.totalAmount)}
            </Typography.Paragraph>
            <Typography.Paragraph>
              <strong>Status:</strong> <OrderStatusBadge status={drawerOrder.status} />
            </Typography.Paragraph>
            <Table<OrderItem>
              size="small"
              tableLayout="fixed"
              components={drawerLineComponents}
              pagination={false}
              rowKey="id"
              dataSource={drawerOrder.items}
              columns={drawerLineColumns}
            />
          </Space>
        )}
      </Drawer>
      <Modal
        title="Create order"
        open={createOpen}
        onCancel={() => {
          setCreateOpen(false)
          form.resetFields()
        }}
        onOk={submitCreate}
        confirmLoading={createMut.isPending}
        width={640}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item name="dealerId" label="Dealer" rules={[{ required: true }]}>
            <Select options={dealerOptions} showSearch optionFilterProp="label" />
          </Form.Item>
          <Form.Item name="customerId" label="Customer" rules={[{ required: true }]}>
            <Select
              options={customerOptions}
              showSearch
              optionFilterProp="label"
              disabled={!watchedDealer}
            />
          </Form.Item>
          <Form.List name="items" initialValue={[{ productId: undefined, quantity: 1 }]}>
            {(fields, { add, remove }) => (
              <div>
                {fields.map((field, index) => (
                  <Space key={field.key} align="baseline" style={{ display: 'flex', marginBottom: 8 }}>
                    <Form.Item
                      {...field}
                      name={[field.name, 'productId']}
                      label={index === 0 ? 'Product' : undefined}
                      rules={[{ required: true, message: 'Pick product' }]}
                    >
                      <Select options={productOptions} style={{ width: 280 }} showSearch optionFilterProp="label" />
                    </Form.Item>
                    <Form.Item
                      {...field}
                      name={[field.name, 'quantity']}
                      label={index === 0 ? 'Qty' : undefined}
                      rules={[{ required: true, min: 1, type: 'number' }]}
                    >
                      <InputNumber min={1} />
                    </Form.Item>
                    <Button type="link" danger onClick={() => remove(field.name)}>
                      Remove
                    </Button>
                  </Space>
                ))}
                <Button type="dashed" onClick={() => add()} block>
                  Add line
                </Button>
              </div>
            )}
          </Form.List>
        </Form>
      </Modal>
    </div>
  )
}
