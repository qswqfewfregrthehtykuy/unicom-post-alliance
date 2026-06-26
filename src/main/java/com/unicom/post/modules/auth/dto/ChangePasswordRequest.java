package com.unicom.post.modules.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @Size(min = 8, message = "密码至少8位")
    private String newPassword;
    @NotBlank
    private String confirmPassword;
}