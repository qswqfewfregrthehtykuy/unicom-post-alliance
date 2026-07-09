package com.unicom.post.modules.developer.dto;

import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeveloperApplyResponse {
    private Long id;
    private String applyNo;

    @PrivacyMask(MaskType.NAME)
    private String applicantName;

    @PrivacyMask(MaskType.PHONE)
    private String applicantPhone;

    @PrivacyMask(MaskType.ID_CARD)
    private String idCard;
    private String developerType;
    private Long cityId;
    private String cityName;
    private Long districtId;
    private String districtName;
    private Long outletId;
    private String outletName;
    private String shopName;
    private String shopAddress;
    private String applyReason;
    private String status;
    private LocalDateTime createdAt;
    private Long userId; // 审核通过后创建的账号ID

    // 审核日志（简化版，用于列表）
    private List<AuditLog> auditLog;

    // 审核历史（详情使用）
    private List<AuditHistory> auditHistory;

    @Data
    public static class AuditLog {
        private String auditLevel;
        private String auditorName;
        private String auditStatus; // PENDING/APPROVED/REJECTED
        private LocalDateTime auditTime;
    }

    @Data
    public static class AuditHistory {
        private String auditLevel;
        private Long auditorId;
        private String auditorName;
        private String auditStatus;
        private String auditRemark;
        private LocalDateTime auditTime;
    }
}