-- ============================================================
-- 联通邮政商盟触点系统 — 数据库初始化入口
-- 使用方式: source /path/to/sql/00_init_all.sql
-- 或: mysql -u root -p < 00_init_all.sql
-- ============================================================
-- 注意: 如果使用 MySQL 命令行，请逐文件执行或使用此入口文件
-- 每个文件都可以独立执行

SOURCE 01_tables.sql;
SOURCE 02_base_data_part1.sql;
SOURCE 02_base_data_part2.sql;
SOURCE 02_base_data_part3.sql;
SOURCE 02_base_data_part4.sql;
SOURCE 02_base_data_part5.sql;
SOURCE 02_base_data_part6.sql;
SOURCE 02_base_data_part7.sql;
SOURCE 02_base_data_part8.sql;

SELECT '=== 数据库初始化完成 ===' AS result;

-- 验证数据量
SELECT 'sys_role' AS table_name, COUNT(*) AS row_count FROM sys_role
UNION ALL SELECT 'sys_user', COUNT(*) FROM sys_user
UNION ALL SELECT 'sys_user_role', COUNT(*) FROM sys_user_role
UNION ALL SELECT 'sys_city', COUNT(*) FROM sys_city
UNION ALL SELECT 'sys_district', COUNT(*) FROM sys_district
UNION ALL SELECT 'sys_org', COUNT(*) FROM sys_org
UNION ALL SELECT 'biz_outlet', COUNT(*) FROM biz_outlet
UNION ALL SELECT 'biz_developer_apply', COUNT(*) FROM biz_developer_apply
UNION ALL SELECT 'biz_developer', COUNT(*) FROM biz_developer
UNION ALL SELECT 'biz_commission_rule', COUNT(*) FROM biz_commission_rule
UNION ALL SELECT 'biz_development_order', COUNT(*) FROM biz_development_order
UNION ALL SELECT 'biz_commission_detail', COUNT(*) FROM biz_commission_detail
UNION ALL SELECT 'sys_operation_log', COUNT(*) FROM sys_operation_log
UNION ALL SELECT 'biz_audit_log', COUNT(*) FROM biz_audit_log;
