package com.unicom.post.modules.commission.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_commission_detail")
public class BizCommissionDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String commissionPhase;
    private String payeeType;
    private Long payeeId;
    private Long ruleId;
    private String ruleNameSnapshot;
    private String businessTypeSnapshot;
    private String developSourceSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal ratio;
    private BigDecimal amount;
    private String status;
    private LocalDateTime settleAt;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}