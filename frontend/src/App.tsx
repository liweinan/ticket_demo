/**
 * 页面总控 — 多租户订票 SaaS Demo 前端入口。
 *
 * 职责：
 * 1. 加载租户列表，支持切换（模拟 X-Tenant-ID 透传）
 * 2. 展示当前租户的活动与订单
 * 3. 触发下单 / 支付 / 出票 / 取消，观察状态机与库存变化
 */

import { useCallback, useEffect, useState } from 'react'
import { setCurrentTenantId } from './api/client'
import { fetchEvents } from './api/eventApi'
import {
  cancelOrder,
  createOrder,
  fetchOrders,
  issueTicket,
  payOrder,
} from './api/orderApi'
import { fetchTenants } from './api/tenantApi'
import EventList from './components/EventList'
import OrderList from './components/OrderList'
import TenantSelector from './components/TenantSelector'
import type { Order, Tenant, TicketEvent } from './types/ticket'
import './App.css'

function App() {
  const [tenants, setTenants] = useState<Tenant[]>([])
  const [selectedTenantId, setSelectedTenantId] = useState('tenant-silver')
  const [events, setEvents] = useState<TicketEvent[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [loadingEvents, setLoadingEvents] = useState(true)
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [orderingEventId, setOrderingEventId] = useState<number | null>(null)
  const [actingOrderId, setActingOrderId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  /** 加载当前租户的活动与订单（切换租户或操作后刷新） */
  const loadTenantData = useCallback(async (tenantId: string) => {
    setCurrentTenantId(tenantId)
    setLoadingEvents(true)
    setLoadingOrders(true)
    setError(null)

    try {
      const [eventData, orderData] = await Promise.all([
        fetchEvents(),
        fetchOrders(),
      ])
      setEvents(eventData)
      setOrders(orderData)
    } catch (err) {
      setError(err instanceof Error ? err.message : '加载失败')
    } finally {
      setLoadingEvents(false)
      setLoadingOrders(false)
    }
  }, [])

  // 首次加载租户列表
  useEffect(() => {
    fetchTenants()
      .then(setTenants)
      .catch((err) =>
        setError(err instanceof Error ? err.message : '加载租户失败'),
      )
  }, [])

  // 切换租户时重新拉数据
  useEffect(() => {
    void loadTenantData(selectedTenantId)
  }, [selectedTenantId, loadTenantData])

  const handleTenantChange = (tenantId: string) => {
    setSelectedTenantId(tenantId)
  }

  const handleOrder = async (eventId: number, quantity: number) => {
    setOrderingEventId(eventId)
    setError(null)
    try {
      await createOrder({ eventId, quantity })
      await loadTenantData(selectedTenantId)
    } catch (err) {
      setError(err instanceof Error ? err.message : '下单失败')
    } finally {
      setOrderingEventId(null)
    }
  }

  const handlePay = async (orderId: string) => {
    setActingOrderId(orderId)
    setError(null)
    try {
      await payOrder(orderId)
      await loadTenantData(selectedTenantId)
    } catch (err) {
      setError(err instanceof Error ? err.message : '支付失败')
    } finally {
      setActingOrderId(null)
    }
  }

  const handleIssue = async (orderId: string) => {
    setActingOrderId(orderId)
    setError(null)
    try {
      await issueTicket(orderId)
      await loadTenantData(selectedTenantId)
    } catch (err) {
      setError(err instanceof Error ? err.message : '出票失败')
    } finally {
      setActingOrderId(null)
    }
  }

  const handleCancel = async (orderId: string) => {
    setActingOrderId(orderId)
    setError(null)
    try {
      await cancelOrder(orderId)
      await loadTenantData(selectedTenantId)
    } catch (err) {
      setError(err instanceof Error ? err.message : '取消失败')
    } finally {
      setActingOrderId(null)
    }
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>多租户订票 SaaS Demo</h1>
        <p>React + Spring Boot · 数据隔离 · 限流 · 库存预占 · 状态机 · 插件化</p>
      </header>

      <TenantSelector
        tenants={tenants}
        selectedId={selectedTenantId}
        onChange={handleTenantChange}
      />

      {error && <div className="error-banner">{error}</div>}

      <div className="main-grid">
        <section className="panel">
          <h2>票务活动</h2>
          <EventList
            events={events}
            loading={loadingEvents}
            onOrder={handleOrder}
            orderingEventId={orderingEventId}
          />
        </section>

        <section className="panel">
          <h2>我的订单</h2>
          <OrderList
            orders={orders}
            loading={loadingOrders}
            onPay={handlePay}
            onIssue={handleIssue}
            onCancel={handleCancel}
            actingOrderId={actingOrderId}
          />
        </section>
      </div>
    </div>
  )
}

export default App
