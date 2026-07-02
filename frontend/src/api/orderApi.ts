import { getJson, postJson } from './client'
import type { CreateOrderRequest, Order } from '../types/ticket'

export function fetchOrders() {
  return getJson<Order[]>('/api/orders')
}

export function createOrder(body: CreateOrderRequest) {
  return postJson<Order>('/api/orders', body)
}

export function payOrder(orderId: string) {
  return postJson<Order>(`/api/orders/${orderId}/pay`)
}

export function issueTicket(orderId: string) {
  return postJson<Order>(`/api/orders/${orderId}/issue`)
}

export function cancelOrder(orderId: string) {
  return postJson<Order>(`/api/orders/${orderId}/cancel`)
}
