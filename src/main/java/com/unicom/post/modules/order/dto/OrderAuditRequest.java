package com.unicom.post.modules.order.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class OrderAuditRequest {
    @NotBlank
    private String auditLevel;   // OUTLET, CITY, PROVINCE
    @NotBlank
    private String status;       // APPROVED, REJECTED
    private String auditRemark;
}