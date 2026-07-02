package com.unicom.post.modules.statistics.service;

import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.vo.BusinessBreakdownVO;
import com.unicom.post.modules.statistics.vo.CommissionStatisticsResult;
import com.unicom.post.modules.statistics.vo.DashboardVO;
import com.unicom.post.modules.statistics.vo.DevelopmentStatisticsResult;
import com.unicom.post.modules.statistics.vo.RankingResult;
import com.unicom.post.modules.statistics.vo.RecentAuditVO;
import com.unicom.post.modules.statistics.vo.TrendResult;

import java.util.List;

public interface StatisticsService {

    /**
     * 首页仪表盘数据（根据角色返回不同维度的汇总数据）
     */
    DashboardVO getDashboard();

    /**
     * 业务类型分布（饼图数据）
     */
    List<BusinessBreakdownVO> getBusinessBreakdown();

    /**
     * 最近审核动态（Dashboard 审核流表格）
     */
    List<RecentAuditVO> getRecentAudits();

    /**
     * 发展量统计
     */
    DevelopmentStatisticsResult getDevelopmentStatistics(StatisticsRequest req);

    /**
     * 佣金统计汇总
     */
    CommissionStatisticsResult getCommissionStatistics(StatisticsRequest req);

    /**
     * 发展排行榜
     */
    RankingResult getRanking(String rankType, String businessType,
                             String startDate, String endDate, Integer limit);

    /**
     * 时间趋势数据
     */
    TrendResult getTrend(String granularity, String metric,
                         String startDate, String endDate, String businessType);
}