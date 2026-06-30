package com.unicom.post.modules.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unicom.post.common.result.Result;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.dto.OrderAuditRequest;
import com.unicom.post.modules.order.dto.OrderResponse;
import com.unicom.post.modules.order.dto.OrderSubmitRequest;
import com.unicom.post.modules.order.service.BizOrderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class BizOrderController {

    private final BizOrderService orderService;

    public BizOrderController(BizOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 提交业务发展记录
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OUTLET','DEVELOPER')")
    public Result<Map<String, Object>> submitOrder(
            @Valid @RequestBody OrderSubmitRequest request) {

        // TODO 后续从 SecurityContext 获取
        Long currentUserId = 1L;
        Long outletId = 1L;
        Long developerId = null;

        BizDevelopmentOrder order = orderService.submitOrder(
                request,
                currentUserId,
                outletId,
                developerId);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("customerPhone", order.getCustomerPhone());
        data.put("leadStatus", order.getLeadStatus());
        data.put("isDuplicate", false);
        data.put("createdAt", order.getCreatedAt());

        return Result.success("发展记录提交成功", data);
    }

    /**
     * 分页查询业务发展记录
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<Page<OrderResponse>> listOrders(
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Long outletId,
            @RequestParam(required = false) Long developerId,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String leadStatus,
            @RequestParam(required = false) String formalStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        Page<OrderResponse> page = orderService.queryOrderList(
                cityId,
                outletId,
                developerId,
                businessType,
                leadStatus,
                formalStatus,
                keyword,
                startDate,
                endDate,
                pageNo,
                pageSize,
                currentUserId,
                currentUserRole);

        return Result.success(page);
    }

    /**
     * 查询业务详情
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET','DEVELOPER')")
    public Result<OrderResponse> getOrderDetail(@PathVariable Long orderId) {

        OrderResponse detail = orderService.getOrderDetail(orderId);

        return Result.success(detail);
    }

    /**
     * 线索审核
     */
    @PutMapping("/{orderId}/lead-audit")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Map<String, Object>> leadAudit(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderAuditRequest request) {

        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        orderService.leadAudit(
                orderId,
                request,
                currentUserId,
                currentUserRole);

        BizDevelopmentOrder order = orderService.getById(orderId);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orderId", orderId);
        data.put("leadStatus", order.getLeadStatus());
//        data.put("nextAuditor", "省级");

        return Result.success("审核成功", data);
    }

    /**
     * 转正式审核
     */
    @PutMapping("/{orderId}/formal-audit")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Map<String, Object>> formalAudit(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderAuditRequest request) {

        Long currentUserId = 1L;
        String currentUserRole = "ROLE_PROVINCE";

        orderService.formalAudit(
                orderId,
                request,
                currentUserId,
                currentUserRole);

        BizDevelopmentOrder order = orderService.getById(orderId);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orderId", orderId);
        data.put("formalStatus", order.getFormalStatus());
//        data.put("commission", "佣金已生成（模拟）");

        return Result.success("审核成功", data);
    }

    /**
     * 驳回业务
     */
    @PutMapping("/{orderId}/reject")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<String> rejectOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body) {

        String auditPhase = body.get("auditPhase");
        String rejectReason = body.get("rejectReason");

        Long currentUserId = 1L;

        orderService.rejectOrder(
                orderId,
                auditPhase,
                rejectReason,
                currentUserId);

        return Result.success("申请已拒绝");
    }
}