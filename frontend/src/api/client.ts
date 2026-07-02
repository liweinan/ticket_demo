/**
 * 通用 fetch 封装。
 * 所有 API 请求自动携带 X-Tenant-ID（除租户列表等公开接口）。
 */

/** 当前选中的租户 ID，由 App.tsx 维护 */
let currentTenantId = 'tenant-silver'

export function setCurrentTenantId(tenantId: string) {
  currentTenantId = tenantId
}

export function getCurrentTenantId() {
  return currentTenantId
}

async function request<T>(
  url: string,
  options: RequestInit = {},
  requireTenant = true,
): Promise<T> {
  const headers = new Headers(options.headers)
  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json')
  }
  if (requireTenant) {
    headers.set('X-Tenant-ID', currentTenantId)
  }

  const response = await fetch(url, { ...options, headers })

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`
    try {
      const body = await response.json()
      if (body.message) message = body.message
      else if (body.error) message = body.error
    } catch {
      // ignore
    }
    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }
  return response.json() as Promise<T>
}

export function getJson<T>(url: string, requireTenant = true) {
  return request<T>(url, { method: 'GET' }, requireTenant)
}

export function postJson<T>(url: string, body?: unknown, requireTenant = true) {
  return request<T>(
    url,
    { method: 'POST', body: body ? JSON.stringify(body) : undefined },
    requireTenant,
  )
}
