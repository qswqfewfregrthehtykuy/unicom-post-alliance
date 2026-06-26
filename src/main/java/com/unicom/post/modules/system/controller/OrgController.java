package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.system.domain.entity.SysOrg;
import com.unicom.post.modules.system.service.SysOrgService;
import com.unicom.post.modules.system.vo.OrgTreeNode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/org")
public class OrgController {

    private final SysOrgService orgService;

    public OrgController(SysOrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/tree")
    public Result<OrgTreeNode> getOrgTree(@RequestParam(required = false, defaultValue = "true") Boolean includeOutlet) {
        OrgTreeNode tree = orgService.getOrgTree(includeOutlet);
        return Result.success(tree);
    }

    @GetMapping("/{orgId}/children")
    public Result<Page<SysOrg>> getChildren(@PathVariable Long orgId,
                                            @RequestParam(defaultValue = "1") Integer pageNo,
                                            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<SysOrg> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysOrg::getParentId, orgId)
                .eq(SysOrg::getIsDeleted, 0)
                .orderByAsc(SysOrg::getSortOrder);
        Page<SysOrg> result = orgService.page(page, wrapper);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<SysOrg> createOrg(@Valid @RequestBody SysOrg org) {
        orgService.createOrg(org);
        return Result.success("组织创建成功", org);
    }

    @PutMapping("/{orgId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOrg(@PathVariable Long orgId, @RequestBody SysOrg org) {
        orgService.updateOrg(orgId, org);
        return Result.success("组织更新成功");
    }

    @DeleteMapping("/{orgId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteOrg(@PathVariable Long orgId) {
        orgService.deleteOrg(orgId);
        return Result.success("组织删除成功");
    }
}