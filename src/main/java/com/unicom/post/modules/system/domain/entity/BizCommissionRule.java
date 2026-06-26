package com.unicom.post.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_commission_rule")
public class BizCommissionRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String businessType;
    private String developSource;
    private String commissionPhase;
    private BigDecimal totalAmount;
    private BigDecimal outletRatio;
    private BigDecimal developerRatio;
    private BigDecimal platformRatio;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Integer status; // 1启用 0禁用
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}