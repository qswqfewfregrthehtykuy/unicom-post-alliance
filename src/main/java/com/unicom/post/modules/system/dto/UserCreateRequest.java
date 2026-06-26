package com.unicom.post.modules.system.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50)
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Size(max = 20)
    private String phone;

    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;

    @NotBlank(message = "数据权限类型不能为空")
    private String dataScopeType;

    private Long scopeCityId;
    private Long scopeOutletId;
    private String remark;
}