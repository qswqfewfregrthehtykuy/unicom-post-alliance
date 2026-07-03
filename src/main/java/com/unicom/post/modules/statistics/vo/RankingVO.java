package com.unicom.post.modules.statistics.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankingVO {
    private Integer rank;
    private String name;               // 网点名称或发展人姓名，根据rankType动态
    private Long developmentCount;
    private BigDecimal commissionAmount;
    private String developmentTrend;   // 环比趋势字符串
}