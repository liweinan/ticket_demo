/**
 * 订单列表 — 展示 Snowflake ID 与状态机驱动的操作按钮。
 */

import type { Order, OrderState } from '../types/ticket'

interface OrderListProps {
  orders: Order[]
  loading: boolean
  onPay: (orderId: string) => void
  onIssue: (orderId: string) => void
  onCancel: (orderId: string) => void
  actingOrderId: string | null
}

const STATE_LABEL: Record<OrderState, string> = {
  PENDING_PAYMENT: '待支付',
  PAID: '已支付',
  TICKET_ISSUED: '已出票',
  CANCELLED: '已取消',
  REFUNDED: '已退款',
}

export default function OrderList({
  orders,
  loading,
  onPay,
  onIssue,
  onCancel,
  actingOrderId,
}: OrderListProps) {
  if (loading) {
    return <p className="hint">加载订单中...</p>
  }

  if (orders.length === 0) {
    return <p className="hint">暂无订单，请在左侧活动列表下单</p>
  }

  return (
    <ul className="order-list">
      {orders.map((order) => (
        <li key={order.id} className={`order-card state-${order.state.toLowerCase()}`}>
          <div className="order-header">
            <span className="order-id">#{order.id}</span>
            <span className={`order-state state-${order.state.toLowerCase()}`}>
              {STATE_LABEL[order.state]}
            </span>
          </div>
          <p className="order-title">{order.eventTitle}</p>
          <p className="order-meta">
            数量: {order.quantity} · 创建于 {new Date(order.createdAt).toLocaleString()}
          </p>
          <div className="order-actions">
            {order.state === 'PENDING_PAYMENT' && (
              <>
                <button
                  type="button"
                  disabled={actingOrderId === order.id}
                  onClick={() => onPay(order.id)}
                >
                  支付
                </button>
                <button
                  type="button"
                  className="btn-secondary"
                  disabled={actingOrderId === order.id}
                  onClick={() => onCancel(order.id)}
                >
                  取消
                </button>
              </>
            )}
            {order.state === 'PAID' && (
              <button
                type="button"
                disabled={actingOrderId === order.id}
                onClick={() => onIssue(order.id)}
              >
                出票
              </button>
            )}
          </div>
        </li>
      ))}
    </ul>
  )
}
