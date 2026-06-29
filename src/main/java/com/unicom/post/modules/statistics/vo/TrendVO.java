package com.unicom.post.modules.statistics.vo;

import lombok.Data;

@Data
public class TrendVO {
    private String dateTime;   // 格式根据粒度不同
    private Long value;        // 数量或金额（BigDecimal可能更合适，但文档用Long，实际金额可能带小数）
}