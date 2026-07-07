-- ============================================================
-- 13. 佣金结算明细（35条，按已结算订单生成）
-- ============================================================
INSERT INTO biz_commission_detail (id, order_id, order_no, commission_phase, payee_type, payee_id, rule_id, rule_name_snapshot, business_type_snapshot, develop_source_snapshot, total_amount, ratio, amount, status, settle_at) VALUES
-- 网点自营意向佣金 (payee_type=OUTLET, payee_id=outlet_id)
( 1,  1, 'ORD20260501001', 'LEAD',   'OUTLET',    1,  3, '移网-网点自营-意向佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 30.00,  0.8000,  24.00, 'SETTLED', '2026-05-02 10:00:00'),
( 2,  1, 'ORD20260501001', 'LEAD',   'DEVELOPER', 1,  3, '移网-网点自营-意向佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 30.00,  0.1000,   3.00, 'SETTLED', '2026-05-02 10:00:00'),
( 3,  1, 'ORD20260501001', 'LEAD',   'PLATFORM',  0,  3, '移网-网点自营-意向佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 30.00,  0.1000,   3.00, 'SETTLED', '2026-05-02 10:00:00'),
-- 网点自营转正佣金
( 4,  1, 'ORD20260501001', 'FORMAL', 'OUTLET',    1,  4, '移网-网点自营-转正佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 100.00, 0.8000,  80.00, 'SETTLED', '2026-05-05 10:00:00'),
( 5,  1, 'ORD20260501001', 'FORMAL', 'DEVELOPER', 1,  4, '移网-网点自营-转正佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 100.00, 0.1000,  10.00, 'SETTLED', '2026-05-05 10:00:00'),
( 6,  1, 'ORD20260501001', 'FORMAL', 'PLATFORM',  0,  4, '移网-网点自营-转正佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 100.00, 0.1000,  10.00, 'SETTLED', '2026-05-05 10:00:00'),
-- 宽带网点自营
( 7,  2, 'ORD20260502001', 'LEAD',   'OUTLET',    1,  7, '宽带-网点自营-意向佣金V1', 'BROADBAND',   'OUTLET_SELF', 50.00,  0.8000,  40.00, 'SETTLED', '2026-05-03 11:00:00'),
( 8,  2, 'ORD20260502001', 'FORMAL', 'OUTLET',    1,  8, '宽带-网点自营-转正佣金V1', 'BROADBAND',   'OUTLET_SELF', 200.00, 0.8000, 160.00, 'SETTLED', '2026-05-06 14:00:00'),
( 9,  2, 'ORD20260502001', 'FORMAL', 'PLATFORM',  0,  8, '宽带-网点自营-转正佣金V1', 'BROADBAND',   'OUTLET_SELF', 200.00, 0.1000,  20.00, 'SETTLED', '2026-05-06 14:00:00'),
-- 发展人订单佣金 (payee_type=DEVELOPER, payee_id=developer_id)
(10, 11, 'ORD20260601001', 'LEAD',   'OUTLET',    1,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.1000,   3.00, 'SETTLED', '2026-06-02 10:00:00'),
(11, 11, 'ORD20260601001', 'LEAD',   'DEVELOPER', 1,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-02 10:00:00'),
(12, 11, 'ORD20260601001', 'LEAD',   'PLATFORM',  0,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.1000,   3.00, 'SETTLED', '2026-06-02 10:00:00'),
(13, 11, 'ORD20260601001', 'FORMAL', 'DEVELOPER', 1,  2, '移网-发展人-转正佣金V1', 'MOBILE_CARD', 'SITE_USER',  100.00,  0.8000,  80.00, 'SETTLED', '2026-06-05 10:00:00'),
(14, 11, 'ORD20260601001', 'FORMAL', 'OUTLET',    1,  2, '移网-发展人-转正佣金V1', 'MOBILE_CARD', 'SITE_USER',  100.00,  0.1000,  10.00, 'SETTLED', '2026-06-05 10:00:00'),
(15, 12, 'ORD20260602001', 'LEAD',   'DEVELOPER', 1,  5, '宽带-发展人-意向佣金V1', 'BROADBAND',   'SITE_USER',   50.00,  0.8000,  40.00, 'SETTLED', '2026-06-03 11:00:00'),
(16, 12, 'ORD20260602001', 'FORMAL', 'DEVELOPER', 1,  6, '宽带-发展人-转正佣金V1', 'BROADBAND',   'SITE_USER',  200.00,  0.7500, 150.00, 'SETTLED', '2026-06-06 14:00:00'),
(17, 13, 'ORD20260603001', 'LEAD',   'DEVELOPER', 2,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-04 09:00:00'),
(18, 15, 'ORD20260605001', 'LEAD',   'DEVELOPER', 7,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-06 10:00:00'),
(19, 15, 'ORD20260605001', 'FORMAL', 'DEVELOPER', 7,  2, '移网-发展人-转正佣金V1', 'MOBILE_CARD', 'SITE_USER',  100.00,  0.8000,  80.00, 'SETTLED', '2026-06-09 10:00:00'),
(20, 18, 'ORD20260608001', 'LEAD',   'DEVELOPER',10,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-09 10:00:00'),
(21, 19, 'ORD20260609001', 'LEAD',   'DEVELOPER',11,  5, '宽带-发展人-意向佣金V1', 'BROADBAND',   'SITE_USER',   50.00,  0.8000,  40.00, 'SETTLED', '2026-06-10 14:00:00'),
(22, 19, 'ORD20260609001', 'FORMAL', 'DEVELOPER',11,  6, '宽带-发展人-转正佣金V1', 'BROADBAND',   'SITE_USER',  200.00,  0.7500, 150.00, 'SETTLED', '2026-06-13 10:00:00'),
(23, 21, 'ORD20260611001', 'LEAD',   'DEVELOPER',13,  9, '其他-发展人-意向佣金V1', 'OTHER',       'SITE_USER',   20.00,  0.8000,  16.00, 'SETTLED', '2026-06-12 10:00:00'),
(24, 22, 'ORD20260612001', 'LEAD',   'DEVELOPER',14,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-13 11:00:00'),
(25, 22, 'ORD20260612001', 'FORMAL', 'DEVELOPER',14,  2, '移网-发展人-转正佣金V1', 'MOBILE_CARD', 'SITE_USER',  100.00,  0.8000,  80.00, 'SETTLED', '2026-06-16 14:00:00'),
(26, 23, 'ORD20260613001', 'LEAD',   'DEVELOPER',15,  5, '宽带-发展人-意向佣金V1', 'BROADBAND',   'SITE_USER',   50.00,  0.8000,  40.00, 'SETTLED', '2026-06-14 09:00:00'),
(27, 25, 'ORD20260615001', 'LEAD',   'DEVELOPER',17,  9, '其他-发展人-意向佣金V1', 'OTHER',       'SITE_USER',   20.00,  0.8000,  16.00, 'SETTLED', '2026-06-16 10:00:00'),
(28, 27, 'ORD20260617001', 'LEAD',   'DEVELOPER',19,  5, '宽带-发展人-意向佣金V1', 'BROADBAND',   'SITE_USER',   50.00,  0.8000,  40.00, 'SETTLED', '2026-06-18 11:00:00'),
(29, 28, 'ORD20260618001', 'LEAD',   'DEVELOPER',20,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-19 14:00:00'),
(30, 30, 'ORD20260620001', 'LEAD',   'DEVELOPER',22,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'SETTLED', '2026-06-21 10:00:00'),
-- PENDING 状态的佣金明细
(31, 16, 'ORD20260606001', 'LEAD',   'DEVELOPER', 8,  5, '宽带-发展人-意向佣金V1', 'BROADBAND',   'SITE_USER',   50.00,  0.8000,  40.00, 'PENDING', NULL),
(32, 10, 'ORD20260510001', 'LEAD',   'OUTLET',   18,  7, '宽带-网点自营-意向佣金V1', 'BROADBAND',   'OUTLET_SELF', 50.00,  0.8000,  40.00, 'PENDING', NULL),
(33, 20, 'ORD20260610001', 'LEAD',   'DEVELOPER',12,  1, '移网-发展人-意向佣金V1', 'MOBILE_CARD', 'SITE_USER',   30.00,  0.8000,  24.00, 'PENDING', NULL),
(34,  5, 'ORD20260505001', 'FORMAL', 'OUTLET',    6,  4, '移网-网点自营-转正佣金V1', 'MOBILE_CARD', 'OUTLET_SELF', 100.00, 0.8000,  80.00, 'PENDING', NULL),
(35,  8, 'ORD20260508001', 'FORMAL', 'OUTLET',   13, 12, '其他-网点自营-转正佣金V1', 'OTHER',       'OUTLET_SELF',  80.00, 0.8000,  64.00, 'PENDING', NULL);