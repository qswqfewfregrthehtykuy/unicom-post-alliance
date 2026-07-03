package com.unicom.post.modules.auth.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String resetType; // AUTO | MANUAL

    private String newPassword; // MANUAL模式时指定新密码
}