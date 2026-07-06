package com.unicom.post.modules.auth.controller;

import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.common.utils.JwtUtils;
import com.unicom.post.common.utils.PasswordUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.dto.*;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import com.unicom.post.modules.system.domain.entity.SysCity;
import com.unicom.post.modules.system.mapper.SysCityMapper;
import com.unicom.post.modules.system.service.SysOperationLogService;
import com.unicom.post.modules.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    private final SysOperationLogService operationLogService;
    private final SysCityMapper cityMapper;
    private final BizOutletMapper outletMapper;

    // 构造器注入，使用 @Qualifier 指定具体的Bean
    public AuthController(SysUserService userService,
                          SysRoleService roleService,
                          JwtUtils jwtUtils,
                          @Qualifier("operationLogService") SysOperationLogService operationLogService,
                          SysCityMapper cityMapper,
                          BizOutletMapper outletMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.jwtUtils = jwtUtils;
        this.operationLogService = operationLogService;
        this.cityMapper = cityMapper;
        this.outletMapper = outletMapper;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ip = IpUtils.getClientIp(httpRequest);
        String module = "LOGIN";
        String action = "用户登录";

        try {
            SysUser user = userService.findByUsername(username);
            if (user == null || !PasswordUtils.matches(request.getPassword(), user.getPasswordHash())) {
                operationLogService.log(module, action, null, "登录失败", ip, "FAIL", "用户名或密码错误");
                throw new BusinessException(401, "用户名或密码错误");
            }
            if (user.getStatus() == 0) {
                operationLogService.log(module, action, user.getId(), "登录失败", ip, "FAIL", "账号已被禁用");
                throw new BusinessException(401, "账号已被禁用，请联系管理员");
            }

            List<String> roles = roleService.findRoleCodesByUserId(user.getId());
            String token = jwtUtils.generateToken(
                    user.getId(), user.getUsername(), user.getRealName(), user.getPhone(),
                    user.getDataScopeType(), user.getScopeCityId(), user.getScopeOutletId(),
                    roles
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
            resp.setScopeCityName(getCityName(user.getScopeCityId()));
            resp.setScopeOutletName(getOutletName(user.getScopeOutletId()));
            resp.setLastLoginAt(user.getLastLoginAt());

            operationLogService.log(module, action, user.getId(), "登录成功", ip, "SUCCESS", null);
            return Result.success(resp);

        } catch (BusinessException e) {
            // 已在内部记录失败日志，直接抛出
            throw e;
        } catch (Exception e) {
            operationLogService.log(module, action, null, "登录异常", ip, "FAIL", e.getMessage());
            throw new BusinessException(500, "登录异常，请稍后重试");
        }
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        Long userId = getCurrentUserId();
        operationLogService.log("LOGIN", "用户登出", userId, "登出成功", ip, "SUCCESS", null);
        return Result.success("登出成功");
    }

    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                         HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        String module = "LOGIN";
        String action = "修改密码";
        try {
            if (!PasswordUtils.matches(request.getOldPassword(), user.getPasswordHash())) {
                operationLogService.log(module, action, user.getId(), "修改密码失败", ip, "FAIL", "旧密码不正确");
                throw new BusinessException(400, "旧密码不正确");
            }
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                operationLogService.log(module, action, user.getId(), "修改密码失败", ip, "FAIL", "两次输入密码不一致");
                throw new BusinessException(400, "两次输入密码不一致");
            }
            if (!isValidPassword(request.getNewPassword())) {
                operationLogService.log(module, action, user.getId(), "修改密码失败", ip, "FAIL", "新密码不符合安全规范");
                throw new BusinessException(400, "新密码不符合安全规范（至少8位，包含大小写字母、数字、特殊符号）");
            }

            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setPasswordHash(PasswordUtils.encode(request.getNewPassword()));
            userService.updateById(update);

            operationLogService.log(module, action, user.getId(), "修改密码成功", ip, "SUCCESS", null);
            return Result.success("密码修改成功，请重新登录");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            operationLogService.log(module, action, user.getId(), "修改密码异常", ip, "FAIL", e.getMessage());
            throw new BusinessException(500, "修改密码异常");
        }
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
        resp.setScopeCityName(getCityName(user.getScopeCityId()));
        resp.setScopeOutletName(getOutletName(user.getScopeOutletId()));
        resp.setLastLoginAt(user.getLastLoginAt());
        return Result.success(resp);
    }

    @PostMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<Map<String, Object>> resetPassword(@PathVariable Long userId,
                                                     @Valid @RequestBody ResetPasswordRequest request,
                                                     HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        Long operatorId = getCurrentUserId();   // 操作人，可用于记录
        String module = "LOGIN";
        String action = "重置密码";

        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            operationLogService.log(module, action, userId, "重置密码失败", ip, "FAIL", "用户不存在或已禁用");
            throw new BusinessException(400, "该用户不存在或已禁用");
        }

        try {
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

                operationLogService.log(module, action, userId, "重置密码成功（自动生成）", ip, "SUCCESS", null);
                return Result.success("密码已重置并发送短信", data);
            } else {
                // MANUAL 方式
                if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                    throw new BusinessException(400, "手动模式需要提供新密码");
                }
                if (!isValidPassword(request.getNewPassword())) {
                    throw new BusinessException(400, "新密码不符合安全规范（至少8位，包含大小写字母、数字、特殊符号）");
                }
                user.setPasswordHash(PasswordUtils.encode(request.getNewPassword()));
                userService.updateById(user);
                operationLogService.log(module, action, userId, "重置密码成功（手动指定）", ip, "SUCCESS", null);
                Map<String, Object> data = new HashMap<>();
                data.put("userId", user.getId());
                data.put("username", user.getUsername());
                data.put("passwordHash", "***");
                return Result.success("密码已设置", data);
            }
        } catch (Exception e) {
            operationLogService.log(module, action, userId, "重置密码异常", ip, "FAIL", e.getMessage());
            throw new BusinessException(500, "重置密码失败，请稍后重试");
        }
    }

    // ----- 辅助方法 -----

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

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = userService.findByUsername(username);
        return user != null ? user.getId() : null;
    }

    /**
     * 根据地市ID查询地市名称
     */
    private String getCityName(Long cityId) {
        if (cityId == null) return null;
        SysCity city = cityMapper.selectById(cityId);
        return city != null ? city.getCityName() : null;
    }

    /**
     * 根据网点ID查询网点名称
     */
    private String getOutletName(Long outletId) {
        if (outletId == null) return null;
        BizOutlet outlet = outletMapper.selectById(outletId);
        return outlet != null ? outlet.getOutletName() : null;
    }
}