package com.unicom.post.modules.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页仪表盘数据 VO（按角色返回不同数据）
 */
@Data
public class DashboardVO {
    // === 今日概览 ===
    private Long todayNewDevelopers;      // 今日新增发展人
    private Long monthOrderCount;         // 本月业务量
    private BigDecimal todayCommission;   // 今日发放佣金
    private Long outletCount;             // 网点数（省分/地市）/ 发展人数（网点/个人）

    // === 环比趋势 ===
    private String developerTrend;        // 发展人趋势 "↑ 12%" / "↓ 3%"
    private String orderTrend;            // 业务趋势
    private String commissionTrend;       // 佣金趋势

    // === 用户信息 ===
    private String roleName;              // 角色中文名
    private String scopeName;             // 数据范围：全省/本市/本网点/个人
    private String displayName;           // 显示名称
}