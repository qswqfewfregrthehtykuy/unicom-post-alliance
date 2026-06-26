package com.unicom.post.modules.outlet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.service.BizOutletService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/outlets")
public class BizOutletController {

    private final BizOutletService outletService;

    public BizOutletController(BizOutletService outletService) {
        this.outletService = outletService;
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
    public Result<BizOutlet> createOutlet(@Valid @RequestBody BizOutlet outlet) {
        // 校验唯一编码
        LambdaQueryWrapper<BizOutlet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOutlet::getOutletCode, outlet.getOutletCode())
                .eq(BizOutlet::getIsDeleted, 0);
        if (outletService.count(wrapper) > 0) {
            return Result.error(400, "网点编码已存在");
        }
        outlet.setStatus(1);
        outletService.save(outlet);
        return Result.success("网点创建成功", outlet);
    }

    /**
     * 编辑网点（仅省分管理员）
     */
    @PutMapping("/{outletId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOutlet(@PathVariable Long outletId,
                                       @RequestBody BizOutlet outlet) {
        BizOutlet exist = outletService.getById(outletId);
        if (exist == null || exist.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        // 只允许修改部分字段
        exist.setOutletName(outlet.getOutletName());
        exist.setAddress(outlet.getAddress());
        exist.setManagerName(outlet.getManagerName());
        exist.setManagerPhone(outlet.getManagerPhone());
        exist.setAllianceMaster(outlet.getAllianceMaster());
        exist.setRemark(outlet.getRemark());
        outletService.updateById(exist);
        return Result.success("网点更新成功");
    }

    /**
     * 启用/禁用网点（仅省分管理员）
     */
    @PutMapping("/{outletId}/status")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateOutletStatus(@PathVariable Long outletId,
                                             @RequestParam Integer status) {
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        outlet.setStatus(status);
        outletService.updateById(outlet);
        return Result.success("网点状态已更新");
    }

    /**
     * 删除网点（软删除，仅省分管理员）
     */
    @DeleteMapping("/{outletId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> deleteOutlet(@PathVariable Long outletId) {
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getIsDeleted() == 1) {
            return Result.error(404, "网点不存在");
        }
        // 检查是否有发展人关联（可选）
        outletService.removeById(outletId);
        return Result.success("网点删除成功");
    }
}