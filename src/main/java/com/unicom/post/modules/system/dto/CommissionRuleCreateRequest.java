package com.unicom.post.modules.system.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CommissionRuleCreateRequest {
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;

    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @NotBlank(message = "发展来源不能为空")
    private String developSource;

    @NotBlank(message = "佣金阶段不能为空")
    private String commissionPhase;

    @NotNull(message = "总金额不能为空")
    @Positive(message = "总金额必须大于0")
    private BigDecimal totalAmount;

    @NotNull(message = "网点比例不能为空")
    private BigDecimal outletRatio;

    @NotNull(message = "发展人比例不能为空")
    private BigDecimal developerRatio;

    @NotNull(message = "平台比例不能为空")
    private BigDecimal platformRatio;

    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    private String remark;
}