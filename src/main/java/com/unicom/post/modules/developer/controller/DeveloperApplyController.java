package com.unicom.post.modules.developer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.auth.domain.UserPrincipal;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.dto.DeveloperApplyAuditRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyResponse;
import com.unicom.post.modules.developer.service.DeveloperApplyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
     * 提交申请（公开接口，无需认证）
     */
    @PostMapping
    public Result<Map<String, Object>> submitApply(
            @Valid @RequestBody DeveloperApplyRequest request) {

        BizDeveloperApply apply = applyService.submitApply(request);

        Map<String, Object> data = new HashMap<>();
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
            @RequestParam(defaultValue = "20") Integer pageSize,
            Authentication authentication) {

        // 从 Authentication 中提取当前用户信息
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return Result.error(401, "用户信息无效");
        }
        UserPrincipal currentUser = (UserPrincipal) principal;
        Long currentUserId = currentUser.getId();
        // 获取第一个角色（实际可能有多个，可根据业务调整）
        String currentUserRole = currentUser.getRoles().isEmpty() ? null : currentUser.getRoles().get(0);
        if (currentUserRole == null) {
            return Result.error(403, "用户无有效角色");
        }

        Page<DeveloperApplyResponse> page = applyService.queryApplyList(
                status, cityId, outletId, keyword,
                pageNo, pageSize,
                currentUserId, currentUserRole);

        return Result.success(page);
    }

    /**
     * 查询详情
     */
    @GetMapping("/{applyId}")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<DeveloperApplyResponse> getApplyDetail(
            @PathVariable Long applyId,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return Result.error(401, "用户信息无效");
        }
        UserPrincipal currentUser = (UserPrincipal) principal;
        Long currentUserId = currentUser.getId();
        String currentUserRole = currentUser.getRoles().isEmpty() ? null : currentUser.getRoles().get(0);
        if (currentUserRole == null) {
            return Result.error(403, "用户无有效角色");
        }

        DeveloperApplyResponse detail = applyService.getApplyDetail(
                applyId, currentUserId, currentUserRole);

        return Result.success(detail);
    }

    /**
     * 审核申请
     */
    @PutMapping("/{applyId}/audit")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Map<String, Object>> auditApply(
            @PathVariable Long applyId,
            @Valid @RequestBody DeveloperApplyAuditRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return Result.error(401, "用户信息无效");
        }
        UserPrincipal currentUser = (UserPrincipal) principal;
        Long currentUserId = currentUser.getId();
        String currentUserRole = currentUser.getRoles().isEmpty() ? null : currentUser.getRoles().get(0);
        if (currentUserRole == null) {
            return Result.error(403, "用户无有效角色");
        }

        Map<String, Object> result = applyService.auditApply(
                applyId, request, currentUserId, currentUserRole);

        return Result.success("审核操作成功", result);
    }

    /**
     * 拒绝申请（已废弃，建议使用audit接口直接拒绝）
     */
    @PutMapping("/{applyId}/reject")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<String> rejectApply(
            @PathVariable Long applyId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal)) {
            return Result.error(401, "用户信息无效");
        }
        UserPrincipal currentUser = (UserPrincipal) principal;
        Long currentUserId = currentUser.getId();

        String rejectReason = body.get("rejectReason");
        applyService.rejectApply(applyId, rejectReason, currentUserId);

        return Result.success("申请已拒绝");
    }
}