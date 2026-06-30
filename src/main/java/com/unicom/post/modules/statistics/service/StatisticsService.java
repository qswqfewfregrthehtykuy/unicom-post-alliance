package com.unicom.post.modules.statistics.service;

import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.vo.CommissionStatisticsResult;
import com.unicom.post.modules.statistics.vo.DevelopmentStatisticsResult;
import com.unicom.post.modules.statistics.vo.RankingResult;
import com.unicom.post.modules.statistics.vo.TrendResult;

public interface StatisticsService {

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