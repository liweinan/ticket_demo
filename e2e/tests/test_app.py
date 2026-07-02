"""
多租户订票 SaaS Demo — Playwright E2E 测试。

依赖：
- backend :8080（H2 种子数据）
- frontend :5173

串行执行：部分用例会修改订单/库存状态。
"""

import re

import pytest
from playwright.sync_api import Page, expect


@pytest.fixture(autouse=True)
def goto_home(page: Page):
    """每个用例从首页开始。"""
    page.goto("/")
    expect(page.get_by_role("heading", name="多租户订票 SaaS Demo")).to_be_visible(
        timeout=30_000
    )


def _events_panel(page: Page):
    """活动列表面板（避免与订单列表中的同名标题冲突）。"""
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="票务活动"))


def _orders_panel(page: Page):
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="我的订单"))


def _select_tenant(page: Page, tenant_name: str) -> None:
    page.get_by_role("button", name=re.compile(tenant_name)).click()


def _first_event_card(page: Page):
    return _events_panel(page).locator(".event-card").first


def test_initial_load_silver_tenant(page: Page):
    """默认银牌租户：展示活动列表。"""
    expect(page.get_by_text("银牌 SaaS 租户")).to_be_visible()
    events = _events_panel(page)
    expect(events.get_by_role("heading", name="周末话剧 · 雷雨")).to_be_visible()
    expect(events.get_by_role("heading", name="亲子马戏团")).to_be_visible()


def test_switch_tenant_isolates_events(page: Page):
    """切换租户后活动列表变化（数据隔离）。"""
    events = _events_panel(page)
    expect(events.get_by_role("heading", name="周末话剧 · 雷雨")).to_be_visible()

    _select_tenant(page, "铜牌小微租户")
    expect(events.get_by_role("heading", name="社区电影之夜")).to_be_visible()
    expect(events.get_by_role("heading", name="周末话剧 · 雷雨")).not_to_be_visible()

    _select_tenant(page, "金牌企业租户")
    expect(events.get_by_role("heading", name=re.compile("春季演唱会"))).to_be_visible()
    expect(events.get_by_role("heading", name="社区电影之夜")).not_to_be_visible()


def test_create_order_and_pay_issue(page: Page):
    """下单 → 支付 → 出票，验证状态机流转与库存减少。"""
    _select_tenant(page, "银牌 SaaS 租户")

    event = _first_event_card(page)
    stock_text = event.locator(".event-stock strong")
    stock_before = int((stock_text.text_content() or "0").strip())

    event.get_by_role("button", name="订 1 张").click()

    orders = _orders_panel(page)
    expect(orders.get_by_text("待支付")).to_be_visible(timeout=15_000)
    expect(stock_text).to_have_text(str(stock_before - 1), timeout=15_000)

    orders.get_by_role("button", name="支付").first.click()
    expect(orders.get_by_text("已支付")).to_be_visible(timeout=15_000)

    orders.get_by_role("button", name="出票").first.click()
    expect(orders.get_by_text("已出票")).to_be_visible(timeout=15_000)


def test_gold_tenant_approval_plugin_blocks_bulk_order(page: Page):
    """金牌租户订 15 张触发审批流插件拦截。"""
    _select_tenant(page, "金牌企业租户")

    event = _first_event_card(page)
    event.get_by_role("button", name=re.compile("订 15 张")).click()

    expect(page.locator(".error-banner")).to_contain_text("审批", timeout=15_000)


def test_health_api(api_url: str, page: Page):
    """健康检查 API 可访问。"""
    response = page.request.get(f"{api_url}/api/health")
    assert response.ok
    body = response.json()
    assert body["status"] == "UP"


def test_tenants_api(api_url: str, page: Page):
    """租户列表 API 返回三个 Demo 租户。"""
    response = page.request.get(f"{api_url}/api/tenants")
    assert response.ok
    tenants = response.json()
    assert len(tenants) == 3
    ids = {t["tenantId"] for t in tenants}
    assert ids == {"tenant-gold", "tenant-silver", "tenant-bronze"}
