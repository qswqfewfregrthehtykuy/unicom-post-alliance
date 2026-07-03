package com.unicom.post.modules.statistics.vo;

import lombok.Data;

/**
 * 业务类型分布 VO（饼图数据）
 */
@Data
public class BusinessBreakdownVO {
    private String businessType;     // MOBILE_CARD | BROADBAND | OTHER
    private String businessName;     // 联通号卡发展 | 邮政宽带融合 等
    private Long count;              // 数量
}
