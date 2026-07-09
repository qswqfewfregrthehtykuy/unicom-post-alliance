package com.unicom.post.modules.order.dto;

import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
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

    @PrivacyMask(MaskType.NAME)
    private String customerName;

    @PrivacyMask(MaskType.PHONE)
    private String customerPhone;

    @PrivacyMask(MaskType.ID_CARD)
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