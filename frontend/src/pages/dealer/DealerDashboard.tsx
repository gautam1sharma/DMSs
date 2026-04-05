import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Table, Typography } from 'antd'
import { dashboardService } from '../../api/dashboardService'
import { formatRupee } from '../../utils/formatCurrency'
import { OrderStatusBadge } from '../../components/StatusBadge'
import type { ColumnsType } from 'antd/es/table'
import type { Order } from '../../types/models'
import dayjs from 'dayjs'

const { Title } = Typography

export default function DealerDashboard() {
  const { data, isLoading } = useQuery({
    queryKey: ['dealer-dashboard'],
    queryFn: () => dashboardService.dealerSummary(),
  })

  const columns: ColumnsType<Order> = [
    { title: 'Order #', dataIndex: 'orderNumber' },
    { title: 'Customer', dataIndex: 'customerName' },
    {
      title: 'Amount',
      dataIndex: 'totalAmount',
      render: (v: number) => formatRupee(v),
    },
    {
      title: 'Status',
      dataIndex: 'status',
      render: (s) => <OrderStatusBadge status={s} />,
    },
    {
      title: 'Date',
      dataIndex: 'orderDate',
      render: (d: string) => dayjs(d).format('YYYY-MM-DD HH:mm'),
    },
  ]

  return (
    <div>
      <Title level={4} style={{ marginTop: 0 }}>
        Dealer overview
      </Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={8}>
          <Card loading={isLoading}>
            <Statistic title="My customers" value={data?.customerCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card loading={isLoading}>
            <Statistic title="My orders" value={data?.orderCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card loading={isLoading}>
            <Statistic
              title="Revenue (my orders)"
              value={data?.revenueTotal ?? 0}
              formatter={(val) => formatRupee(Number(val))}
            />
          </Card>
        </Col>
      </Row>
      <Card title="Recent orders" style={{ marginTop: 24 }} loading={isLoading}>
        <Table
          rowKey="id"
          size="small"
          columns={columns}
          dataSource={data?.recentOrders ?? []}
          pagination={false}
        />
      </Card>
    </div>
  )
}
