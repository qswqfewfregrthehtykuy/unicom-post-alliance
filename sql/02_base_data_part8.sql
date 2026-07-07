-- ============================================================
-- 14. 操作日志（40条，各模块操作记录）
-- ============================================================
INSERT INTO sys_operation_log (id, user_id, username, role, module, action, target_id, target_type, result, ip_address, created_at) VALUES
( 1,  1, '13800000001', 'ROLE_PROVINCE', 'AUTH',    '用户登录',           NULL, NULL,          'SUCCESS', '10.0.1.1',   '2026-06-01 08:00:00'),
( 2,  2, '13800000002', 'ROLE_CITY',     'AUTH',    '用户登录',           NULL, NULL,          'SUCCESS', '10.0.2.1',   '2026-06-01 08:30:00'),
( 3, 13, '13800000013', 'ROLE_OUTLET',   'AUTH',    '用户登录',           NULL, NULL,          'SUCCESS', '10.0.3.1',   '2026-06-01 09:00:00'),
( 4, 43, '13900000001', 'ROLE_DEVELOPER','AUTH',    '用户登录',           NULL, NULL,          'SUCCESS', '10.0.4.1',   '2026-06-01 09:30:00'),
( 5,  1, '13800000001', 'ROLE_PROVINCE', 'DEVELOPER','审核发展人申请',     1, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.1.1',   '2026-05-15 10:00:00'),
( 6,  1, '13800000001', 'ROLE_PROVINCE', 'DEVELOPER','审核发展人申请',     2, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.1.1',   '2026-05-16 11:00:00'),
( 7, 13, '13800000013', 'ROLE_OUTLET',   'DEVELOPER','审核发展人申请',    36, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.3.1',   '2026-06-25 10:00:00'),
( 8, 16, '13800000016', 'ROLE_OUTLET',   'DEVELOPER','审核发展人申请',    36, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.3.2',   '2026-06-25 10:05:00'),
( 9, 20, '13800000020', 'ROLE_OUTLET',   'DEVELOPER','拒绝发展人申请',    44, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.3.3',   '2026-06-15 10:00:00'),
(10, 27, '13800000027', 'ROLE_OUTLET',   'DEVELOPER','拒绝发展人申请',    45, 'DEVELOPER_APPLY', 'SUCCESS', '10.0.3.4',   '2026-06-16 11:00:00'),
(11,  1, '13800000001', 'ROLE_PROVINCE', 'ORDER',    '意向单审核(通过)',    1, 'ORDER',          'SUCCESS', '10.0.1.1',   '2026-05-02 10:00:00'),
(12, 13, '13800000013', 'ROLE_OUTLET',   'ORDER',    '意向单审核(通过)',    1, 'ORDER',          'SUCCESS', '10.0.3.1',   '2026-05-02 09:30:00'),
(13,  1, '13800000001', 'ROLE_PROVINCE', 'ORDER',    '转正单审核(通过)',    1, 'ORDER',          'SUCCESS', '10.0.1.1',   '2026-05-05 10:00:00'),
(14, 14, '13800000014', 'ROLE_OUTLET',   'ORDER',    '意向单审核(通过)',    3, 'ORDER',          'SUCCESS', '10.0.3.2',   '2026-05-04 09:00:00'),
(15, 25, '13800000025', 'ROLE_OUTLET',   'ORDER',    '意向单审核(通过)',    8, 'ORDER',          'SUCCESS', '10.0.3.5',   '2026-05-09 10:00:00'),
(16, 27, '13800000027', 'ROLE_OUTLET',   'ORDER',    '意向单审核(通过)',    9, 'ORDER',          'SUCCESS', '10.0.3.6',   '2026-05-10 14:00:00'),
(17, 24, '13800000024', 'ROLE_OUTLET',   'ORDER',    '拒绝意向单',         35, 'ORDER',          'SUCCESS', '10.0.3.7',   '2026-06-26 10:00:00'),
(18,  1, '13800000001', 'ROLE_PROVINCE', 'ORDER',    '转正单审核(通过)',   11, 'ORDER',          'SUCCESS', '10.0.1.1',   '2026-06-05 10:00:00'),
(19,  1, '13800000001', 'ROLE_PROVINCE', 'ORDER',    '转正单审核(通过)',   12, 'ORDER',          'SUCCESS', '10.0.1.1',   '2026-06-06 14:00:00'),
(20,  1, '13800000001', 'ROLE_PROVINCE', 'ORDER',    '转正单审核(通过)',   15, 'ORDER',          'SUCCESS', '10.0.1.1',   '2026-06-09 10:00:00'),
(21,  1, '13800000001', 'ROLE_PROVINCE', 'COMMISSION','佣金规则创建',       1, 'COMMISSION_RULE','SUCCESS', '10.0.1.1',   '2026-01-01 09:00:00'),
(22,  1, '13800000001', 'ROLE_PROVINCE', 'COMMISSION','佣金规则创建',      13, 'COMMISSION_RULE','SUCCESS', '10.0.1.1',   '2026-06-30 10:00:00'),
(23,  1, '13800000001', 'ROLE_PROVINCE', 'COMMISSION','佣金规则创建',      25, 'COMMISSION_RULE','SUCCESS', '10.0.1.1',   '2026-06-01 09:00:00'),
(24, 13, '13800000013', 'ROLE_OUTLET',   'ORDER',    '创建订单',           1, 'ORDER',          'SUCCESS', '10.0.3.1',   '2026-05-01 09:00:00'),
(25, 13, '13800000013', 'ROLE_OUTLET',   'ORDER',    '创建订单',           2, 'ORDER',          'SUCCESS', '10.0.3.1',   '2026-05-02 10:00:00'),
(26, 43, '13900000001', 'ROLE_DEVELOPER','ORDER',    '创建订单',          11, 'ORDER',          'SUCCESS', '10.0.4.1',   '2026-06-01 09:00:00'),
(27, 44, '13900000002', 'ROLE_DEVELOPER','ORDER',    '创建订单',          13, 'ORDER',          'SUCCESS', '10.0.4.2',   '2026-06-03 08:00:00'),
(28, 49, '13900000007', 'ROLE_DEVELOPER','ORDER',    '创建订单',          15, 'ORDER',          'SUCCESS', '10.0.4.3',   '2026-06-05 09:00:00'),
(29, 54, '13900000012', 'ROLE_DEVELOPER','ORDER',    '创建订单',          20, 'ORDER',          'SUCCESS', '10.0.4.4',   '2026-06-10 10:00:00'),
(30, 64, '13900000022', 'ROLE_DEVELOPER','ORDER',    '创建订单',          30, 'ORDER',          'SUCCESS', '10.0.4.5',   '2026-06-20 08:00:00'),
(31,  1, '13800000001', 'ROLE_PROVINCE', 'SYSTEM',  '创建用户',          43, 'SYS_USER',       'SUCCESS', '10.0.1.1',   '2026-05-15 10:01:00'),
(32,  1, '13800000001', 'ROLE_PROVINCE', 'SYSTEM',  '创建用户',          44, 'SYS_USER',       'SUCCESS', '10.0.1.1',   '2026-05-16 11:01:00'),
(33,  1, '13800000001', 'ROLE_PROVINCE', 'SYSTEM',  '修改佣金规则',       1, 'COMMISSION_RULE','SUCCESS', '10.0.1.1',   '2026-05-01 10:00:00'),
(34,  2, '13800000002', 'ROLE_CITY',     'SYSTEM',  '查看统计报表',       NULL, NULL,          'SUCCESS', '10.0.2.1',   '2026-06-15 14:00:00'),
(35,  8, '13800000008', 'ROLE_CITY',     'SYSTEM',  '查看统计报表',       NULL, NULL,          'SUCCESS', '10.0.2.2',   '2026-06-16 15:00:00'),
(36,  1, '13800000001', 'ROLE_PROVINCE', 'SYSTEM',  '数据导出',           NULL, 'EXPORT',       'SUCCESS', '10.0.1.1',   '2026-06-20 16:00:00'),
(37, 13, '13800000013', 'ROLE_OUTLET',   'AUTH',    '登录失败(密码错误)',  NULL, NULL,          'FAIL',    '10.0.3.1',   '2026-06-21 08:30:00'),
(38, 43, '13900000001', 'ROLE_DEVELOPER','AUTH',    '登录失败(密码错误)',  NULL, NULL,          'FAIL',    '10.0.4.1',   '2026-06-22 09:00:00'),
(39,  1, '13800000001', 'ROLE_PROVINCE', 'SYSTEM',  '创建网点',           1, 'BIZ_OUTLET',     'SUCCESS', '10.0.1.1',   '2026-01-15 10:00:00'),
(40,  1, '13800000001', 'ROLE_PROVINCE', 'AUTH',    '用户登出',           NULL, NULL,          'SUCCESS', '10.0.1.1',   '2026-07-01 18:00:00');

-- ============================================================
-- 15. 业务审核日志（35条，记录各级审核历史）
-- ============================================================
INSERT INTO biz_audit_log (id, target_type, target_id, audit_level, audit_phase, auditor_id, auditor_name, audit_status, audit_remark, audit_time) VALUES
-- 发展人申请审核记录
( 1, 'DEVELOPER_APPLY',  1, 'OUTLET',   NULL, 13, '张网点01', 'APPROVED', '资料齐全，初审通过',      '2026-05-14 10:00:00'),
( 2, 'DEVELOPER_APPLY',  1, 'CITY',     NULL,  2, '李南昌',   'APPROVED', '市级审核通过',            '2026-05-14 14:00:00'),
( 3, 'DEVELOPER_APPLY',  1, 'PROVINCE', NULL,  1, '张省管',   'APPROVED', '审核通过，准予入网',      '2026-05-15 10:00:00'),
( 4, 'DEVELOPER_APPLY',  2, 'OUTLET',   NULL, 14, '张网点02', 'APPROVED', '通过',                    '2026-05-15 10:00:00'),
( 5, 'DEVELOPER_APPLY',  2, 'CITY',     NULL,  2, '李南昌',   'APPROVED', '通过',                    '2026-05-15 14:00:00'),
( 6, 'DEVELOPER_APPLY',  2, 'PROVINCE', NULL,  1, '张省管',   'APPROVED', '审核通过',                '2026-05-16 11:00:00'),
( 7, 'DEVELOPER_APPLY', 31, 'OUTLET',   NULL, 13, '张网点01', 'APPROVED', '初审通过',                '2026-06-19 10:00:00'),
( 8, 'DEVELOPER_APPLY', 31, 'CITY',     NULL,  2, '李南昌',   'APPROVED', '市级通过，待省审',        '2026-06-20 10:00:00'),
( 9, 'DEVELOPER_APPLY', 36, 'OUTLET',   NULL, 16, '张网点04', 'APPROVED', '网点通过',               '2026-06-25 10:00:00'),
(10, 'DEVELOPER_APPLY', 44, 'OUTLET',   NULL, 13, '张网点01', 'REJECTED', '身份证信息不匹配',        '2026-06-15 10:00:00'),
(11, 'DEVELOPER_APPLY', 45, 'OUTLET',   NULL, 27, '张网点15', 'REJECTED', '店铺地址不符合要求',      '2026-06-16 11:00:00'),
(12, 'DEVELOPER_APPLY', 46, 'OUTLET',   NULL, 30, '张网点18', 'REJECTED', '已有重复申请',            '2026-06-17 14:00:00'),
-- 订单审核记录 (意向单 LEAD)
(13, 'ORDER',  1, 'OUTLET',   'LEAD', 13, '张网点01', 'APPROVED', '审核通过',                '2026-05-02 09:30:00'),
(14, 'ORDER',  1, 'PROVINCE', 'LEAD',  1, '张省管',   'APPROVED', '通过',                    '2026-05-02 10:00:00'),
(15, 'ORDER',  2, 'OUTLET',   'LEAD', 13, '张网点01', 'APPROVED', '通过',                    '2026-05-03 10:00:00'),
(16, 'ORDER',  2, 'PROVINCE', 'LEAD',  1, '张省管',   'APPROVED', '准予',                    '2026-05-03 11:00:00'),
(17, 'ORDER',  3, 'OUTLET',   'LEAD', 14, '张网点02', 'APPROVED', '通过',                    '2026-05-04 08:30:00'),
(18, 'ORDER',  3, 'CITY',     'LEAD',  2, '李南昌',   'APPROVED', '地市审核通过',             '2026-05-04 09:00:00'),
(19, 'ORDER',  8, 'OUTLET',   'LEAD', 25, '张网点13', 'APPROVED', '通过',                    '2026-05-09 09:00:00'),
(20, 'ORDER',  9, 'OUTLET',   'LEAD', 27, '张网点15', 'APPROVED', 'VIP客户，请加急',          '2026-05-10 13:00:00'),
(21, 'ORDER',  9, 'CITY',     'LEAD',  8, '周赣州',   'APPROVED', '通过',                    '2026-05-10 14:00:00'),
(22, 'ORDER', 11, 'OUTLET',   'LEAD', 13, '张网点01', 'APPROVED', '审核通过',                '2026-06-02 09:00:00'),
(23, 'ORDER', 11, 'CITY',     'LEAD',  2, '李南昌',   'APPROVED', '通过',                    '2026-06-02 09:30:00'),
(24, 'ORDER', 11, 'PROVINCE', 'LEAD',  1, '张省管',   'APPROVED', '通过',                    '2026-06-02 10:00:00'),
(25, 'ORDER', 35, 'OUTLET',   'LEAD', 24, '张网点12', 'REJECTED', '重复订单',                 '2026-06-26 10:00:00'),
-- 订单审核记录 (转正单 FORMAL)
(26, 'ORDER',  1, 'OUTLET',   'FORMAL', 13, '张网点01', 'APPROVED', '转正确认',               '2026-05-05 09:00:00'),
(27, 'ORDER',  1, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '同意转正',               '2026-05-05 10:00:00'),
(28, 'ORDER',  2, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '转正',                   '2026-05-06 14:00:00'),
(29, 'ORDER', 11, 'OUTLET',   'FORMAL', 13, '张网点01', 'APPROVED', '确认转正',               '2026-06-05 09:00:00'),
(30, 'ORDER', 11, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '同意转正，触发佣金',      '2026-06-05 10:00:00'),
(31, 'ORDER', 12, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '转正通过',               '2026-06-06 14:00:00'),
(32, 'ORDER', 15, 'CITY',     'FORMAL',  5, '陈九江',   'APPROVED', '市级通过转正',            '2026-06-08 14:00:00'),
(33, 'ORDER', 15, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '省级通过转正',            '2026-06-09 10:00:00'),
(34, 'ORDER', 22, 'OUTLET',   'FORMAL', 29, '张网点17', 'APPROVED', '确认转正',               '2026-06-16 09:00:00'),
(35, 'ORDER', 22, 'PROVINCE', 'FORMAL',  1, '张省管',   'APPROVED', '同意转正',               '2026-06-16 14:00:00');

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All sample data inserted successfully!' AS result;