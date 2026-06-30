package com.unicom.post.modules.log.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String role;

    private String module;

    private String action;

    private Long targetId;

    private String targetType;

    private String beforeData;

    private String afterData;

    private String ipAddress;

    private String result;

    private String errorMsg;

    private LocalDateTime createdAt;
}