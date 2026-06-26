package com.unicom.post.modules.developer.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DeveloperApplyResponse {
    private Long id;
    private String applyNo;
    private String applicantName;
    private String applicantPhone;
    private String idCard;
    private String developerType;
    private String cityName;
    private String districtName;
    private String outletName;
    private String shopName;
    private String shopAddress;
    private String applyReason;
    private String status;
    private LocalDateTime createdAt;
    private List<AuditLog> auditLog;

    @Data
    public static class AuditLog {
        private String auditLevel;
        private String auditorName;
        private String auditStatus;
        private LocalDateTime auditTime;
        private String auditRemark;
    }
}