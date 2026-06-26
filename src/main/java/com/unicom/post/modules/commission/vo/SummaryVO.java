package com.unicom.post.modules.commission.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SummaryVO {
    private BigDecimal totalAmount;
    private BigDecimal leadTotalAmount;
    private BigDecimal formalTotalAmount;
    private BigDecimal settledAmount;
    private BigDecimal pendingAmount;
    private BigDecimal frozenAmount;
    private Integer orderCount;
    private Integer detailCount;
    private Breakdown breakdown;

    @Data
    public static class Breakdown {
        private List<ByPayeeItem> byPayee;
        private List<ByPhaseItem> byPhase;
    }

    @Data
    public static class ByPayeeItem {
        private String payeeType;
        private Integer payeeCount;
        private BigDecimal totalAmount;
        private BigDecimal settledAmount;
    }

    @Data
    public static class ByPhaseItem {
        private String phase;
        private BigDecimal amount;
        private BigDecimal settledAmount;
    }
}