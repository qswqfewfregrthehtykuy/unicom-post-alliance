package com.unicom.post.modules.auth.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private List<String> roles;
    private String dataScopeType;
    private Long scopeCityId;
    private Long scopeOutletId;
    private String scopeCityName;      // 数据权限对应的地市名称
    private String scopeOutletName;    // 数据权限对应的网点名称
    private LocalDateTime lastLoginAt;
}