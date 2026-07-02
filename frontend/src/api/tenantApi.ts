import { getJson } from './client'
import type { Tenant } from '../types/ticket'

/** 获取所有 Demo 租户（不需要 X-Tenant-ID） */
export function fetchTenants() {
  return getJson<Tenant[]>('/api/tenants', false)
}
