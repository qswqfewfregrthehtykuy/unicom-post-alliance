package com.unicom.post.modules.outlet.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_outlet")
public class BizOutlet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String outletCode;
    private String outletName;
    private Long cityId;
    private Long districtId;
    @TableField(exist = false)
    private String cityName;
    @TableField(exist = false)
    private String districtName;
    private String address;

    @PrivacyMask(MaskType.NAME)
    private String managerName;

    @PrivacyMask(MaskType.PHONE)
    private String managerPhone;
    private String allianceMaster;
    private Long adminUserId;
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