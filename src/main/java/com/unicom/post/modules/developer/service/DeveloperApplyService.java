package com.unicom.post.modules.developer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.dto.DeveloperApplyAuditRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyResponse;
import com.unicom.post.modules.developer.dto.DeveloperCreateRequest;

import java.util.Map;

public interface DeveloperApplyService extends IService<BizDeveloperApply> {
    BizDeveloperApply submitApply(DeveloperApplyRequest request);
    Page<DeveloperApplyResponse> queryApplyList(String status, Long cityId, Long outletId, String keyword,
                                                Integer pageNo, Integer pageSize, Long currentUserId, String currentUserRole);
    DeveloperApplyResponse getApplyDetail(Long applyId, Long currentUserId, String currentUserRole);
    Map<String, Object> auditApply(Long applyId, DeveloperApplyAuditRequest request,
                                   Long currentUserId, String currentUserRole);
    void rejectApply(Long applyId, String rejectReason, Long currentUserId);

    /**
     * 管理员直接创建发展人（绕过审核流程）
     */
    Map<String, Object> createDeveloper(DeveloperCreateRequest request, Long currentUserId);
}