package com.unicom.post.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_org")
public class SysOrg {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String orgCode;
    private String orgName;
    private Integer orgLevel;
    private String orgType;
    private Long cityId;
    private Long provinceId;

    @PrivacyMask(MaskType.NAME)
    private String leaderName;

    @PrivacyMask(MaskType.PHONE)
    private String leaderPhone;
    private String address;
    private Integer sortOrder;
    private Integer status;
    private String remark;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}