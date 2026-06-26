package com.unicom.post.modules.commission.controller;

import com.unicom.post.common.result.PageResult;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.commission.dto.CommissionDetailQueryDTO;
import com.unicom.post.modules.commission.vo.CommissionDetailVO;
import com.unicom.post.modules.commission.vo.SummaryVO;
import com.unicom.post.modules.commission.service.CommissionDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/commission-details")
@RequiredArgsConstructor
public class CommissionDetailController {

    private final CommissionDetailService commissionDetailService;

    /**
     * 7.1 查询佣金明细列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<PageResult<CommissionDetailVO>> list(CommissionDetailQueryDTO query) {
        PageResult<CommissionDetailVO> pageResult = commissionDetailService.listCommissionDetails(query);
        return Result.success(pageResult);
    }

    /**
     * 7.2 佣金汇总统计
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<SummaryVO> summary(CommissionDetailQueryDTO query) {
        SummaryVO summary = commissionDetailService.summaryStatistics(query);
        return Result.success(summary);
    }

    /**
     * 7.3 导出佣金明细
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public void export(CommissionDetailQueryDTO query, HttpServletResponse response) {
        commissionDetailService.exportCommissionDetails(query, response);
    }
}