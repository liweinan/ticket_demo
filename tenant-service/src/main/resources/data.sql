-- 幂等种子数据：重复启动不插入重复行
INSERT INTO tenants (tenant_id, name, tier, isolation_mode, max_qps, enabled_plugins) VALUES
('tenant-gold',   '金牌企业租户',   'GOLD',   'DEDICATED_DB',  100, 'approval-workflow'),
('tenant-silver', '银牌 SaaS 租户', 'SILVER', 'SHARED_SCHEMA',  30, ''),
('tenant-bronze', '铜牌小微租户',   'BRONZE', 'SHARED_TABLE',   10, '')
ON CONFLICT (tenant_id) DO NOTHING;
