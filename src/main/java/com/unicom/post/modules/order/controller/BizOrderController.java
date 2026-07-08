package com.unicom.post.modules.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;          // 实体类
import com.unicom.post.modules.auth.service.SysUserService; // Service
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.dto.OrderAuditRequest;
import com.unicom.post.modules.order.dto.OrderResponse;
import com.unicom.post.modules.order.dto.OrderSubmitRequest;
import com.unicom.post.modules.order.service.BizOrderService;
import com.unicom.post.modules.system.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class BizOrderController {

    private final BizOrderService orderService;
    private final SysUserService userService;
    private final BizDeveloperMapper developerMapper;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BizOrderController(BizOrderService orderService,
                              SysUserService userService,
                              BizDeveloperMapper developerMapper,
                              @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.orderService = orderService;
        this.userService = userService;
        this.developerMapper = developerMapper;
        this.operationLogService = operationLogService;
    }

    /**
     * 提交业务发展记录
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OUTLET','DEVELOPER')")
    public Result<Map<String, Object>> submitOrder(
            @Valid @RequestBody OrderSubmitRequest request,
            HttpServletRequest httpRequest) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentRole();
        String ip = IpUtils.getClientIp(httpRequest);

        Long outletId = null;
        Long developerId = null;

        if ("ROLE_OUTLET".equals(currentUserRole)) {
            // 网点管理员：从数据库查询用户获取 scopeOutletId
            SysUser user = userService.getById(currentUserId);
            if (user == null || user.getScopeOutletId() == null) {
                operationLogService.log("ORDER", "提交发展记录", null,
                        "提交失败 - 用户未绑定网点", ip, "FAIL", "当前用户未绑定网点");
                throw new BusinessException("当前用户未绑定网点，请联系管理员");
            }
            outletId = user.getScopeOutletId();
            // 优先使用请求中传入的发展人ID（网点管理员可代发展人录单）
            developerId = request.getDeveloperId();
        } else if ("ROLE_DEVELOPER".equals(currentUserRole)) {
            // 发展人：查询关联的发展人记录
            BizDeveloper developer = developerMapper.selectOne(
                    new LambdaQueryWrapper<BizDeveloper>()
                            .eq(BizDeveloper::getUserId, currentUserId)
                            .eq(BizDeveloper::getIsDeleted, 0)
            );
            if (developer == null) {
                operationLogService.log("ORDER", "提交发展记录", null,
                        "提交失败 - 未关联发展人", ip, "FAIL", "当前用户未关联发展人信息");
                throw new BusinessException("当前用户未关联发展人信息，请联系管理员");
            }
            outletId = developer.getOutletId();
            developerId = developer.getId();
        } else {
            operationLogService.log("ORDER", "提交发展记录", null,
                    "提交失败 - 无权限", ip, "FAIL", "当前角色无权提交发展记录");
            throw new BusinessException("当前角色无权提交发展记录");
        }

        try {
            BizDevelopmentOrder order = orderService.submitOrder(
                    request,
                    currentUserId,
                    outletId,
                    developerId);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getId());
            data.put("orderNo", order.getOrderNo());
            data.put("customerPhone", order.getCustomerPhone());
            data.put("leadStatus", order.getLeadStatus());
            data.put("isDuplicate", false);
            data.put("createdAt", order.getCreatedAt());

            operationLogService.logDetail("ORDER", "提交发展记录", order.getId(), "Order",
                    "提交成功", null, toJson(order), ip, "SUCCESS", null);
            return Result.success("发展记录提交成功", data);
        } catch (Exception e) {
            operationLogService.log("ORDER", "提交发展记录", null,
                    "提交失败", ip, "FAIL", e.getMessage());
            throw e;
        }
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

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentRole();

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
            @Valid @RequestBody OrderAuditRequest request,
            HttpServletRequest httpRequest) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentRole();
        String ip = IpUtils.getClientIp(httpRequest);

        BizDevelopmentOrder before = orderService.getById(orderId);
        String beforeData = toJson(before);

        try {
            orderService.leadAudit(orderId, request, currentUserId, currentUserRole);

            BizDevelopmentOrder order = orderService.getById(orderId);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", orderId);
            data.put("leadStatus", order.getLeadStatus());

            operationLogService.logDetail("ORDER", "线索审核", orderId, "Order",
                    "审核结果: " + request.getStatus(), beforeData, toJson(order), ip, "SUCCESS", null);
            return Result.success("审核成功", data);
        } catch (Exception e) {
            operationLogService.logDetail("ORDER", "线索审核", orderId, "Order",
                    "审核失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 转正式审核
     */
    @PutMapping("/{orderId}/formal-audit")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<Map<String, Object>> formalAudit(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderAuditRequest request,
            HttpServletRequest httpRequest) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentRole();
        String ip = IpUtils.getClientIp(httpRequest);

        BizDevelopmentOrder before = orderService.getById(orderId);
        String beforeData = toJson(before);

        try {
            orderService.formalAudit(orderId, request, currentUserId, currentUserRole);

            BizDevelopmentOrder order = orderService.getById(orderId);

            Map<String, Object> data = new HashMap<>();
            data.put("orderId", orderId);
            data.put("formalStatus", order.getFormalStatus());

            operationLogService.logDetail("ORDER", "转正式审核", orderId, "Order",
                    "审核结果: " + request.getStatus(), beforeData, toJson(order), ip, "SUCCESS", null);
            return Result.success("审核成功", data);
        } catch (Exception e) {
            operationLogService.logDetail("ORDER", "转正式审核", orderId, "Order",
                    "审核失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 驳回业务
     */
    @PutMapping("/{orderId}/reject")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<String> rejectOrder(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> body,
            HttpServletRequest httpRequest) {

        String auditPhase = body.get("auditPhase");
        String rejectReason = body.get("rejectReason");

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String ip = IpUtils.getClientIp(httpRequest);

        BizDevelopmentOrder before = orderService.getById(orderId);
        String beforeData = toJson(before);

        try {
            orderService.rejectOrder(orderId, auditPhase, rejectReason, currentUserId);

            BizDevelopmentOrder after = orderService.getById(orderId);
            operationLogService.logDetail("ORDER", "驳回订单", orderId, "Order",
                    "驳回阶段: " + auditPhase + ", 原因: " + (rejectReason != null ? rejectReason : "未填写"),
                    beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("申请已拒绝");
        } catch (Exception e) {
            operationLogService.logDetail("ORDER", "驳回订单", orderId, "Order",
                    "驳回失败", beforeData, null, ip, "FAIL", e.getMessage());
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