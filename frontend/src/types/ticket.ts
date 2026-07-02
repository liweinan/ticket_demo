/** 租户付费等级 */
export type TenantTier = 'GOLD' | 'SILVER' | 'BRONZE'

/** 数据隔离模式（与后端 IsolationMode 对齐） */
export type IsolationMode = 'DEDICATED_DB' | 'SHARED_SCHEMA' | 'SHARED_TABLE'

export interface Tenant {
  tenantId: string
  name: string
  tier: TenantTier
  isolationMode: IsolationMode
  maxQps: number
  enabledPlugins: string
}

export interface TicketEvent {
  id: number
  tenantId: string
  title: string
  venue: string
  totalStock: number
  availableStock: number
  version: number
  extensionFields: string
}

/** 订单状态 — 与后端 OrderState 枚举一致 */
export type OrderState =
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'TICKET_ISSUED'
  | 'CANCELLED'
  | 'REFUNDED'

export interface Order {
  /** Snowflake ID — 必须用 string，避免 JS 大整数精度丢失 */
  id: string
  tenantId: string
  eventId: number
  eventTitle: string
  quantity: number
  state: OrderState
  createdAt: string
  updatedAt: string
  extensionSnapshot: string
}

export interface CreateOrderRequest {
  eventId: number
  quantity: number
}
