-- 幂等种子：按 tenant_id + title 去重（需先清理历史重复数据）
DELETE FROM ticket_events te
WHERE te.id NOT IN (
    SELECT MIN(id) FROM ticket_events GROUP BY tenant_id, title
);

INSERT INTO ticket_events (tenant_id, title, venue, total_stock, available_stock, version, extension_fields)
SELECT v.tenant_id, v.title, v.venue, v.total_stock, v.available_stock, v.version, v.extension_fields::jsonb
FROM (VALUES
    ('tenant-gold',   '2026 春季演唱会 · 北京站', '国家体育场', 500, 500, 0, '{"seatMap":"vip-a","requireRealName":true}'),
    ('tenant-gold',   '企业年会晚宴',             '国贸大酒店',   200, 200, 0, '{"dressCode":"formal"}'),
    ('tenant-silver', '周末话剧 · 雷雨',          '人民艺术剧院', 300, 300, 0, '{"language":"zh-CN"}'),
    ('tenant-silver', '亲子马戏团',               '市体育馆',     800, 800, 0, '{"minAge":3}'),
    ('tenant-bronze', '社区电影之夜',             '社区活动中心',  50,  50, 0, '{"bringSnacks":true}'),
    ('tenant-bronze', '露天音乐节',               '滨江公园',     1000, 1000, 0, '{}')
) AS v(tenant_id, title, venue, total_stock, available_stock, version, extension_fields)
WHERE NOT EXISTS (
    SELECT 1 FROM ticket_events e WHERE e.tenant_id = v.tenant_id AND e.title = v.title
);
