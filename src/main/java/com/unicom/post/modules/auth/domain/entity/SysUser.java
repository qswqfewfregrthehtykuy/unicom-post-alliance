package com.unicom.post.modules.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private String realName;
    private String phone;

    @TableField("idcard")
    private String idCard;
    private String dataScopeType;
    private Long scopeCityId;
    private Long scopeOutletId;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private String tempPassword;  // 仅用于创建用户时返回临时密码
}