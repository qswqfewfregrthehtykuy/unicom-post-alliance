package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.domain.entity.SysUserRole;
import com.unicom.post.modules.system.mapper.SysUserRoleMapper;
import com.unicom.post.modules.system.service.SysRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    public UserRoleController(SysUserService userService, SysRoleService roleService, SysUserRoleMapper userRoleMapper) {
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleMapper = userRoleMapper;
    }

    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('PROVINCE')")
    @Transactional
    public Result<Map<String, Object>> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(400, "用户不存在或已被删除");
        }
        for (Long roleId : roleIds) {
            if (roleService.getById(roleId) == null) {
                throw new BusinessException(400, "角色不存在: roleId=" + roleId);
            }
        }
        userService.assignRoles(userId, roleIds);
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("roleIds", roleIds);
        return Result.success("角色分配成功", data);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('PROVINCE')")
    @Transactional
    public Result<String> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }
        List<Long> currentRoleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (currentRoleIds.size() <= 1) {
            throw new BusinessException(400, "用户至少需要保留一个角色");
        }
        if (!currentRoleIds.contains(roleId)) {
            throw new BusinessException(400, "该用户没有此角色");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId));
        return Result.success("角色已移除");
    }
}