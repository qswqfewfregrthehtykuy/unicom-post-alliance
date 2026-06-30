package com.unicom.post.modules.statistics.vo;

import lombok.Data;

@Data
public class DevelopmentStatisticsVO {
    private String dimensionId;        // 分组ID（如网点ID、日期等）
    private String dimensionName;      // 分组名称
    private Long developmentCount;
    private Long mobileCardCount;
    private Long broadbandCount;
    private Long otherCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Long pendingCount;
}