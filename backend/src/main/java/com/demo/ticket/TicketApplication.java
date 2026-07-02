package com.demo.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 多租户订票 SaaS 样例应用入口。
 * <p>
 * 本项目重点演示以下架构设计要点：
 * <ul>
 *   <li>租户身份识别与上下文透传（{@code X-Tenant-ID}）</li>
 *   <li>共享表 + tenant_id 数据隔离</li>
 *   <li>租户等级限流（防噪声邻居）</li>
 *   <li>Redis 预占 + 数据库 CAS 库存扣减</li>
 *   <li>订单状态机</li>
 *   <li>Snowflake 分布式 ID</li>
 *   <li>JSON 扩展字段 + 插件化定制</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
public class TicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }
}
