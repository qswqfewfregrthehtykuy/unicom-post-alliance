package com.unicom.post.modules.developer.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理员直接创建发展人请求（绕过审核流程）
 */
@Data
public class DeveloperCreateRequest {
    @NotBlank(message = "姓名不能为空")
    private String applicantName;

    @NotBlank(message = "手机号不能为空")
    private String applicantPhone;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "发展人类型不能为空")
    private String developerType;   // FREE_SHOP / SELF_EMPLOYED

    @NotNull(message = "地市不能为空")
    private Long cityId;

    @NotNull(message = "区县不能为空")
    private Long districtId;

    @NotNull(message = "网点不能为空")
    private Long outletId;

    private String shopName;
    private String shopAddress;
    private String remark;
}
