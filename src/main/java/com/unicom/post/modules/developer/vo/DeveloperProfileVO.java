package com.unicom.post.modules.developer.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 发展人个人资料 VO（Profile 页面使用）
 */
@Data
public class DeveloperProfileVO {
    private Long developerId;
    private Long outletId;
    private String outletName;           // 所属网点名称
    private String developerType;        // FREE_SHOP | OUTLET_USER
    private String shopName;             // 商铺名称
    private String level;                // 等级（暂用 developerType 替代）
    private Long totalDeveloped;         // 累计发展数
    private BigDecimal totalCommission;  // 累计佣金
    private String joinDate;             // 加入日期
}
