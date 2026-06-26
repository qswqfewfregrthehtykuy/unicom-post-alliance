package com.unicom.post.modules.commission.service;

import com.unicom.post.common.result.PageResult;
import com.unicom.post.modules.commission.dto.CommissionDetailQueryDTO;
import com.unicom.post.modules.commission.vo.CommissionDetailVO;
import com.unicom.post.modules.commission.vo.SummaryVO;

import javax.servlet.http.HttpServletResponse;

public interface CommissionDetailService {

    /**
     * 分页查询佣金明细
     */
    PageResult<CommissionDetailVO> listCommissionDetails(CommissionDetailQueryDTO query);

    /**
     * 佣金汇总统计
     */
    SummaryVO summaryStatistics(CommissionDetailQueryDTO query);

    /**
     * 导出佣金明细Excel
     */
    void exportCommissionDetails(CommissionDetailQueryDTO query, HttpServletResponse response);
}