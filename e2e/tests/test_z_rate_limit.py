"""限流测试 — 放在最后执行，避免影响 UI 用例。"""

import requests


def test_rate_limit_returns_429(api_url: str, http: requests.Session):
    """Gateway Resilience4j 租户限流：超出 QPS 返回 429。"""
    headers = {"X-Tenant-ID": "tenant-bronze"}
    statuses = [
        http.get(f"{api_url}/api/events", headers=headers, timeout=5).status_code
        for _ in range(25)
    ]
    assert 429 in statuses, f"期望出现 429，实际状态码: {statuses}"
