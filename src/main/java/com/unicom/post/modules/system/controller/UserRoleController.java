package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.domain.entity.SysUserRole;
import com.unicom.post.modules.system.mapper.SysUserRoleMapper;
import com.unicom.post.modules.system.service.SysOperationLogService;
import com.unicom.post.modules.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserRoleController {

    private final SysUserService userService;
    private final SysRoleService roleService;
    private final SysUserRoleMapper userRoleMapper;
    private final SysOperationLogService operationLogService;

    public UserRoleController(SysUserService userService,
                               SysRoleService roleService,
                               SysUserRoleMapper userRoleMapper,
                               @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleMapper = userRoleMapper;
        this.operationLogService = operationLogService;
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('PROVINCE')")
    @Transactional
    public Result<Map<String, Object>> assignRoles(@PathVariable Long userId,
                                                    @RequestBody List<Long> roleIds,
                                                    HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(400, "用户不存在或已被删除");
        }
        for (Long roleId : roleIds) {
            if (roleService.getById(roleId) == null) {
                operationLogService.log("USER_ROLE", "分配角色", userId,
                        "分配失败 - 角色不存在: " + roleId, ip, "FAIL", "角色不存在");
                throw new BusinessException(400, "角色不存在: roleId=" + roleId);
            }
        }
        try {
            userService.assignRoles(userId, roleIds);
            Map<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("roleIds", roleIds);
            operationLogService.logDetail("USER_ROLE", "分配角色", userId, "UserRole",
                    "分配成功，角色: " + roleIds.toString(), null, null, ip, "SUCCESS", null);
            return Result.success("角色分配成功", data);
        } catch (Exception e) {
            operationLogService.log("USER_ROLE", "分配角色", userId,
                    "分配失败", ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('PROVINCE')")
    @Transactional
    public Result<String> removeRole(@PathVariable Long userId,
                                      @PathVariable Long roleId,
                                      HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        List<Long> currentRoleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (currentRoleIds.size() <= 1) {
            operationLogService.log("USER_ROLE", "移除角色", userId,
                    "移除失败 - 至少保留一个角色", ip, "FAIL", "用户至少需要保留一个角色");
            throw new BusinessException(400, "用户至少需要保留一个角色");
        }
        if (!currentRoleIds.contains(roleId)) {
            operationLogService.log("USER_ROLE", "移除角色", userId,
                    "移除失败 - 无此角色: " + roleId, ip, "FAIL", "该用户没有此角色");
            throw new BusinessException(400, "该用户没有此角色");
        }
        try {
            userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId)
                    .eq(SysUserRole::getRoleId, roleId));
            operationLogService.logDetail("USER_ROLE", "移除角色", userId, "UserRole",
                    "移除角色成功，角色ID: " + roleId, null, null, ip, "SUCCESS", null);
            return Result.success("角色已移除");
        } catch (Exception e) {
            operationLogService.log("USER_ROLE", "移除角色", userId,
                    "移除失败", ip, "FAIL", e.getMessage());
            throw e;
        }
    }
}