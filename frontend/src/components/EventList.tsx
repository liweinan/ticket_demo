/**
 * 活动列表 — 展示库存与 JSON 扩展字段。
 */

import type { TicketEvent } from '../types/ticket'

interface EventListProps {
  events: TicketEvent[]
  loading: boolean
  onOrder: (eventId: number, quantity: number) => void
  orderingEventId: number | null
}

export default function EventList({
  events,
  loading,
  onOrder,
  orderingEventId,
}: EventListProps) {
  if (loading) {
    return <p className="hint">加载活动中...</p>
  }

  if (events.length === 0) {
    return <p className="hint">当前租户暂无活动</p>
  }

  return (
    <ul className="event-list">
      {events.map((event) => (
        <li key={event.id} className="event-card">
          <div className="event-main">
            <h3>{event.title}</h3>
            <p className="event-venue">{event.venue}</p>
            <p className="event-stock">
              剩余 <strong>{event.availableStock}</strong> / {event.totalStock} 张
              <span className="version-tag">v{event.version}</span>
            </p>
            {event.extensionFields && event.extensionFields !== '{}' && (
              <pre className="extension-fields">
                扩展字段: {event.extensionFields}
              </pre>
            )}
          </div>
          <div className="event-actions">
            <button
              type="button"
              disabled={orderingEventId === event.id || event.availableStock < 1}
              onClick={() => onOrder(event.id, 1)}
            >
              {orderingEventId === event.id ? '下单中...' : '订 1 张'}
            </button>
            <button
              type="button"
              className="btn-secondary"
              disabled={orderingEventId === event.id || event.availableStock < 5}
              onClick={() => onOrder(event.id, 5)}
            >
              订 5 张
            </button>
            <button
              type="button"
              className="btn-warn"
              disabled={orderingEventId === event.id || event.availableStock < 15}
              onClick={() => onOrder(event.id, 15)}
              title="金牌租户启用了审批流插件，>10 张会被拦截"
            >
              订 15 张（测插件）
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}
