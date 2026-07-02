"""Playwright E2E 公共 fixture 与配置。"""

import os
import time

import pytest
import requests

# 本地默认 localhost；Docker Compose e2e 服务中设为 http://frontend:5173
BASE_URL = os.getenv("BASE_URL", "http://localhost:5173")
API_URL = os.getenv("API_URL", "http://localhost:8080")


@pytest.fixture(scope="session")
def base_url() -> str:
    """pytest-playwright / pytest-base-url 读取此 fixture 作为 page.goto('/') 的前缀。"""
    return BASE_URL


@pytest.fixture(scope="session", autouse=True)
def wait_for_services(base_url: str, api_url: str):
    """等待前后端就绪（容器冷启动时 Vite 可能较慢）。"""
    deadline = time.time() + 90
    last_error = None
    session = requests.Session()
    session.trust_env = False  # 不继承 http_proxy，避免 localhost 走代理
    while time.time() < deadline:
        try:
            health = session.get(f"{api_url}/api/health", timeout=3)
            if not health.ok:
                last_error = f"health={health.status_code}"
                time.sleep(2)
                continue
            front = session.get(base_url, timeout=3, headers={"Host": "localhost"})
            if front.status_code < 500:
                return
            last_error = f"health={health.status_code} front={front.status_code}"
        except requests.RequestException as exc:
            last_error = str(exc)
        time.sleep(2)
    raise RuntimeError(f"服务未在 90s 内就绪: {last_error}")


@pytest.fixture(scope="session")
def api_url() -> str:
    return API_URL


@pytest.fixture(scope="session")
def http() -> requests.Session:
    """共享 HTTP 会话，API 测试不走代理。"""
    session = requests.Session()
    session.trust_env = False
    return session
