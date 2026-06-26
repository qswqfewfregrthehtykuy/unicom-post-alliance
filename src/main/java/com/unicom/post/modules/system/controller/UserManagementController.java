package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.dto.UserCreateRequest;
import com.unicom.post.modules.system.dto.UserUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserManagementController {

    private final SysUserService userService;

    public UserManagementController(SysUserService userService) {
        this.userService = userService;
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
    public Result<SysUser> createUser(@Valid @RequestBody UserCreateRequest request) {
        SysUser user = userService.createUser(request);
        return Result.success("账号创建成功，初始密码已发送至手机", user);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateUser(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(userId, request);
        return Result.success("账号信息已更新");
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateUserStatus(@PathVariable Long userId, @RequestParam Integer status) {
        userService.updateUserStatus(userId, status);
        return Result.success("账号状态已更新");
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success("账号已注销");
    }
}