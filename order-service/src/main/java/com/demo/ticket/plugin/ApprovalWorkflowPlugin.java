package com.demo.ticket.plugin;

import com.demo.ticket.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 示例插件：金牌租户的「审批流」定制。
 * <p>
 * Demo 行为：quantity > 10 时要求走审批，直接拒绝下单。
 * 真实系统可创建审批单并异步回调。
 * </p>
 */
@Component
public class ApprovalWorkflowPlugin implements OrderPlugin {

    private static final Logger log = LoggerFactory.getLogger(ApprovalWorkflowPlugin.class);

    private static final int APPROVAL_THRESHOLD = 10;

    @Override
    public String id() {
        return "approval-workflow";
    }

    @Override
    public String description() {
        return "大批量购票需审批（Demo：数量>" + APPROVAL_THRESHOLD + " 时拦截）";
    }

    @Override
    public void beforeCreate(OrderPluginContext context) {
        if (context.getQuantity() > APPROVAL_THRESHOLD) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "购票数量超过 " + APPROVAL_THRESHOLD + "，已启用审批流插件，请联系管理员审批后下单");
        }
        // 写入插件标记到扩展快照
        context.setExtensionSnapshot("{\"approvalRequired\":false,\"plugin\":\"approval-workflow\"}");
        log.info("[Plugin/approval-workflow] 租户 {} 下单校验通过 qty={}",
                context.getTenantId(), context.getQuantity());
    }

    @Override
    public void afterCreate(Order order, OrderPluginContext context) {
        log.info("[Plugin/approval-workflow] 订单 {} 创建完成，可在此触发 OA 审批", order.getId());
    }
}
