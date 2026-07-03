"""纯 API 测试 — 不启动浏览器，避免 Playwright 卡住。"""

import requests


def test_health_api(api_url: str, http: requests.Session):
    """健康检查 API 可访问。"""
    response = http.get(f"{api_url}/api/health", timeout=5)
    assert response.ok
    body = response.json()
    assert body["status"] == "UP"
    assert body["postgresUp"] is True
    assert body["redisUp"] is True


def test_tenants_api(api_url: str, http: requests.Session):
    """租户列表 API 返回三个 Demo 租户。"""
    response = http.get(f"{api_url}/api/tenants", timeout=5)
    assert response.ok
    tenants = response.json()
    assert len(tenants) == 3
    ids = {t["tenantId"] for t in tenants}
    assert ids == {"tenant-gold", "tenant-silver", "tenant-bronze"}
