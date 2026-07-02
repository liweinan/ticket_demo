/**
 * 租户切换器 — 演示 X-Tenant-ID 透传与不同等级租户的差异。
 */

import type { Tenant } from '../types/ticket'

interface TenantSelectorProps {
  tenants: Tenant[]
  selectedId: string
  onChange: (tenantId: string) => void
}

const TIER_LABEL: Record<string, string> = {
  GOLD: '金牌',
  SILVER: '银牌',
  BRONZE: '铜牌',
}

const ISOLATION_LABEL: Record<string, string> = {
  DEDICATED_DB: '独立库',
  SHARED_SCHEMA: '独立Schema',
  SHARED_TABLE: '共享表',
}

export default function TenantSelector({
  tenants,
  selectedId,
  onChange,
}: TenantSelectorProps) {
  const current = tenants.find((t) => t.tenantId === selectedId)

  return (
    <section className="panel tenant-panel">
      <h2>租户切换</h2>
      <p className="hint">
        每个请求携带 <code>X-Tenant-ID</code> Header，后端据此隔离数据并限流
      </p>
      <div className="tenant-tabs">
        {tenants.map((tenant) => (
          <button
            key={tenant.tenantId}
            type="button"
            className={`tenant-tab ${tenant.tenantId === selectedId ? 'active' : ''} tier-${tenant.tier.toLowerCase()}`}
            onClick={() => onChange(tenant.tenantId)}
          >
            <span className="tenant-name">{tenant.name}</span>
            <span className="tenant-meta">
              {TIER_LABEL[tenant.tier]} · {ISOLATION_LABEL[tenant.isolationMode]}
            </span>
          </button>
        ))}
      </div>
      {current && (
        <div className="tenant-info">
          <span>QPS 上限: {current.maxQps}/s</span>
          {current.enabledPlugins && (
            <span>已启用插件: {current.enabledPlugins}</span>
          )}
        </div>
      )}
    </section>
  )
}
