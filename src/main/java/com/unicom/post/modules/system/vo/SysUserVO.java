package com.unicom.post.modules.system.vo;

import com.unicom.post.common.annotation.PrivacyMask;
import com.unicom.post.common.enums.MaskType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户 VO（脱敏后返回给前端）
 */
@Data
public class SysUserVO {
    private Long id;
    private String username;

    @PrivacyMask(MaskType.NAME)
    private String realName;

    @PrivacyMask(MaskType.PHONE)
    private String phone;

    @PrivacyMask(MaskType.ID_CARD)
    private String idCard;

    private String dataScopeType;
    private Long scopeCityId;
    private Long scopeOutletId;
    private Integer status;
    private LocalDateTime lastLoginAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 仅创建时返回，不持久化 */
    private String tempPassword;
}
