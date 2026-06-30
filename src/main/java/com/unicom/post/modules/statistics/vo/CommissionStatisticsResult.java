package com.unicom.post.modules.statistics.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;



// CommissionStatisticsResult
@Data
public class CommissionStatisticsResult {
    private String dimension;
    private BigDecimal totalAmount;
    private BigDecimal leadAmount;
    private BigDecimal formalAmount;
    private List<CommissionStatisticsVO> list;
}