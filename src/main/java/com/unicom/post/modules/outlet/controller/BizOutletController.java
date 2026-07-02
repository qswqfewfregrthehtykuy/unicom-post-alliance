package com.unicom.post.modules.outlet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.service.BizOutletService;
import com.unicom.post.modules.system.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/outlets")
public class BizOutletController {

    private final BizOutletService outletService;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BizOutletController(BizOutletService outletService,
                                @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.outletService = outletService;
        this.operationLogService = operationLogService;
    }

    /**
     * 查询网点列表（带分页）
     * 权限：省分管理员、地市管理员（本市）
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROVINCE', 'CITY')")
    public Result<Page<BizOutlet>> listOutlets(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Page<BizOutlet> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BizOutlet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOutlet::getIsDeleted, 0);
        if (cityId != null) {
            wrapper.eq(BizOutlet::getCityId, cityId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(BizOutlet::getOutletName, keyword)
                    .or().like(BizOutlet::getOutletCode, keyword));
        }
        wrapper.orderByAsc(BizOutlet::getCreatedAt);
        Page<BizOutlet> result = outletService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 查询网点详情
     */
    @GetMapping("/{outletId}")
    @PreAuthorize("hasAnyRole('PROVINCE', 'CITY', 'OUTLET', 'DEVELOPER')")
    public Result<BizOutlet> getOutletDetail(@PathVariable Long outletId) {
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        return Result.success(outlet);
    }

    /**
     * 创建网点（仅省分管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<BizOutlet> createOutlet(@Valid @RequestBody BizOutlet outlet,
                                           HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        // 校验唯一编码
        LambdaQueryWrapper<BizOutlet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOutlet::getOutletCode, outlet.getOutletCode())
                .eq(BizOutlet::getIsDeleted, 0);
        if (outletService.count(wrapper) > 0) {
            operationLogService.log("OUTLET", "创建网点", null,
                    "创建网点失败 - 编码已存在", ip, "FAIL", "网点编码已存在: " + outlet.getOutletCode());
            return Result.error(400, "网点编码已存在");
        }
        try {
            outlet.setStatus(1);
            outletService.save(outlet);
            operationLogService.logDetail("OUTLET", "创建网点", outlet.getId(), "Outlet",
                    "创建网点成功", null, toJson(outlet), ip, "SUCCESS", null);
            return Result.success("网点创建成功", outlet);
        } catch (Exception e) {
            operationLogService.log("OUTLET", "创建网点", null,
                    "创建网点失败", ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 编辑网点（仅省分管理员）
     */
    @PutMapping("/{outletId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOutlet(@PathVariable Long outletId,
                                        @RequestBody BizOutlet outlet,
                                        HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizOutlet exist = outletService.getById(outletId);
        if (exist == null || exist.getIsDeleted() == 1) {
            operationLogService.log("OUTLET", "编辑网点", outletId,
                    "编辑网点失败 - 网点不存在", ip, "FAIL", "网点不存在");
            return Result.error(404, "网点不存在");
        }
        String beforeData = toJson(exist);
        try {
            // 只允许修改部分字段
            exist.setOutletName(outlet.getOutletName());
            exist.setAddress(outlet.getAddress());
            exist.setManagerName(outlet.getManagerName());
            exist.setManagerPhone(outlet.getManagerPhone());
            exist.setAllianceMaster(outlet.getAllianceMaster());
            exist.setRemark(outlet.getRemark());
            outletService.updateById(exist);
            operationLogService.logDetail("OUTLET", "编辑网点", outletId, "Outlet",
                    "编辑网点成功", beforeData, toJson(exist), ip, "SUCCESS", null);
            return Result.success("网点更新成功");
        } catch (Exception e) {
            operationLogService.logDetail("OUTLET", "编辑网点", outletId, "Outlet",
                    "编辑网点失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 启用/禁用网点（仅省分管理员）
     */
    @PutMapping("/{outletId}/status")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOutletStatus(@PathVariable Long outletId,
                                              @RequestParam Integer status,
                                              HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        String beforeData = toJson(outlet);
        String action = status == 1 ? "启用网点" : "禁用网点";
        try {
            outlet.setStatus(status);
            outletService.updateById(outlet);
            operationLogService.logDetail("OUTLET", action, outletId, "Outlet",
                    action + "成功", beforeData, toJson(outlet), ip, "SUCCESS", null);
            return Result.success("网点状态已更新");
        } catch (Exception e) {
            operationLogService.logDetail("OUTLET", action, outletId, "Outlet",
                    action + "失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 删除网点（软删除，仅省分管理员）
     */
    @DeleteMapping("/{outletId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteOutlet(@PathVariable Long outletId,
                                        HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        String beforeData = toJson(outlet);
        try {
            outletService.removeById(outletId);
            operationLogService.logDetail("OUTLET", "删除网点", outletId, "Outlet",
                    "删除网点成功", beforeData, null, ip, "SUCCESS", null);
            return Result.success("网点删除成功");
        } catch (Exception e) {
            operationLogService.logDetail("OUTLET", "删除网点", outletId, "Outlet",
                    "删除网点失败", beforeData, null, ip, "FAIL", e.getMessage());
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