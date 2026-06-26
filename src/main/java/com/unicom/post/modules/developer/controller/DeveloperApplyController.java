package com.unicom.post.modules.developer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.dto.DeveloperApplyAuditRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyResponse;
import com.unicom.post.modules.developer.service.DeveloperApplyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/developer-applies")
public class DeveloperApplyController {

    private final DeveloperApplyService applyService;

    public DeveloperApplyController(DeveloperApplyService applyService) {
        this.applyService = applyService;
    }

    /**
     * 提交申请
     */
    @PostMapping
    public Result<Map<String, Object>> submitApply(
            @Valid @RequestBody DeveloperApplyRequest request) {

        BizDeveloperApply apply = applyService.submitApply(request);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("applyId", apply.getId());
        data.put("applyNo", "APPLY-" + System.currentTimeMillis());
        data.put("applicantPhone", apply.getApplicantPhone());
        data.put("status", apply.getStatus());
        data.put("createTime", apply.getCreatedAt());
        data.put("estimatedReviewTime", "1-3个工作日");

        return Result.success("申请提交成功，请等待审核", data);
    }

    /**
     * 查询申请列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Page<DeveloperApplyResponse>> listApplies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        // TODO 后续从 SecurityContext 获取
        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        Page<DeveloperApplyResponse> page =
                applyService.queryApplyList(
                        status,
                        cityId,
                        outletId,
                        keyword,
                        pageNo,
                        pageSize,
                        currentUserId,
                        currentUserRole);

        return Result.success(page);
    }

    /**
     * 查询详情
     */
    @GetMapping("/{applyId}")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<DeveloperApplyResponse> getApplyDetail(
            @PathVariable Long applyId) {

        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        DeveloperApplyResponse detail =
                applyService.getApplyDetail(
                        applyId,
                        currentUserId,
                        currentUserRole);

        return Result.success(detail);
    }

    /**
     * 审核申请
     */
    @PutMapping("/{applyId}/audit")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Map<String, Object>> auditApply(
            @PathVariable Long applyId,
            @Valid @RequestBody DeveloperApplyAuditRequest request) {

        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        applyService.auditApply(
                applyId,
                request,
                currentUserId,
                currentUserRole);

        BizDeveloperApply apply = applyService.getById(applyId);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("applyId", applyId);
        data.put("status", apply.getStatus());
        data.put("nextAuditor", "省级");

        return Result.success("审核操作成功", data);
    }

    /**
     * 拒绝申请
     */
    @PutMapping("/{applyId}/reject")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<String> rejectApply(
            @PathVariable Long applyId,
            @RequestBody Map<String, String> body) {

        String rejectReason = body.get("rejectReason");

        Long currentUserId = 1L;

        applyService.rejectApply(
                applyId,
                rejectReason,
                currentUserId);

        return Result.success("申请已拒绝");
    }
}