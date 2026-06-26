package com.unicom.post.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class CommissionRuleUpdateRequest {
    private String ruleName;
    @Positive(message = "总金额必须大于0")
    private BigDecimal totalAmount;
    private BigDecimal outletRatio;
    private BigDecimal developerRatio;
    private BigDecimal platformRatio;
    private String remark;
}