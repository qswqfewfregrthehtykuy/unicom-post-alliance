package com.unicom.post.modules.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.dto.OrderAuditRequest;
import com.unicom.post.modules.order.dto.OrderSubmitRequest;
import com.unicom.post.modules.order.dto.OrderResponse;

public interface BizOrderService extends IService<BizDevelopmentOrder> {

    BizDevelopmentOrder submitOrder(OrderSubmitRequest request, Long currentUserId, Long outletId, Long developerId);

    Page<OrderResponse> queryOrderList(Long cityId, Long outletId, Long developerId,
                                       String businessType, String leadStatus, String formalStatus,
                                       String keyword, String startDate, String endDate,
                                       Integer pageNo, Integer pageSize,
                                       Long currentUserId, String currentUserRole);

    OrderResponse getOrderDetail(Long orderId);

    void leadAudit(Long orderId, OrderAuditRequest request, Long currentUserId, String currentUserRole);

    void formalAudit(Long orderId, OrderAuditRequest request, Long currentUserId, String currentUserRole);

    void rejectOrder(Long orderId, String auditPhase, String rejectReason, Long currentUserId);
}