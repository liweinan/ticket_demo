"""
生成 README 用截图，输出到 docs/screenshots/。

运行（需 backend :8080 + frontend :5173 已启动）：
  cd e2e && NO_PROXY='*' uv run pytest tests/capture_screenshots.py -v
  或项目根目录：pnpm capture-screenshots
"""

import re
from pathlib import Path

import pytest
from playwright.sync_api import Page, expect

SCREENSHOT_DIR = Path(__file__).resolve().parents[2] / "docs" / "screenshots"


def _events_panel(page: Page):
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="票务活动"))


def _orders_panel(page: Page):
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="我的订单"))


def _select_tenant(page: Page, name: str) -> None:
    page.get_by_role("button", name=re.compile(name)).click()


@pytest.fixture(scope="module", autouse=True)
def ensure_screenshot_dir():
    SCREENSHOT_DIR.mkdir(parents=True, exist_ok=True)


class TestCaptureScreenshots:
    def test_01_initial_silver_tenant(self, page: Page, base_url: str):
        page.goto(base_url)
        expect(page.get_by_role("heading", name="多租户订票 SaaS Demo")).to_be_visible()
        expect(_events_panel(page).get_by_role("heading", name="周末话剧 · 雷雨")).to_be_visible()
        page.screenshot(path=SCREENSHOT_DIR / "01-initial-silver.png", full_page=True)

    def test_02_gold_tenant_events(self, page: Page, base_url: str):
        page.goto(base_url)
        _select_tenant(page, "金牌企业租户")
        events = _events_panel(page)
        expect(events.get_by_role("heading", name=re.compile("春季演唱会"))).to_be_visible()
        page.screenshot(path=SCREENSHOT_DIR / "02-gold-tenant.png", full_page=True)

    def test_03_after_order_pending_payment(self, page: Page, base_url: str):
        page.goto(base_url)
        _select_tenant(page, "银牌 SaaS 租户")
        _events_panel(page).locator(".event-card").first.get_by_role("button", name="订 1 张").click()
        expect(_orders_panel(page).get_by_text("待支付")).to_be_visible(timeout=15_000)
        page.screenshot(path=SCREENSHOT_DIR / "03-after-order.png", full_page=True)

    def test_04_after_pay_and_issue(self, page: Page, base_url: str):
        page.goto(base_url)
        _select_tenant(page, "银牌 SaaS 租户")
        orders = _orders_panel(page)
        orders.get_by_role("button", name="支付").first.click()
        expect(orders.get_by_text("已支付")).to_be_visible(timeout=15_000)
        orders.get_by_role("button", name="出票").first.click()
        expect(orders.get_by_text("已出票")).to_be_visible(timeout=15_000)
        page.screenshot(path=SCREENSHOT_DIR / "04-after-issue.png", full_page=True)

    def test_05_approval_plugin_blocked(self, page: Page, base_url: str):
        page.goto(base_url)
        _select_tenant(page, "金牌企业租户")
        _events_panel(page).locator(".event-card").first.get_by_role(
            "button", name=re.compile("订 15 张")
        ).click()
        expect(page.locator(".error-banner")).to_contain_text("审批", timeout=15_000)
        page.screenshot(path=SCREENSHOT_DIR / "05-approval-blocked.png", full_page=True)
