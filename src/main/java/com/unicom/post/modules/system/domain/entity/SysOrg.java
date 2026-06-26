package com.unicom.post.modules.system.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
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
    private String leaderName;
    private String leaderPhone;
    private String address;
    private Integer sortOrder;
    private Integer status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;
}