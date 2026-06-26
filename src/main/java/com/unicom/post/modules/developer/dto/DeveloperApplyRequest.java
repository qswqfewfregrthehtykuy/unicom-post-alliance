package com.unicom.post.modules.developer.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeveloperApplyRequest {
    @NotBlank
    private String applicantName;
    @NotBlank
    private String applicantPhone;
    @NotBlank
    private String idCard;
    @NotBlank
    private String developerType;
    @NotNull
    private Long cityId;
    @NotNull
    private Long districtId;
    @NotNull
    private Long outletId;
    private String shopName;
    private String shopAddress;
    private String applyReason;
}