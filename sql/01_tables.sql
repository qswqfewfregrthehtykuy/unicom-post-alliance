-- ============================================================
-- 联通邮政商盟触点系统 — 建表脚本
-- 数据库: MySQL 8.0+  字符集: utf8mb4
-- ============================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 角色表
-- ============================================================
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `role_code`       VARCHAR(50)  NOT NULL UNIQUE COMMENT '角色编码',
    `role_name`       VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `description`     VARCHAR(255)     NULL COMMENT '描述',
    `status`          TINYINT(1)   NOT NULL DEFAULT 1,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 2. 用户账号表
-- ============================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username`         VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
    `password_hash`    VARCHAR(255) NOT NULL COMMENT 'BCrypt加密密码',
    `real_name`        VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    `phone`            VARCHAR(20)  NOT NULL UNIQUE COMMENT '手机号',
    `idcard`           VARCHAR(20)      NULL COMMENT '身份证号',
    `data_scope_type`  VARCHAR(20)  NOT NULL COMMENT '数据范围 PROVINCE|CITY|OUTLET|SELF',
    `scope_city_id`    BIGINT           NULL COMMENT '所属地市ID',
    `scope_outlet_id`  BIGINT           NULL COMMENT '所属网点ID',
    `status`           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '0=禁用 1=启用',
    `last_login_at`    DATETIME         NULL,
    `remark`           VARCHAR(255)     NULL,
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
    UNIQUE KEY `uk_phone` (`phone`),
    INDEX `idx_scope_type` (`data_scope_type`),
    INDEX `idx_city` (`scope_city_id`),
    INDEX `idx_outlet` (`scope_outlet_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户账号表';

-- ============================================================
-- 3. 用户角色关联表
-- ============================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`         BIGINT       NOT NULL,
    `role_id`         BIGINT       NOT NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user` (`user_id`),
    INDEX `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================
-- 4. 地市表
-- ============================================================
DROP TABLE IF EXISTS `sys_district`;
DROP TABLE IF EXISTS `sys_city`;
CREATE TABLE `sys_city` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `city_code`   VARCHAR(10)  NOT NULL UNIQUE COMMENT '地市编码',
    `city_name`   VARCHAR(50)  NOT NULL COMMENT '地市名称',
    `sort_order`  INT          NOT NULL DEFAULT 0,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地市字典';

-- ============================================================
-- 5. 区县表
-- ============================================================
CREATE TABLE `sys_district` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `city_id`         BIGINT      NOT NULL COMMENT '所属地市ID',
    `district_code`   VARCHAR(10) NOT NULL UNIQUE COMMENT '区县编码',
    `district_name`   VARCHAR(50) NOT NULL COMMENT '区县名称',
    `sort_order`      INT         NOT NULL DEFAULT 0,
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT(1)  NOT NULL DEFAULT 0,
    INDEX `idx_city` (`city_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区县字典';

-- ============================================================
-- 6. 组织架构表
-- ============================================================
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `parent_id`       BIGINT           NULL COMMENT '父节点ID',
    `org_code`        VARCHAR(30)  NOT NULL UNIQUE COMMENT '组织编码',
    `org_name`        VARCHAR(100) NOT NULL COMMENT '组织名称',
    `org_level`       INT          NOT NULL COMMENT '级别 1=省 2=地市 3=区县 4=网点',
    `org_type`        VARCHAR(20)  NOT NULL COMMENT 'PROVINCE|CITY|DISTRICT|OUTLET',
    `city_id`         BIGINT           NULL,
    `province_id`     BIGINT           NULL,
    `leader_name`     VARCHAR(50)      NULL COMMENT '负责人',
    `leader_phone`    VARCHAR(20)      NULL COMMENT '负责人电话',
    `address`         VARCHAR(255)     NULL,
    `sort_order`      INT          NOT NULL DEFAULT 0,
    `status`          TINYINT(1)   NOT NULL DEFAULT 1,
    `remark`          VARCHAR(255)     NULL COMMENT '备注',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX `idx_parent` (`parent_id`),
    INDEX `idx_city` (`city_id`),
    INDEX `idx_org_type` (`org_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组织架构树';

-- ============================================================
-- 7. 邮政网点表
-- ============================================================
DROP TABLE IF EXISTS `biz_developer_apply`;
DROP TABLE IF EXISTS `biz_developer`;
DROP TABLE IF EXISTS `biz_outlet`;
CREATE TABLE `biz_outlet` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `outlet_code`     VARCHAR(30)  NOT NULL UNIQUE COMMENT '网点编码',
    `outlet_name`     VARCHAR(100) NOT NULL COMMENT '网点名称',
    `city_id`         BIGINT       NOT NULL COMMENT '所属地市',
    `district_id`     BIGINT       NOT NULL COMMENT '所属区县',
    `address`         VARCHAR(255) NOT NULL COMMENT '地址',
    `manager_name`    VARCHAR(50)  NOT NULL COMMENT '负责人',
    `manager_phone`   VARCHAR(20)  NOT NULL COMMENT '负责人电话',
    `alliance_master` VARCHAR(50)      NULL COMMENT '商盟盟主',
    `admin_user_id`   BIGINT           NULL UNIQUE COMMENT '绑定网点管理员账号',
    `status`          TINYINT(1)   NOT NULL DEFAULT 1,
    `remark`          VARCHAR(255)     NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX `idx_city` (`city_id`),
    INDEX `idx_district` (`district_id`),
    FOREIGN KEY (`admin_user_id`) REFERENCES `sys_user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮政网点';

-- ============================================================
-- 8. 发展人申请表
-- ============================================================
CREATE TABLE `biz_developer_apply` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `applicant_name`   VARCHAR(50)  NOT NULL COMMENT '申请人姓名',
    `applicant_phone`  VARCHAR(20)  NOT NULL COMMENT '申请人手机号',
    `id_card`          VARCHAR(20)  NOT NULL UNIQUE COMMENT '身份证号',
    `developer_type`   VARCHAR(20)  NOT NULL COMMENT 'FREE_SHOP|SELF_EMPLOYED',
    `city_id`          BIGINT       NOT NULL COMMENT '所在地市',
    `district_id`      BIGINT       NOT NULL COMMENT '所在区县',
    `outlet_id`        BIGINT       NOT NULL COMMENT '归属网点',
    `shop_name`        VARCHAR(100)     NULL COMMENT '店铺名称',
    `shop_address`     VARCHAR(255)     NULL COMMENT '店铺地址',
    `apply_reason`     VARCHAR(500)     NULL COMMENT '申请理由',
    `status`           VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    `reviewer_id`      BIGINT           NULL,
    `review_at`        DATETIME         NULL,
    `review_remark`    VARCHAR(255)     NULL,
    `user_id`          BIGINT           NULL COMMENT '审核通过后创建的账号ID',
    `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`       TINYINT(1)   NOT NULL DEFAULT 0,
    UNIQUE KEY `uk_idcard_phone` (`id_card`, `applicant_phone`),
    INDEX `idx_outlet` (`outlet_id`),
    INDEX `idx_city` (`city_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发展人申请表';

-- ============================================================
-- 9. 发展人信息表
-- ============================================================
CREATE TABLE `biz_developer` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`              BIGINT       NOT NULL UNIQUE COMMENT '关联sys_user',
    `apply_id`             BIGINT       NOT NULL COMMENT '来源申请ID',
    `outlet_id`            BIGINT       NOT NULL COMMENT '归属网点',
    `developer_type`       VARCHAR(20)  NOT NULL COMMENT 'FREE_SHOP|SELF_EMPLOYED',
    `shop_name`            VARCHAR(100)     NULL,
    `shop_address`         VARCHAR(255)     NULL,
    `created_by`           BIGINT           NULL COMMENT '批准人ID',
    `created_outlet_admin` BIGINT           NULL COMMENT '网点管理员ID',
    `status`               TINYINT(1)   NOT NULL DEFAULT 1,
    `join_date`            DATE         NOT NULL COMMENT '加入日期',
    `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`           TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX `idx_outlet` (`outlet_id`),
    INDEX `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='发展人信息表';

-- ============================================================
-- 10. 佣金规则配置表
-- ============================================================
DROP TABLE IF EXISTS `biz_commission_detail`;
DROP TABLE IF EXISTS `biz_commission_rule`;
CREATE TABLE `biz_commission_rule` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `rule_name`         VARCHAR(100)    NOT NULL COMMENT '规则名称',
    `business_type`     VARCHAR(30)     NOT NULL COMMENT 'MOBILE_CARD|BROADBAND|OTHER',
    `develop_source`    VARCHAR(30)     NOT NULL COMMENT 'SITE_USER|OUTLET_SELF',
    `commission_phase`  VARCHAR(20)     NOT NULL COMMENT 'LEAD|FORMAL',
    `total_amount`      DECIMAL(10,2)   NOT NULL COMMENT '总金额',
    `outlet_ratio`      DECIMAL(5,4)    NOT NULL COMMENT '网点比例',
    `developer_ratio`   DECIMAL(5,4)    NOT NULL COMMENT '发展人比例',
    `platform_ratio`    DECIMAL(5,4)    NOT NULL COMMENT '平台比例',
    `effective_date`    DATE            NOT NULL,
    `expiry_date`       DATE                NULL,
    `status`            TINYINT(1)      NOT NULL DEFAULT 1,
    `remark`            VARCHAR(500)        NULL,
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金规则配置表';

-- ============================================================
-- 11. 业务发展记录表
-- ============================================================
DROP TABLE IF EXISTS `biz_development_order`;
CREATE TABLE `biz_development_order` (
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `order_no`            VARCHAR(50)  NOT NULL UNIQUE COMMENT '订单编号',
    `outlet_id`           BIGINT       NOT NULL,
    `city_id`             BIGINT       NOT NULL,
    `developer_id`        BIGINT           NULL,
    `develop_source`      VARCHAR(30)  NOT NULL COMMENT 'SITE_USER|OUTLET_SELF',
    `business_type`       VARCHAR(30)  NOT NULL COMMENT 'MOBILE_CARD|BROADBAND|OTHER',
    `customer_name`       VARCHAR(50)  NOT NULL,
    `customer_phone`      VARCHAR(20)  NOT NULL,
    `customer_id_card`    VARCHAR(20)      NULL,
    `customer_address`    VARCHAR(255)     NULL,
    `lead_status`         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    `formal_status`       VARCHAR(20)  NOT NULL DEFAULT 'N/A',
    `lead_audit_at`       DATETIME         NULL,
    `lead_auditor_id`     BIGINT           NULL,
    `formal_audit_at`     DATETIME         NULL,
    `formal_auditor_id`   BIGINT           NULL,
    `remark`              VARCHAR(500)     NULL,
    `created_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`          TINYINT(1)   NOT NULL DEFAULT 0,
    INDEX `idx_outlet` (`outlet_id`),
    INDEX `idx_city` (`city_id`),
    INDEX `idx_developer` (`developer_id`),
    INDEX `idx_lead_status` (`lead_status`),
    INDEX `idx_formal_status` (`formal_status`),
    INDEX `idx_customer` (`customer_phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务发展记录';

-- ============================================================
-- 12. 佣金结算明细表
-- ============================================================
CREATE TABLE `biz_commission_detail` (
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `order_id`                BIGINT         NOT NULL,
    `order_no`                VARCHAR(50)    NOT NULL,
    `commission_phase`        VARCHAR(20)    NOT NULL COMMENT 'LEAD|FORMAL',
    `payee_type`              VARCHAR(20)    NOT NULL COMMENT 'OUTLET|DEVELOPER|PLATFORM',
    `payee_id`                BIGINT         NOT NULL,
    `rule_id`                 BIGINT         NOT NULL,
    `rule_name_snapshot`      VARCHAR(100)       NULL,
    `business_type_snapshot`  VARCHAR(30)        NULL,
    `develop_source_snapshot` VARCHAR(30)        NULL,
    `total_amount`            DECIMAL(10,2)  NOT NULL,
    `ratio`                   DECIMAL(5,4)   NOT NULL,
    `amount`                  DECIMAL(10,2)  NOT NULL,
    `status`                  VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    `settle_at`               DATETIME           NULL,
    `remark`                  VARCHAR(255)       NULL,
    `created_at`              DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`              DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`              TINYINT(1)     NOT NULL DEFAULT 0,
    INDEX `idx_order` (`order_id`),
    INDEX `idx_payee` (`payee_type`, `payee_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='佣金结算明细';

-- ============================================================
-- 13. 操作日志表
-- ============================================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id`      BIGINT       NOT NULL,
    `username`     VARCHAR(50)  NOT NULL,
    `role`         VARCHAR(30)  NOT NULL,
    `module`       VARCHAR(50)  NOT NULL,
    `action`       VARCHAR(100) NOT NULL,
    `target_id`    BIGINT           NULL,
    `target_type`  VARCHAR(50)      NULL,
    `before_data`  TEXT             NULL,
    `after_data`   TEXT             NULL,
    `ip_address`   VARCHAR(50)      NULL,
    `result`       VARCHAR(20)  NOT NULL COMMENT 'SUCCESS|FAIL',
    `error_msg`    VARCHAR(500)     NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user` (`user_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志';

-- ============================================================
-- 14. 业务审核日志表
-- ============================================================
DROP TABLE IF EXISTS `biz_audit_log`;
CREATE TABLE `biz_audit_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `target_type`     VARCHAR(30)  NOT NULL COMMENT 'DEVELOPER_APPLY|ORDER',
    `target_id`       BIGINT       NOT NULL,
    `audit_level`     VARCHAR(20)  NOT NULL COMMENT 'OUTLET|CITY|PROVINCE',
    `audit_phase`     VARCHAR(10)      NULL COMMENT 'LEAD|FORMAL',
    `auditor_id`      BIGINT       NOT NULL,
    `auditor_name`    VARCHAR(50)  NOT NULL,
    `audit_status`    VARCHAR(20)  NOT NULL COMMENT 'APPROVED|REJECTED',
    `audit_remark`    VARCHAR(500)     NULL,
    `audit_time`      DATETIME         NULL,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_auditor` (`auditor_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业务审核日志表';

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All tables created successfully!' AS result;
