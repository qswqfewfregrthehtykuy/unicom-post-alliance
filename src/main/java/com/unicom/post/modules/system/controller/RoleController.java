package com.unicom.post.modules.system.controller;

import com.unicom.post.common.result.Result;
import com.unicom.post.modules.system.domain.entity.SysRole;
import com.unicom.post.modules.system.service.SysRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final SysRoleService roleService;

    public RoleController(SysRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleService.list());
    }
}