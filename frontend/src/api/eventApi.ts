import { getJson } from './client'
import type { TicketEvent } from '../types/ticket'

/** 当前租户下的活动列表 */
export function fetchEvents() {
  return getJson<TicketEvent[]>('/api/events')
}
