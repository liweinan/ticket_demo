"""Playwright E2E 公共 fixture 与配置。"""

import os
import time

import pytest
import requests
from playwright.sync_api import sync_playwright

API_URL = os.getenv("API_URL", "http://localhost:8080")
BASE_URL_CANDIDATES = [
    u for u in dict.fromkeys([
        os.getenv("BASE_URL"),
        "http://localhost:51730",
        "http://localhost:5173",
    ]) if u
]


def _wait_api_ready(api_url: str) -> None:
    session = requests.Session()
    session.trust_env = False
    deadline = time.time() + 90
    last_error = None
    while time.time() < deadline:
        try:
            health = session.get(f"{api_url}/api/health", timeout=3)
            if health.ok:
                return
            last_error = f"health={health.status_code}"
        except requests.RequestException as exc:
            last_error = str(exc)
        time.sleep(2)
    raise RuntimeError(f"API 未在 90s 内就绪: {last_error}")


def _is_ticket_demo_frontend(url: str) -> bool:
    try:
        with sync_playwright() as playwright:
            browser = playwright.chromium.launch(headless=True)
            page = browser.new_page()
            page.goto(url, wait_until="domcontentloaded", timeout=30_000)
            page.wait_for_selector("h1", timeout=15_000)
            heading = page.locator("h1").inner_text(timeout=5_000)
            browser.close()
            return "多租户订票" in heading
    except Exception:
        return False


def _resolve_base_url() -> str:
    session = requests.Session()
    session.trust_env = False
    deadline = time.time() + 60
    last_error = None

    while time.time() < deadline:
        for candidate in BASE_URL_CANDIDATES:
            try:
                if session.get(candidate, timeout=3).status_code >= 500:
                    continue
                if _is_ticket_demo_frontend(candidate):
                    return candidate
                last_error = f"非 ticket_demo 前端: {candidate}"
            except requests.RequestException as exc:
                last_error = str(exc)
        time.sleep(2)

    raise RuntimeError(
        f"未找到 ticket_demo 前端。最后错误: {last_error}。"
        "Docker: docker compose up -d frontend → http://localhost:51730 ；"
        "本地: cd ticket_demo && pnpm dev → http://localhost:5173（勿与其他 Vite 项目共用端口）。"
    )


@pytest.fixture(scope="session", autouse=True)
def wait_for_api(api_url: str):
    """所有用例仅等待 Gateway API 就绪。"""
    _wait_api_ready(api_url)


@pytest.fixture(scope="session")
def api_url() -> str:
    return API_URL


@pytest.fixture(scope="session")
def base_url() -> str:
    """UI 用例专用：解析 ticket_demo 前端 URL。"""
    return _resolve_base_url()


@pytest.fixture(scope="session")
def http() -> requests.Session:
    session = requests.Session()
    session.trust_env = False
    return session
