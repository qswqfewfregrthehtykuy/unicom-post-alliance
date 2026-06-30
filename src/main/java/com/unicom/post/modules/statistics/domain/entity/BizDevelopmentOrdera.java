package com.unicom.post.modules.statistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("biz_development_order")
public class BizDevelopmentOrdera {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long outletId;
    private Long cityId;
    private Long developerId;
    private String developSource;
    private String businessType;
    private String customerName;
    private String customerPhone;
    private String customerIdCard;
    private String customerAddress;
    private String leadStatus;
    private String formalStatus;
    private LocalDateTime leadAuditAt;
    private Long leadAuditorId;
    private LocalDateTime formalAuditAt;
    private Long formalAuditorId;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}