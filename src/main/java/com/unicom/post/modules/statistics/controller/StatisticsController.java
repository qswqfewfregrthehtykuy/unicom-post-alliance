package com.unicom.post.modules.statistics.controller;

import com.unicom.post.common.result.Result;
import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.service.StatisticsService;
import com.unicom.post.modules.statistics.vo.DevelopmentStatisticsResult;
import com.unicom.post.modules.statistics.vo.CommissionStatisticsResult;
import com.unicom.post.modules.statistics.vo.RankingResult;
import com.unicom.post.modules.statistics.vo.TrendResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 8.1 发展量统计
     */
    @GetMapping("/development")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<DevelopmentStatisticsResult> developmentStatistics(@Valid StatisticsRequest req) {
        return Result.success(statisticsService.getDevelopmentStatistics(req));
    }

    /**
     * 8.2 佣金统计汇总
     */
    @GetMapping("/commission")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<CommissionStatisticsResult> commissionStatistics(@Valid StatisticsRequest req) {
        return Result.success(statisticsService.getCommissionStatistics(req));
    }

    /**
     * 8.3 发展排行榜
     */
    @GetMapping("/ranking")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY')")   // 仅省/市可查看排行
    public Result<RankingResult> ranking(
            @RequestParam String rankType,           // OUTLET|DEVELOPER
            @RequestParam(required = false) String businessType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") Integer limit) {

        RankingResult result = statisticsService.getRanking(
                rankType, businessType,
                startDate.toString(), endDate.toString(), limit);
        return Result.success(result);
    }

    /**
     * 8.4 时间趋势数据
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<TrendResult> trend(
            @RequestParam String granularity,        // DAY|WEEK|MONTH
            @RequestParam String metric,             // DEVELOPMENT_COUNT|COMMISSION_AMOUNT
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String businessType) {

        TrendResult result = statisticsService.getTrend(
                granularity, metric,
                startDate.toString(), endDate.toString(), businessType);
        return Result.success(result);
    }
}