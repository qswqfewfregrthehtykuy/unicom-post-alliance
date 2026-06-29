package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CommissionStatisticsVO {
    private String dimensionId;
    private String dimensionName;
    private BigDecimal totalAmount;
    private BigDecimal leadAmount;
    private BigDecimal formalAmount;
    private BigDecimal settledAmount;
    private BigDecimal pendingAmount;
}