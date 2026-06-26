package com.unicom.post.modules.developer.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class DeveloperApplyAuditRequest {
    @NotBlank
    private String auditLevel;
    @NotBlank
    private String status;
    private String auditRemark;
}