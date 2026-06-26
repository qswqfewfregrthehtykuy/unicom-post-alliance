package com.unicom.post.modules.developer.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_developer")
public class BizDeveloper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long applyId;
    private Long outletId;
    private String developerType;
    private String shopName;
    private String shopAddress;
    private Long createdBy;
    private Long createdOutletAdmin;
    private Integer status;
    private LocalDate joinDate;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}