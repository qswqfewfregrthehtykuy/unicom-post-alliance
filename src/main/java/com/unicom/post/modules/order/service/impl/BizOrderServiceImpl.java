package com.unicom.post.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.developer.domain.entity.BizAuditLog;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.mapper.BizAuditLogMapper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.order.domain.entity.BizCommissionDetailb;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.dto.OrderAuditRequest;
import com.unicom.post.modules.order.dto.OrderSubmitRequest;
import com.unicom.post.modules.order.dto.OrderResponse;
import com.unicom.post.modules.order.mapper.BizCommissionDetailMapper;
import com.unicom.post.modules.order.mapper.BizDevelopmentOrderMapper;
import com.unicom.post.modules.order.service.BizOrderService;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.service.BizOutletService;
import com.unicom.post.modules.system.domain.entity.BizCommissionRule;
import com.unicom.post.modules.system.service.BizCommissionRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BizOrderServiceImpl extends ServiceImpl<BizDevelopmentOrderMapper, BizDevelopmentOrder>
        implements BizOrderService {

    private final BizOutletService outletService;
    private final BizDeveloperMapper developerMapper;
    private final SysUserService userService;
    private final BizCommissionRuleService commissionRuleService;
    private final BizCommissionDetailMapper commissionDetailMapper;
    private final BizAuditLogMapper auditLogMapper;

    public BizOrderServiceImpl(BizOutletService outletService,
                               BizDeveloperMapper developerMapper,
                               SysUserService userService,
                               BizCommissionRuleService commissionRuleService,
                               BizCommissionDetailMapper commissionDetailMapper,
                               BizAuditLogMapper auditLogMapper) {
        this.outletService = outletService;
        this.developerMapper = developerMapper;
        this.userService = userService;
        this.commissionRuleService = commissionRuleService;
        this.commissionDetailMapper = commissionDetailMapper;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional
    public BizDevelopmentOrder submitOrder(OrderSubmitRequest request, Long currentUserId,
                                           Long outletId, Long developerId) {
        // 1. 校验网点
        BizOutlet outlet = outletService.getById(outletId);
        if (outlet == null || outlet.getStatus() == 0) {
            throw new BusinessException("网点不存在或已停用");
        }

        // 2. 去重校验（调用自定义Mapper方法）
        BizDevelopmentOrder duplicate = baseMapper.findDuplicateOrder(
                request.getCustomerPhone(), request.getBusinessType(), outletId);
        if (duplicate != null) {
            throw new BusinessException(400, "该客户在30天内已有相同业务的发展记录，请勿重复提交");
        }

        // 3. 生成订单号
        String orderNo = "BIZ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        BizDevelopmentOrder order = new BizDevelopmentOrder();
        BeanUtils.copyProperties(request, order);
        order.setOrderNo(orderNo);
        order.setOutletId(outletId);
        order.setCityId(outlet.getCityId());
        order.setDeveloperId(developerId);
        order.setLeadStatus("PENDING");
        order.setFormalStatus("N/A");
        this.save(order);
        return order;
    }

    @Override
    public Page<OrderResponse> queryOrderList(Long cityId, Long outletId, Long developerId,
                                              String businessType, String leadStatus, String formalStatus,
                                              String keyword, String startDate, String endDate,
                                              Integer pageNo, Integer pageSize,
                                              Long currentUserId, String currentUserRole) {
        Page<BizDevelopmentOrder> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0);
        if (cityId != null) wrapper.eq(BizDevelopmentOrder::getCityId, cityId);
        if (outletId != null) wrapper.eq(BizDevelopmentOrder::getOutletId, outletId);
        if (developerId != null) wrapper.eq(BizDevelopmentOrder::getDeveloperId, developerId);
        if (StringUtils.hasText(businessType)) wrapper.eq(BizDevelopmentOrder::getBusinessType, businessType);
        if (StringUtils.hasText(leadStatus)) wrapper.eq(BizDevelopmentOrder::getLeadStatus, leadStatus);
        if (StringUtils.hasText(formalStatus)) wrapper.eq(BizDevelopmentOrder::getFormalStatus, formalStatus);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizDevelopmentOrder::getCustomerName, keyword)
                    .or().like(BizDevelopmentOrder::getCustomerPhone, keyword));
        }
        if (StringUtils.hasText(startDate)) {
            LocalDate start = LocalDate.parse(startDate);
            wrapper.ge(BizDevelopmentOrder::getCreatedAt, start.atStartOfDay());
        }
        if (StringUtils.hasText(endDate)) {
            LocalDate end = LocalDate.parse(endDate);
            wrapper.le(BizDevelopmentOrder::getCreatedAt, end.atTime(23, 59, 59));
        }
        // 数据权限过滤：根据角色自动添加过滤条件（此处简化，实际可扩展）
        // 例如：若角色是CITY，则只查询scope_city_id下的订单
        wrapper.orderByDesc(BizDevelopmentOrder::getCreatedAt);
        Page<BizDevelopmentOrder> result = this.page(page, wrapper);

        // 转换为Response对象
        Page<OrderResponse> respPage = new Page<>(pageNo, pageSize, result.getTotal());
        List<OrderResponse> list = new ArrayList<>();
        for (BizDevelopmentOrder order : result.getRecords()) {
            OrderResponse resp = new OrderResponse();
            BeanUtils.copyProperties(order, resp);
            // 填充网点名称
            if (order.getOutletId() != null) {
                BizOutlet outlet = outletService.getById(order.getOutletId());
                if (outlet != null) {
                    resp.setOutletName(outlet.getOutletName());
                }
            }
            // 填充发展人名称
            if (order.getDeveloperId() != null) {
                BizDeveloper developer = developerMapper.selectById(order.getDeveloperId());
                if (developer != null) {
                    SysUser devUser = userService.getById(developer.getUserId());
                    resp.setDeveloperName(devUser != null ? devUser.getRealName() : "未知");
                }
            }
            // 填充审核日志
            resp.setLeadAuditLog(buildOrderAuditLogList(order.getId(), "LEAD"));
            resp.setFormalAuditLog(buildOrderAuditLogList(order.getId(), "FORMAL"));
            list.add(resp);
        }
        respPage.setRecords(list);
        return respPage;
    }

    @Override
    public OrderResponse getOrderDetail(Long orderId) {
        BizDevelopmentOrder order = this.getById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException("订单不存在");
        }
        OrderResponse resp = new OrderResponse();
        BeanUtils.copyProperties(order, resp);
        // 填充网点名称
        if (order.getOutletId() != null) {
            BizOutlet outlet = outletService.getById(order.getOutletId());
            if (outlet != null) {
                resp.setOutletName(outlet.getOutletName());
            }
        }
        // 填充发展人名称
        if (order.getDeveloperId() != null) {
            BizDeveloper developer = developerMapper.selectById(order.getDeveloperId());
            if (developer != null) {
                SysUser devUser = userService.getById(developer.getUserId());
                resp.setDeveloperName(devUser != null ? devUser.getRealName() : "未知");
            }
        }
        // 填充审核日志
        resp.setLeadAuditLog(buildOrderAuditLogList(orderId, "LEAD"));
        resp.setFormalAuditLog(buildOrderAuditLogList(orderId, "FORMAL"));
        return resp;
    }

    @Override
    @Transactional
    public void leadAudit(Long orderId, OrderAuditRequest request, Long currentUserId, String currentUserRole) {
        BizDevelopmentOrder order = this.getById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException("订单不存在");
        }
        String currentStatus = order.getLeadStatus();
        if ("REJECTED".equals(currentStatus) || "PROVINCE_APPROVED".equals(currentStatus)) {
            throw new BusinessException("意向单已结束，无法再次审核");
        }
        String auditLevel = request.getAuditLevel();
        String targetStatus = request.getStatus();
        boolean isApproved = "APPROVED".equals(targetStatus);

        // 校验审核级别与当前角色匹配
        int levelOrder = getLevelOrder(auditLevel);
        int currentLevel = getLevelOrderByRole(currentUserRole);
        if (levelOrder != currentLevel) {
            throw new BusinessException(403, "您没有权限审核该级别");
        }
        // 校验状态流转合法性
        if (!isValidLeadTransition(currentStatus, auditLevel)) {
            throw new BusinessException("当前状态不允许进行该级别审核");
        }

        String newStatus;
        if (isApproved) {
            if ("OUTLET".equals(auditLevel)) newStatus = "OUTLET_APPROVED";
            else if ("CITY".equals(auditLevel)) newStatus = "CITY_APPROVED";
            else if ("PROVINCE".equals(auditLevel)) newStatus = "PROVINCE_APPROVED";
            else throw new BusinessException("未知审核级别");
            if ("PROVINCE_APPROVED".equals(newStatus)) {
                // 省级通过：生成引流佣金（此处预留，后续集成佣金模块）
                generateCommission(order, "LEAD");
                log.info("意向单省级通过，生成引流佣金，订单ID: {}", orderId);
            }
        } else {
            newStatus = "REJECTED";
        }
        order.setLeadStatus(newStatus);
        order.setLeadAuditorId(currentUserId);
        order.setLeadAuditAt(LocalDateTime.now());
        order.setRemark(request.getAuditRemark());
        this.updateById(order);

        // 记录审核日志
        saveOrderAuditLog(orderId, "LEAD", auditLevel, currentUserId,
                isApproved ? "APPROVED" : "REJECTED", request.getAuditRemark());
    }

    @Override
    @Transactional
    public void formalAudit(Long orderId, OrderAuditRequest request, Long currentUserId, String currentUserRole) {
        BizDevelopmentOrder order = this.getById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException("订单不存在");
        }
        // 要求意向单必须省级通过
        if (!"PROVINCE_APPROVED".equals(order.getLeadStatus())) {
            throw new BusinessException("意向单未完成审核，不能进行转正审核");
        }
        String currentStatus = order.getFormalStatus();
        if ("N/A".equals(currentStatus)) {
            // 首次转正审核，先变为待审
            order.setFormalStatus("PENDING");
            this.updateById(order);
            currentStatus = "PENDING";
        }
        if ("REJECTED".equals(currentStatus) || "PROVINCE_APPROVED".equals(currentStatus)) {
            throw new BusinessException("转正单已结束，无法再次审核");
        }

        String auditLevel = request.getAuditLevel();
        String targetStatus = request.getStatus();
        boolean isApproved = "APPROVED".equals(targetStatus);

        int levelOrder = getLevelOrder(auditLevel);
        int currentLevel = getLevelOrderByRole(currentUserRole);
        if (levelOrder != currentLevel) {
            throw new BusinessException(403, "您没有权限审核该级别");
        }
        if (!isValidFormalTransition(currentStatus, auditLevel)) {
            throw new BusinessException("当前状态不允许进行该级别审核");
        }

        String newStatus;
        if (isApproved) {
            if ("OUTLET".equals(auditLevel)) newStatus = "OUTLET_APPROVED";
            else if ("CITY".equals(auditLevel)) newStatus = "CITY_APPROVED";
            else if ("PROVINCE".equals(auditLevel)) newStatus = "PROVINCE_APPROVED";
            else throw new BusinessException("未知审核级别");
            if ("PROVINCE_APPROVED".equals(newStatus)) {
                // 省级通过：生成转正佣金

                generateCommission(order,"FORMAL");


                log.info("转正单省级通过，生成转正佣金，订单ID: {}", orderId);
            }
        } else {
            newStatus = "REJECTED";
        }
        order.setFormalStatus(newStatus);
        order.setFormalAuditorId(currentUserId);
        order.setFormalAuditAt(LocalDateTime.now());
        order.setRemark(request.getAuditRemark());
        this.updateById(order);

        // 记录审核日志
        saveOrderAuditLog(orderId, "FORMAL", auditLevel, currentUserId,
                isApproved ? "APPROVED" : "REJECTED", request.getAuditRemark());
    }



    /**
     * 生成佣金明细（支持 LEAD 和 FORMAL 阶段）
     * @param order 订单实体
     * @param phase "LEAD" 或 "FORMAL"
     */
    private void generateCommission(BizDevelopmentOrder order, String phase) {
        // 1. 查询匹配的佣金规则
        BizCommissionRule rule = findMatchingRule(order.getBusinessType(),
                order.getDevelopSource(), phase);
        if (rule == null) {
            throw new BusinessException("未找到匹配的" + ("LEAD".equals(phase) ? "引流" : "转正") + "佣金规则，请联系管理员配置");
        }

        // 2. 计算各方金额
        BigDecimal total = rule.getTotalAmount();
        BigDecimal outletAmount = total.multiply(rule.getOutletRatio());
        BigDecimal developerAmount = total.multiply(rule.getDeveloperRatio());
        BigDecimal platformAmount = total.multiply(rule.getPlatformRatio());

        // 3. 构建并插入明细（只插入比例>0的）
        List<BizCommissionDetailb> details = new ArrayList<>();

        // 网点
        if (rule.getOutletRatio().compareTo(BigDecimal.ZERO) > 0) {
            details.add(buildDetail(order, rule, "OUTLET", order.getOutletId(),
                    rule.getOutletRatio(), outletAmount,phase));
        }
        // 发展人（仅当订单有关联发展人且比例>0）
        if (order.getDeveloperId() != null &&
                rule.getDeveloperRatio().compareTo(BigDecimal.ZERO) > 0) {
            details.add(buildDetail(order, rule, "DEVELOPER", order.getDeveloperId(),
                    rule.getDeveloperRatio(), developerAmount,phase ));
        }
        // 平台（比例>0）
        if (rule.getPlatformRatio().compareTo(BigDecimal.ZERO) > 0) {
            // 平台 payee_id 可固定为 0 或从系统配置读取
            details.add(buildDetail(order, rule, "PLATFORM", 0L,
                    rule.getPlatformRatio(), platformAmount,phase ));
        }

        if (details.isEmpty()) {
            log.warn("订单 {} {} 佣金无有效分配比例，规则ID: {}", order.getOrderNo(), phase, rule.getId());
            return;
        }

        // 批量插入
        for (BizCommissionDetailb detail : details) {
            commissionDetailMapper.insert(detail);
        }
    }

    /**
     * 构建单个佣金明细
     */
    private BizCommissionDetailb buildDetail(BizDevelopmentOrder order,
                                             BizCommissionRule rule,
                                             String payeeType,
                                             Long payeeId,
                                             BigDecimal ratio,
                                             BigDecimal amount,
                                             String phase) {
        BizCommissionDetailb detail = new BizCommissionDetailb();
        detail.setOrderId(order.getId());
        detail.setOrderNo(order.getOrderNo());
        detail.setCommissionPhase(phase);   // 动态传入 "LEAD" 或 "FORMAL"
        detail.setPayeeType(payeeType);
        detail.setPayeeId(payeeId);
        detail.setRuleId(rule.getId());
        // 快照字段
        detail.setRuleNameSnapshot(rule.getRuleName());
        detail.setBusinessTypeSnapshot(rule.getBusinessType());
        detail.setDevelopSourceSnapshot(rule.getDevelopSource());
        detail.setTotalAmount(rule.getTotalAmount());
        detail.setRatio(ratio);
        detail.setAmount(amount);
        detail.setStatus("PENDING");
        detail.setRemark(("LEAD".equals(phase) ? "引流" : "转正") + "省级审核通过自动生成");
        return detail;
    }

    /**
     * 查询当前生效的佣金规则（按业务类型、发展来源、阶段）
     */
    private BizCommissionRule findMatchingRule(String businessType, String developSource, String phase) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<BizCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCommissionRule::getBusinessType, businessType)
                .eq(BizCommissionRule::getDevelopSource, developSource)
                .eq(BizCommissionRule::getCommissionPhase, phase)
                .eq(BizCommissionRule::getStatus, 1)          // 启用
                .eq(BizCommissionRule::getIsDeleted, 0)
                .le(BizCommissionRule::getEffectiveDate, today)
                .and(w -> w.isNull(BizCommissionRule::getExpiryDate)
                        .or().ge(BizCommissionRule::getExpiryDate, today))
                .orderByDesc(BizCommissionRule::getEffectiveDate)  // 取最新生效的
                .last("LIMIT 1");
        return commissionRuleService.getOne(wrapper);
    }


    @Override
    @Transactional
    public void rejectOrder(Long orderId, String auditPhase, String rejectReason, Long currentUserId) {
        BizDevelopmentOrder order = this.getById(orderId);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException("订单不存在");
        }
        if ("LEAD".equals(auditPhase)) {
            if ("REJECTED".equals(order.getLeadStatus()) || "PROVINCE_APPROVED".equals(order.getLeadStatus())) {
                throw new BusinessException("意向单已结束");
            }
            order.setLeadStatus("REJECTED");
            order.setLeadAuditorId(currentUserId);
            order.setLeadAuditAt(LocalDateTime.now());
        } else if ("FORMAL".equals(auditPhase)) {
            if ("REJECTED".equals(order.getFormalStatus()) || "PROVINCE_APPROVED".equals(order.getFormalStatus())) {
                throw new BusinessException("转正单已结束");
            }
            order.setFormalStatus("REJECTED");
            order.setFormalAuditorId(currentUserId);
            order.setFormalAuditAt(LocalDateTime.now());
        } else {
            throw new BusinessException("未知审核阶段");
        }
        order.setRemark(rejectReason);
        this.updateById(order);
        // 记录审核日志
        saveOrderAuditLog(orderId, auditPhase, "OUTLET", currentUserId,
                "REJECTED", rejectReason);
    }

    // ---------- 审核日志辅助方法 ----------

    /**
     * 保存订单审核日志
     */
    private void saveOrderAuditLog(Long orderId, String auditPhase, String auditLevel,
                                   Long auditorId, String auditStatus, String auditRemark) {
        SysUser auditor = userService.getById(auditorId);
        String auditorName = auditor != null ? auditor.getRealName() : "未知";

        BizAuditLog log = new BizAuditLog();
        log.setTargetType("ORDER");
        log.setTargetId(orderId);
        log.setAuditLevel(auditLevel);
        log.setAuditPhase(auditPhase);
        log.setAuditorId(auditorId);
        log.setAuditorName(auditorName);
        log.setAuditStatus(auditStatus);
        log.setAuditRemark(auditRemark);
        log.setAuditTime(LocalDateTime.now());
        auditLogMapper.insert(log);
    }

    /**
     * 构建订单审核日志列表
     */
    private List<OrderResponse.AuditLog> buildOrderAuditLogList(Long orderId, String phase) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BizAuditLog> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(BizAuditLog::getTargetType, "ORDER")
                .eq(BizAuditLog::getTargetId, orderId)
                .eq(BizAuditLog::getAuditPhase, phase)
                .orderByAsc(BizAuditLog::getCreatedAt);
        List<BizAuditLog> logs = auditLogMapper.selectList(wrapper);

        // 构建预定义的三级审核状态
        String[] levels = {"OUTLET", "CITY", "PROVINCE"};
        List<OrderResponse.AuditLog> result = new ArrayList<>();
        for (String level : levels) {
            OrderResponse.AuditLog al = new OrderResponse.AuditLog();
            al.setLevel(level);
            al.setAuditorName(null);
            al.setAuditStatus("PENDING");
            al.setAuditTime(null);
            al.setAuditRemark(null);
            for (BizAuditLog log : logs) {
                if (level.equals(log.getAuditLevel())) {
                    al.setAuditorName(log.getAuditorName());
                    al.setAuditStatus(log.getAuditStatus());
                    al.setAuditTime(log.getAuditTime());
                    al.setAuditRemark(log.getAuditRemark());
                    break;
                }
            }
            result.add(al);
        }
        return result;
    }

    // ---------- 辅助方法 ----------
    private int getLevelOrder(String level) {
        if ("OUTLET".equals(level)) return 1;
        if ("CITY".equals(level)) return 2;
        if ("PROVINCE".equals(level)) return 3;
        throw new BusinessException("未知审核级别");
    }

    private int getLevelOrderByRole(String role) {
        if ("ROLE_OUTLET".equals(role)) return 1;
        if ("ROLE_CITY".equals(role)) return 2;
        if ("ROLE_PROVINCE".equals(role)) return 3;
        throw new BusinessException("角色无法审核");
    }

    private boolean isValidLeadTransition(String currentStatus, String auditLevel) {
        if ("PENDING".equals(currentStatus) && "OUTLET".equals(auditLevel)) return true;
        if ("OUTLET_APPROVED".equals(currentStatus) && "CITY".equals(auditLevel)) return true;
        if ("CITY_APPROVED".equals(currentStatus) && "PROVINCE".equals(auditLevel)) return true;
        return false;
    }

    private boolean isValidFormalTransition(String currentStatus, String auditLevel) {
        if ("PENDING".equals(currentStatus) && "OUTLET".equals(auditLevel)) return true;
        if ("OUTLET_APPROVED".equals(currentStatus) && "CITY".equals(auditLevel)) return true;
        if ("CITY_APPROVED".equals(currentStatus) && "PROVINCE".equals(auditLevel)) return true;
        return false;




    }
}