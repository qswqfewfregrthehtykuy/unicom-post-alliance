package com.unicom.post.modules.order.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderNo;
    private String outletName;
    private String developerName;
    private String businessType;
    private String developSource;
    private String customerName;
    private String customerPhone;
    private String customerIdCard;
    private String customerAddress;
    private String leadStatus;
    private String formalStatus;
    private List<AuditLog> leadAuditLog;
    private List<AuditLog> formalAuditLog;
    private LocalDateTime createdAt;

    @Data
    public static class AuditLog {
        private String level;
        private String auditorName;
        private String auditStatus;
        private LocalDateTime auditTime;
        private String auditRemark;
    }
}