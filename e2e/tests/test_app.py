"""
多租户订票 SaaS Demo — Playwright UI E2E 测试。

依赖：gateway-service :8080、frontend :5173
"""

import re

import pytest
from playwright.sync_api import Page, expect


@pytest.fixture(autouse=True)
def goto_home(request, page: Page):
    """仅 UI 用例（声明了 page 参数）才打开首页。"""
    if "page" not in request.fixturenames:
        yield
        return
    page.goto("/", wait_until="domcontentloaded")
    expect(page.get_by_role("heading", name="多租户订票 SaaS Demo")).to_be_visible(
        timeout=30_000
    )
    yield


def _events_panel(page: Page):
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="票务活动"))


def _orders_panel(page: Page):
    return page.locator(".panel").filter(has=page.get_by_role("heading", name="我的订单"))


def _select_tenant(page: Page, tenant_name: str) -> None:
    page.get_by_role("button", name=re.compile(tenant_name)).click()
    # 等待租户切换后活动列表刷新（避免读到上一租户缓存）
    expect(_events_panel(page).locator(".event-card").first).to_be_visible(timeout=15_000)


def _wait_tenant_events(page: Page, event_title: str | re.Pattern[str]) -> None:
    events = _events_panel(page)
    expect(events.get_by_role("heading", name=event_title)).to_be_visible(timeout=15_000)


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
    _wait_tenant_events(page, "社区电影之夜")
    expect(events.get_by_role("heading", name="周末话剧 · 雷雨")).not_to_be_visible()

    _select_tenant(page, "金牌企业租户")
    _wait_tenant_events(page, re.compile("春季演唱会"))
    expect(events.get_by_role("heading", name="社区电影之夜")).not_to_be_visible()

    # 恢复默认租户，减少后续用例干扰
    _select_tenant(page, "银牌 SaaS 租户")
    _wait_tenant_events(page, "周末话剧 · 雷雨")


def test_create_order_and_pay_issue(page: Page):
    """下单 → 支付 → 出票，验证状态机流转与库存减少。"""
    _select_tenant(page, "银牌 SaaS 租户")
    _wait_tenant_events(page, "周末话剧 · 雷雨")

    events = _events_panel(page)
    event = events.locator(".event-card").filter(
        has=page.get_by_role("heading", name="周末话剧 · 雷雨")
    )
    expect(event).to_be_visible()

    stock_text = event.locator(".event-stock strong")
    stock_before = int((stock_text.text_content() or "0").strip())

    orders = _orders_panel(page)
    pending = orders.locator(".order-card").filter(
        has_text="周末话剧 · 雷雨"
    ).filter(has=page.locator(".state-pending_payment"))
    pending_count_before = pending.count()

    event.get_by_role("button", name=re.compile(r"订 1 张")).click()

    expect(pending).to_have_count(pending_count_before + 1, timeout=15_000)
    # 订单按 createdAt 降序，第一条即刚创建的订单
    new_order = pending.first
    order_id = (new_order.locator(".order-id").inner_text() or "").strip()
    expect(new_order).to_be_visible(timeout=15_000)
    expect(stock_text).to_have_text(str(stock_before - 1), timeout=15_000)

    new_order.get_by_role("button", name="支付").click()
    paid_order = orders.locator(".order-card").filter(has_text=order_id)
    expect(paid_order).to_have_class(re.compile(r"state-paid"), timeout=15_000)

    paid_order.get_by_role("button", name="出票").click()
    expect(paid_order).to_have_class(re.compile(r"state-ticket_issued"), timeout=15_000)


def test_gold_tenant_approval_plugin_blocks_bulk_order(page: Page):
    """金牌租户订 15 张触发审批流插件拦截。"""
    _select_tenant(page, "金牌企业租户")
    _wait_tenant_events(page, re.compile("春季演唱会"))

    event = _events_panel(page).locator(".event-card").filter(
        has=page.get_by_role("heading", name=re.compile("春季演唱会"))
    )
    expect(event.get_by_role("button", name=re.compile("订 15 张"))).to_be_enabled()
    event.get_by_role("button", name=re.compile("订 15 张")).click()

    banner = page.locator(".error-banner")
    expect(banner).to_be_visible(timeout=15_000)
    expect(banner).to_contain_text("审批", timeout=15_000)
