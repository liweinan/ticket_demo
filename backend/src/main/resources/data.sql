-- ============================================================
-- 种子数据：演示三种租户等级 + 不同隔离模式配置
-- 实际运行时 Demo 仅实现「共享表 + tenant_id」读写隔离
-- ============================================================

-- 租户元数据（隔离模式在此声明，供运维/路由层参考）
INSERT INTO tenants (tenant_id, name, tier, isolation_mode, max_qps, enabled_plugins) VALUES
('tenant-gold',   '金牌企业租户',   'GOLD',   'DEDICATED_DB',  100, 'approval-workflow'),
('tenant-silver', '银牌 SaaS 租户', 'SILVER', 'SHARED_SCHEMA',  30, ''),
('tenant-bronze', '铜牌小微租户',   'BRONZE', 'SHARED_TABLE',   10, '');

-- 票务活动（共享表，通过 tenant_id 逻辑隔离）
INSERT INTO ticket_events (tenant_id, title, venue, total_stock, available_stock, version, extension_fields) VALUES
('tenant-gold',   '2026 春季演唱会 · 北京站', '国家体育场', 500, 500, 0, '{"seatMap":"vip-a","requireRealName":true}'),
('tenant-gold',   '企业年会晚宴',             '国贸大酒店',   200, 200, 0, '{"dressCode":"formal"}'),
('tenant-silver', '周末话剧 · 雷雨',          '人民艺术剧院', 300, 300, 0, '{"language":"zh-CN"}'),
('tenant-silver', '亲子马戏团',               '市体育馆',     800, 800, 0, '{"minAge":3}'),
('tenant-bronze', '社区电影之夜',             '社区活动中心',  50,  50, 0, '{"bringSnacks":true}'),
('tenant-bronze', '露天音乐节',               '滨江公园',     1000, 1000, 0, '{}');
