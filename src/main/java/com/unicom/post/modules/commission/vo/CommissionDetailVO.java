package com.unicom.post.modules.commission.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CommissionDetailVO {
    private Long id;
    private String orderNo;
    private String commissionPhase;
    private String payeeType;
    private String payeeName;
    private String ruleNameSnapshot;
    private String businessTypeSnapshot;
    private String developSourceSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal ratio;
    private BigDecimal amount;
    private String status;
    private LocalDateTime settleAt;
    private LocalDateTime createdAt;
}