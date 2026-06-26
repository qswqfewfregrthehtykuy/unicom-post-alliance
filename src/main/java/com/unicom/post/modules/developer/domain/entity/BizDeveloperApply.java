package com.unicom.post.modules.developer.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_developer_apply")
public class BizDeveloperApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String applicantName;
    private String applicantPhone;
    private String idCard;
    private String developerType;
    private Long cityId;
    private Long districtId;
    private Long outletId;
    private String shopName;
    private String shopAddress;
    private String applyReason;
    private String status;
    private Long reviewerId;
    private LocalDateTime reviewAt;
    private String reviewRemark;
    private Long userId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}