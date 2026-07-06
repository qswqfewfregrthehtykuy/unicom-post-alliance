package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.dto.UserCreateRequest;
import com.unicom.post.modules.system.dto.UserUpdateRequest;
import com.unicom.post.modules.system.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {

    private final SysUserService userService;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserManagementController(SysUserService userService,
                                     @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<Page<SysUser>> listUsers(@RequestParam(required = false) String role,
                                           @RequestParam(required = false) Long cityId,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer pageNo,
                                           @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<SysUser> page = userService.queryUserList(role, cityId, status, keyword, pageNo, pageSize);
        return Result.success(page);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<SysUser> createUser(@Valid @RequestBody UserCreateRequest request,
                                       HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        try {
            SysUser user = userService.createUser(request);
            operationLogService.logDetail("USER", "创建用户", user.getId(), "User",
                    "创建用户成功", null, toJson(user), ip, "SUCCESS", null);
            return Result.success("账号创建成功，初始密码已发送至手机", user);
        } catch (Exception e) {
            operationLogService.log("USER", "创建用户", null, "创建用户失败",
                    ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateUser(@PathVariable Long userId,
                                      @Valid @RequestBody UserUpdateRequest request,
                                      HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysUser before = userService.getById(userId);
        String beforeData = toJson(before);
        try {
            userService.updateUser(userId, request);
            SysUser after = userService.getById(userId);
            operationLogService.logDetail("USER", "修改用户信息", userId, "User",
                    "修改用户信息成功", beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("账号信息已更新");
        } catch (Exception e) {
            operationLogService.logDetail("USER", "修改用户信息", userId, "User",
                    "修改用户信息失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateUserStatus(@PathVariable Long userId,
                                            @RequestParam Integer status,
                                            HttpServletRequest httpRequest) {
        // 禁止管理员禁用/停用自己的账号
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(userId) && status == 0) {
            throw new BusinessException(400, "不能禁用自己的账号");
        }

        String ip = IpUtils.getClientIp(httpRequest);
        SysUser before = userService.getById(userId);
        String beforeData = toJson(before);
        String action = status == 1 ? "启用用户" : "禁用用户";
        try {
            userService.updateUserStatus(userId, status);
            SysUser after = userService.getById(userId);
            operationLogService.logDetail("USER", action, userId, "User",
                    action + "成功", beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("账号状态已更新");
        } catch (Exception e) {
            operationLogService.logDetail("USER", action, userId, "User",
                    action + "失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteUser(@PathVariable Long userId,
                                      HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysUser before = userService.getById(userId);
        String beforeData = toJson(before);
        try {
            userService.deleteUser(userId);
            operationLogService.logDetail("USER", "删除用户", userId, "User",
                    "删除用户成功", beforeData, null, ip, "SUCCESS", null);
            return Result.success("账号已注销");
        } catch (Exception e) {
            operationLogService.logDetail("USER", "删除用户", userId, "User",
                    "删除用户失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    private String toJson(Object obj) {
        try {
            return obj != null ? objectMapper.writeValueAsString(obj) : null;
        } catch (Exception e) {
            return null;
        }
    }
}