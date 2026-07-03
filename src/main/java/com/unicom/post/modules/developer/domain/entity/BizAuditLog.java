package com.unicom.post.modules.developer.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 业务审核日志表 - 记录发展人申请和业务订单的多级审核历史
 */
@Data
@TableName("biz_audit_log")
public class BizAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标类型：DEVELOPER_APPLY | ORDER */
    private String targetType;

    /** 目标ID（申请ID或订单ID） */
    private Long targetId;

    /** 审核级别：OUTLET | CITY | PROVINCE */
    private String auditLevel;

    /** 审核阶段：LEAD | FORMAL（仅订单使用） */
    private String auditPhase;

    /** 审核人ID */
    private Long auditorId;

    /** 审核人姓名 */
    private String auditorName;

    /** 审核结果：APPROVED | REJECTED */
    private String auditStatus;

    /** 审核备注 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
