package com.unicom.post.modules.system.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserUpdateRequest {
    private String realName;
    private String phone;
    private List<Long> roleIds;
    private String dataScopeType;
    private Long scopeCityId;
    private Long scopeOutletId;
    private String remark;
}