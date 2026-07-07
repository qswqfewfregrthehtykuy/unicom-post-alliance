-- ============================================================
-- 6. 邮政网点（30个，覆盖全省11个地市）
-- ============================================================
INSERT INTO biz_outlet (id, outlet_code, outlet_name, city_id, district_id, address, manager_name, manager_phone, admin_user_id, status) VALUES
( 1, 'NC001', '南昌东湖邮政支局',    1,  1, '南昌市东湖区八一大道1号',     '张网点01', '13800000013', 13, 1),
( 2, 'NC002', '南昌西湖邮政所',      1,  2, '南昌市西湖区站前路55号',     '张网点02', '13800000014', 14, 1),
( 3, 'NC003', '南昌青云谱邮政营业厅', 1,  3, '南昌市青云谱区洪都大道88号',  '张网点03', '13800000015', 15, 1),
( 4, 'JDZ01', '景德镇昌江邮政支局',   2, 10, '景德镇市昌江区瓷都大道20号',  '张网点04', '13800000016', 16, 1),
( 5, 'JDZ02', '景德镇珠山邮政所',     2, 11, '景德镇市珠山区广场北路12号',  '张网点05', '13800000017', 17, 1),
( 6, 'PX01',  '萍乡安源邮政支局',     3, 14, '萍乡市安源区跃进路30号',     '张网点06', '13800000018', 18, 1),
( 7, 'PX02',  '萍乡上栗邮政所',       3, 17, '萍乡市上栗县平安路15号',     '张网点07', '13800000019', 19, 1),
( 8, 'JJ01',  '九江浔阳邮政支局',     4, 19, '九江市浔阳区浔阳东路100号',  '张网点08', '13800000020', 20, 1),
( 9, 'JJ02',  '九江濂溪邮政所',       4, 20, '九江市濂溪区十里大道200号',  '张网点09', '13800000021', 21, 1),
(10, 'JJ03',  '九江瑞昌邮政支局',     4, 22, '瑞昌市赤乌东路50号',         '张网点10', '13800000022', 22, 1),
(11, 'XY01',  '新余渝水邮政支局',     5, 32, '新余市渝水区胜利南路80号',   '张网点11', '13800000023', 23, 1),
(12, 'XY02',  '新余分宜邮政所',       5, 33, '新余市分宜县昌山路18号',     '张网点12', '13800000024', 24, 1),
(13, 'YT01',  '鹰潭月湖邮政支局',     6, 34, '鹰潭市月湖区胜利西路60号',   '张网点13', '13800000025', 25, 1),
(14, 'YT02',  '鹰潭贵溪邮政所',       6, 36, '贵溪市雄石路25号',          '张网点14', '13800000026', 26, 1),
(15, 'GZ01',  '赣州章贡邮政支局',     7, 37, '赣州市章贡区红旗大道120号',  '张网点15', '13800000027', 27, 1),
(16, 'GZ02',  '赣州南康邮政所',       7, 38, '赣州市南康区泰康路33号',     '张网点16', '13800000028', 28, 1),
(17, 'GZ03',  '赣州瑞金邮政支局',     7, 40, '瑞金市红都大道55号',         '张网点17', '13800000029', 29, 1),
(18, 'JA01',  '吉安吉州邮政支局',     8, 55, '吉安市吉州区井冈山大道80号',  '张网点18', '13800000030', 30, 1),
(19, 'JA02',  '吉安井冈山邮政所',     8, 67, '井冈山市茨坪镇红军路10号',    '张网点19', '13800000031', 31, 1),
(20, 'YC01',  '宜春袁州邮政支局',     9, 68, '宜春市袁州区中山西路90号',   '张网点20', '13800000032', 32, 1),
(21, 'YC02',  '宜春丰城邮政所',       9, 69, '丰城市人民路40号',          '张网点21', '13800000033', 33, 1),
(22, 'FZ01',  '抚州临川邮政支局',    10, 78, '抚州市临川区赣东大道150号',  '张网点22', '13800000034', 34, 1),
(23, 'FZ02',  '抚州东乡邮政所',      10, 79, '抚州市东乡区恒安路22号',    '张网点23', '13800000035', 35, 1),
(24, 'SR01',  '上饶信州邮政支局',    11, 89, '上饶市信州区中山路180号',   '张网点24', '13800000036', 36, 1),
(25, 'SR02',  '上饶广丰邮政所',      11, 90, '上饶市广丰区永丰大道35号',  '张网点25', '13800000037', 37, 1),
(26, 'NC004', '南昌青山湖邮政支局',   1,  4, '南昌市青山湖区南京东路99号', '张网点26', '13800000038', 38, 1),
(27, 'NC005', '南昌红谷滩邮政营业厅', 1,  6, '南昌市红谷滩区会展路66号',   '张网点27', '13800000039', 39, 1),
(28, 'JJ04',  '九江庐山邮政所',       4, 24, '庐山市南康大道12号',         '张网点28', '13800000040', 40, 1),
(29, 'GZ04',  '赣州兴国邮政支局',     7, 50, '赣州市兴国县凤凰大道70号',   '张网点29', '13800000041', 41, 1),
(30, 'SR03',  '上饶婺源邮政营业厅',  11, 99, '上饶市婺源县文公路88号',    '张网点30', '13800000042', 42, 1);

-- ============================================================
-- 7. 组织架构树（sys_org）
-- 江西省(1) → 11地市(101-111) → 100区县(1001-1100) → 30网点(2001-2030)
-- ============================================================
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, province_id, sort_order, status) VALUES
-- 省
(1,    NULL, 'JX',         '江西省邮政',       1, 'PROVINCE', NULL, NULL, 1, 1),
-- 地市 (parent=1)
(101,  1,    'JX-NC',      '南昌市邮政',       2, 'CITY',     1, 1, 1, 1),
(102,  1,    'JX-JDZ',     '景德镇市邮政',     2, 'CITY',     2, 1, 2, 1),
(103,  1,    'JX-PX',      '萍乡市邮政',       2, 'CITY',     3, 1, 3, 1),
(104,  1,    'JX-JJ',      '九江市邮政',       2, 'CITY',     4, 1, 4, 1),
(105,  1,    'JX-XY',      '新余市邮政',       2, 'CITY',     5, 1, 5, 1),
(106,  1,    'JX-YT',      '鹰潭市邮政',       2, 'CITY',     6, 1, 6, 1),
(107,  1,    'JX-GZ',      '赣州市邮政',       2, 'CITY',     7, 1, 7, 1),
(108,  1,    'JX-JA',      '吉安市邮政',       2, 'CITY',     8, 1, 8, 1),
(109,  1,    'JX-YC',      '宜春市邮政',       2, 'CITY',     9, 1, 9, 1),
(110,  1,    'JX-FZ',      '抚州市邮政',       2, 'CITY',    10, 1, 10, 1),
(111,  1,    'JX-SR',      '上饶市邮政',       2, 'CITY',    11, 1, 11, 1);

-- 南昌市区县 (parent=101, ids base=1000 + district_id)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 101, CONCAT('JX-NC-', district_code), district_name, 3, 'DISTRICT', 1, sort_order, 1
FROM sys_district WHERE city_id = 1;

-- 景德镇区县 (parent=102)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 102, CONCAT('JX-JDZ-', district_code), district_name, 3, 'DISTRICT', 2, sort_order, 1
FROM sys_district WHERE city_id = 2;

-- 萍乡区县 (parent=103)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 103, CONCAT('JX-PX-', district_code), district_name, 3, 'DISTRICT', 3, sort_order, 1
FROM sys_district WHERE city_id = 3;

-- 九江区县 (parent=104)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 104, CONCAT('JX-JJ-', district_code), district_name, 3, 'DISTRICT', 4, sort_order, 1
FROM sys_district WHERE city_id = 4;

-- 新余区县 (parent=105)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 105, CONCAT('JX-XY-', district_code), district_name, 3, 'DISTRICT', 5, sort_order, 1
FROM sys_district WHERE city_id = 5;

-- 鹰潭区县 (parent=106)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 106, CONCAT('JX-YT-', district_code), district_name, 3, 'DISTRICT', 6, sort_order, 1
FROM sys_district WHERE city_id = 6;

-- 赣州区县 (parent=107)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 107, CONCAT('JX-GZ-', district_code), district_name, 3, 'DISTRICT', 7, sort_order, 1
FROM sys_district WHERE city_id = 7;

-- 吉安区县 (parent=108)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 108, CONCAT('JX-JA-', district_code), district_name, 3, 'DISTRICT', 8, sort_order, 1
FROM sys_district WHERE city_id = 8;

-- 宜春区县 (parent=109)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 109, CONCAT('JX-YC-', district_code), district_name, 3, 'DISTRICT', 9, sort_order, 1
FROM sys_district WHERE city_id = 9;

-- 抚州区县 (parent=110)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 110, CONCAT('JX-FZ-', district_code), district_name, 3, 'DISTRICT', 10, sort_order, 1
FROM sys_district WHERE city_id = 10;

-- 上饶区县 (parent=111)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 1000 + id, 111, CONCAT('JX-SR-', district_code), district_name, 3, 'DISTRICT', 11, sort_order, 1
FROM sys_district WHERE city_id = 11;

-- 网点 (parent = 对应区县的org_id, org_id = 1000 + district_id)
INSERT INTO sys_org (id, parent_id, org_code, org_name, org_level, org_type, city_id, sort_order, status)
SELECT 2000 + o.id, 1000 + o.district_id, o.outlet_code, o.outlet_name, 4, 'OUTLET', o.city_id, 1, 1
FROM biz_outlet o;