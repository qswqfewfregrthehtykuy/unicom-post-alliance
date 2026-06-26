package com.unicom.post.modules.auth.controller;

import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.JwtUtils;
import com.unicom.post.common.utils.PasswordUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.dto.*;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final JwtUtils jwtUtils;

    public AuthController(SysUserService userService, SysRoleService roleService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.roleService = roleService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userService.findByUsername(request.getUsername());
        if (user == null || !PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(401, "账号已被禁用，请联系管理员");
        }

        List<String> roles = roleService.findRoleCodesByUserId(user.getId());
        String token = jwtUtils.generateToken(
                user.getId(), user.getUsername(), user.getRealName(), user.getPhone(),
                user.getDataScopeType(), user.getScopeCityId(), user.getScopeOutletId(),
                roles   // 👈 传入角色列表
        );

        userService.updateLoginInfo(user.getId());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setPhone(user.getPhone());
        resp.setRoles(roles);
        resp.setDataScopeType(user.getDataScopeType());
        resp.setScopeCityId(user.getScopeCityId());
        resp.setScopeOutletId(user.getScopeOutletId());
        resp.setLastLoginAt(user.getLastLoginAt());

        return Result.success(resp);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!PasswordUtils.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(400, "旧密码不正确");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(400, "两次输入密码不一致");
        }
        if (!isValidPassword(request.getNewPassword())) {
            throw new BusinessException(400, "新密码不符合安全规范（至少8位，包含大小写字母、数字、特殊符号）");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPasswordHash(PasswordUtils.encode(request.getNewPassword()));
        userService.updateById(update);
        return Result.success("密码修改成功，请重新登录");
    }

    @GetMapping("/me")
    public Result<LoginResponse> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        List<String> roles = roleService.findRoleCodesByUserId(user.getId());
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setPhone(user.getPhone());
        resp.setRoles(roles);
        resp.setDataScopeType(user.getDataScopeType());
        resp.setScopeCityId(user.getScopeCityId());
        resp.setScopeOutletId(user.getScopeOutletId());
        resp.setLastLoginAt(user.getLastLoginAt());
        return Result.success(resp);
    }

    @PostMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<Map<String, Object>> resetPassword(@PathVariable Long userId, @Valid @RequestBody ResetPasswordRequest request) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(400, "该用户不存在或已禁用");
        }
        if ("AUTO".equalsIgnoreCase(request.getResetType())) {
            String newPassword = PasswordUtils.generateRandomPassword();
            user.setPasswordHash(PasswordUtils.encode(newPassword));
            userService.updateById(user);
            log.info("重置密码，发送短信至 {}，临时密码：{}", user.getPhone(), newPassword);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("tempPassword", newPassword);
            data.put("smsStatus", "sent");
            data.put("smsPhone", user.getPhone());
            data.put("expiryTime", LocalDateTime.now().plusHours(24).toString());
            return Result.success("密码已重置并发送短信", data);
        } else {
            // MANUAL 方式：管理员手动指定，这里仅返回成功信息，不返回密码
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            // 可添加其他信息，但不含密码
            return Result.success("密码已设置", data);
        }
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        String special = "!@#$%^&*";
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else if (special.indexOf(c) >= 0) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}