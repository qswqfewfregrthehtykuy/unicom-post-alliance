package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.modules.system.domain.entity.SysOrg;
import com.unicom.post.modules.system.service.SysOperationLogService;
import com.unicom.post.modules.system.service.SysOrgService;
import com.unicom.post.modules.system.vo.OrgTreeNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/org")
public class OrgController {

    private final SysOrgService orgService;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrgController(SysOrgService orgService,
                          @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.orgService = orgService;
        this.operationLogService = operationLogService;
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
    public Result<SysOrg> createOrg(@Valid @RequestBody SysOrg org,
                                     HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        try {
            orgService.createOrg(org);
            operationLogService.logDetail("ORG", "创建组织", org.getId(), "Org",
                    "创建组织成功", null, toJson(org), ip, "SUCCESS", null);
            return Result.success("组织创建成功", org);
        } catch (Exception e) {
            operationLogService.log("ORG", "创建组织", null,
                    "创建组织失败", ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{orgId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOrg(@PathVariable Long orgId,
                                     @RequestBody SysOrg org,
                                     HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysOrg before = orgService.getById(orgId);
        String beforeData = toJson(before);
        try {
            orgService.updateOrg(orgId, org);
            SysOrg after = orgService.getById(orgId);
            operationLogService.logDetail("ORG", "编辑组织", orgId, "Org",
                    "编辑组织成功", beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("组织更新成功");
        } catch (Exception e) {
            operationLogService.logDetail("ORG", "编辑组织", orgId, "Org",
                    "编辑组织失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/{orgId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteOrg(@PathVariable Long orgId,
                                     HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        SysOrg before = orgService.getById(orgId);
        String beforeData = toJson(before);
        try {
            orgService.deleteOrg(orgId);
            operationLogService.logDetail("ORG", "删除组织", orgId, "Org",
                    "删除组织成功", beforeData, null, ip, "SUCCESS", null);
            return Result.success("组织删除成功");
        } catch (Exception e) {
            operationLogService.logDetail("ORG", "删除组织", orgId, "Org",
                    "删除组织失败", beforeData, null, ip, "FAIL", e.getMessage());
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