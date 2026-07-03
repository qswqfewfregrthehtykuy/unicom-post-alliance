package com.unicom.post.modules.statistics.vo;

import lombok.Data;

/**
 * 审核动态 VO（Dashboard 底部审核流表格）
 */
@Data
public class RecentAuditVO {
    private Long applyId;
    private String applyNo;
    private String applicantName;
    private String applicantPhone;
    private String developerType;      // FREE_SHOP | OUTLET_USER
    private String outletName;         // 归属网点
    private String cityName;           // 归属地市
    private String status;             // PENDING | OUTLET_APPROVED | CITY_APPROVED | PROVINCE_APPROVED | REJECTED
    private String createdAt;          // 提交时间
}
