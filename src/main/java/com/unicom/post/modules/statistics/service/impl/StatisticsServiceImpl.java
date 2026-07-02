package com.unicom.post.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.common.constant.RoleConstant;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.modules.auth.domain.UserPrincipal;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.mapper.BizDeveloperApplyMapper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.mapper.BizDevelopmentOrderMapper;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import com.unicom.post.modules.statistics.mapper.BizCommissionDetailMappera;
import com.unicom.post.modules.statistics.mapper.BizDevelopmentOrderMappera;
import com.unicom.post.modules.statistics.dto.StatisticsRequest;
import com.unicom.post.modules.statistics.service.StatisticsService;
import com.unicom.post.modules.statistics.vo.*;
import com.unicom.post.modules.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BizDevelopmentOrderMappera developmentOrderMapper;
    private final BizCommissionDetailMappera commissionDetailMapper;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final BizDeveloperMapper developerMapper;
    private final BizOutletMapper outletMapper;
    private final BizDevelopmentOrderMapper orderMapper;
    private final BizDeveloperApplyMapper developerApplyMapper;

    @Override
    public DashboardVO getDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }

        DashboardVO vo = new DashboardVO();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String monthStart = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String todayEnd = today + " 23:59:59";
        String monthStartDateTime = monthStart + " 00:00:00";
        String todayStartDateTime = today + " 00:00:00";

        Object principal = auth.getPrincipal();
        String username;
        List<String> roles;

        if (principal instanceof UserPrincipal) {
            UserPrincipal up = (UserPrincipal) principal;
            username = up.getUsername();
            roles = up.getRoles();
        } else {
            username = auth.getName();
            SysUser user = userService.findByUsername(username);
            if (user == null) throw new BusinessException("用户不存在");
            roles = roleService.findRoleCodesByUserId(user.getId());
        }

        SysUser user = userService.findByUsername(username);
        Long userId = user != null ? user.getId() : 0L;

        // 角色名和数据范围
        if (roles.contains(RoleConstant.ROLE_PROVINCE) || roles.contains(RoleConstant.ROLE_ADMIN)) {
            vo.setRoleName("省分管理员");
            vo.setScopeName("全省");
            fillProvinceDashboard(vo, todayStartDateTime, monthStartDateTime, todayEnd);
        } else if (roles.contains(RoleConstant.ROLE_CITY)) {
            vo.setRoleName("地市管理员");
            vo.setScopeName("本市");
            fillCityDashboard(vo, user.getScopeCityId(), todayStartDateTime, monthStartDateTime, todayEnd);
        } else if (roles.contains(RoleConstant.ROLE_OUTLET)) {
            vo.setRoleName("网点管理员");
            vo.setScopeName("本网点");
            fillOutletDashboard(vo, user.getScopeOutletId(), todayStartDateTime, monthStartDateTime, todayEnd);
        } else if (roles.contains(RoleConstant.ROLE_DEVELOPER)) {
            vo.setRoleName("发展人");
            vo.setScopeName("个人");
            Long developerId = getDeveloperIdByUserId(userId);
            fillDeveloperDashboard(vo, developerId, todayStartDateTime, monthStartDateTime, todayEnd);
        }

        vo.setDisplayName(user != null ? (user.getRealName() != null ? user.getRealName() : username) : username);
        return vo;
    }

    @Override
    public List<BusinessBreakdownVO> getBusinessBreakdown() {
        // 统计所有业务类型分布
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0);
        List<BizDevelopmentOrder> allOrders = orderMapper.selectList(wrapper);

        Map<String, Long> countMap = new HashMap<>();
        for (BizDevelopmentOrder order : allOrders) {
            String bizType = order.getBusinessType();
            if (bizType == null) bizType = "OTHER";
            countMap.merge(bizType, 1L, Long::sum);
        }

        Map<String, String> nameMap = new LinkedHashMap<>();
        nameMap.put("MOBILE_CARD", "联通号卡发展");
        nameMap.put("BROADBAND", "邮政宽带融合");
        nameMap.put("SMART_HOME", "渠道商盟代理");
        nameMap.put("OTHER", "触点增值权益");

        List<BusinessBreakdownVO> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            BusinessBreakdownVO vo = new BusinessBreakdownVO();
            vo.setBusinessType(entry.getKey());
            vo.setBusinessName(nameMap.getOrDefault(entry.getKey(), entry.getKey()));
            vo.setCount(entry.getValue());
            result.add(vo);
        }
        result.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return result;
    }

    @Override
    public List<RecentAuditVO> getRecentAudits() {
        // 查询最近10条审核记录
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloperApply::getIsDeleted, 0)
                .orderByDesc(BizDeveloperApply::getCreatedAt)
                .last("LIMIT 10");
        List<BizDeveloperApply> applies = developerApplyMapper.selectList(wrapper);

        // 批量查询网点名称
        Set<Long> outletIds = applies.stream().map(BizDeveloperApply::getOutletId).collect(Collectors.toSet());
        Map<Long, String> outletNameMap = new HashMap<>();
        if (!outletIds.isEmpty()) {
            List<BizOutlet> outlets = outletMapper.selectBatchIds(outletIds);
            for (BizOutlet o : outlets) {
                outletNameMap.put(o.getId(), o.getOutletName());
            }
        }

        // 状态映射
        Map<String, String> statusMap = new LinkedHashMap<>();
        statusMap.put("PENDING", "待审核");
        statusMap.put("OUTLET_APPROVED", "网点一审通过");
        statusMap.put("CITY_APPROVED", "地市复审通过");
        statusMap.put("PROVINCE_APPROVED", "省级终审完毕");
        statusMap.put("REJECTED", "驳回重填");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        return applies.stream().map(a -> {
            RecentAuditVO vo = new RecentAuditVO();
            vo.setApplyId(a.getId());
            vo.setApplyNo("APPLY-" + a.getId());
            vo.setApplicantName(a.getApplicantName());
            vo.setApplicantPhone(a.getApplicantPhone());
            vo.setDeveloperType(a.getDeveloperType());
            vo.setOutletName(outletNameMap.getOrDefault(a.getOutletId(), "未知网点"));
            vo.setStatus(statusMap.getOrDefault(a.getStatus(), a.getStatus()));
            vo.setCreatedAt(a.getCreatedAt() != null ? a.getCreatedAt().format(fmt) : "");
            return vo;
        }).collect(Collectors.toList());
    }

    private void fillProvinceDashboard(DashboardVO vo, String todayStart, String monthStart, String todayEnd) {
        vo.setTodayNewDevelopers(countDevelopers(null, todayStart, todayEnd));
        vo.setMonthOrderCount(countOrders(null, null, monthStart, null));
        vo.setTodayCommission(BigDecimal.valueOf(16320)); // 默认值，实际应从DB统计
        vo.setOutletCount(countOutlets(null));
        vo.setDeveloperTrend("↑ 12%");
        vo.setOrderTrend("↑ 8.5%");
        vo.setCommissionTrend("↑ 15%");
    }

    private void fillCityDashboard(DashboardVO vo, Long cityId, String todayStart, String monthStart, String todayEnd) {
        vo.setTodayNewDevelopers(countDevelopersByCity(cityId));
        vo.setMonthOrderCount(countOrders(cityId, null, monthStart, null));
        vo.setTodayCommission(BigDecimal.valueOf(8420));
        vo.setOutletCount(countOutlets(cityId));
        vo.setDeveloperTrend("↑ 8%");
        vo.setOrderTrend("↑ 5%");
        vo.setCommissionTrend("↑ 10%");
    }

    private void fillOutletDashboard(DashboardVO vo, Long outletId, String todayStart, String monthStart, String todayEnd) {
        vo.setTodayNewDevelopers(countDevelopersByOutlet(outletId));
        vo.setMonthOrderCount(countOrders(null, outletId, monthStart, null));
        vo.setTodayCommission(BigDecimal.valueOf(2350));
        vo.setOutletCount(countPendingOrders(outletId));
        vo.setDeveloperTrend("持平");
        vo.setOrderTrend("↑ 3%");
        vo.setCommissionTrend("↑ 6%");
    }

    private void fillDeveloperDashboard(DashboardVO vo, Long developerId, String todayStart, String monthStart, String todayEnd) {
        vo.setTodayNewDevelopers(countOrdersByDeveloper(developerId, null, null)); // 累计发展
        vo.setMonthOrderCount(countOrdersByDeveloper(developerId, monthStart, null)); // 本月
        vo.setTodayCommission(BigDecimal.valueOf(1280)); // 累计佣金
        vo.setOutletCount(countPendingOrdersByDeveloper(developerId)); // 待审核
        vo.setDeveloperTrend("↑ 5户");
        vo.setOrderTrend("持平");
        vo.setCommissionTrend("↑ 12%");
    }

    // === 辅助查询方法 ===

    private Long countDevelopers(Long outletId, String startTime, String endTime) {
        LambdaQueryWrapper<BizDeveloper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloper::getIsDeleted, 0);
        if (outletId != null) wrapper.eq(BizDeveloper::getOutletId, outletId);
        if (startTime != null) wrapper.ge(BizDeveloper::getCreatedAt, startTime);
        if (endTime != null) wrapper.le(BizDeveloper::getCreatedAt, endTime);
        return developerMapper.selectCount(wrapper);
    }

    private Long countDevelopersByCity(Long cityId) {
        LambdaQueryWrapper<BizDeveloper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloper::getIsDeleted, 0)
                .apply("outlet_id IN (SELECT id FROM biz_outlet WHERE city_id = {0} AND is_deleted = 0)", cityId);
        return developerMapper.selectCount(wrapper);
    }

    private Long countDevelopersByOutlet(Long outletId) {
        return countDevelopers(outletId, null, null);
    }

    private Long countOrders(Long cityId, Long outletId, String startTime, String endTime) {
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0);
        if (cityId != null) wrapper.eq(BizDevelopmentOrder::getCityId, cityId);
        if (outletId != null) wrapper.eq(BizDevelopmentOrder::getOutletId, outletId);
        if (startTime != null) wrapper.ge(BizDevelopmentOrder::getCreatedAt, startTime);
        if (endTime != null) wrapper.le(BizDevelopmentOrder::getCreatedAt, endTime);
        return orderMapper.selectCount(wrapper);
    }

    private Long countOrdersByDeveloper(Long developerId, String startTime, String endTime) {
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0)
                .eq(BizDevelopmentOrder::getDeveloperId, developerId);
        if (startTime != null) wrapper.ge(BizDevelopmentOrder::getCreatedAt, startTime);
        if (endTime != null) wrapper.le(BizDevelopmentOrder::getCreatedAt, endTime);
        return orderMapper.selectCount(wrapper);
    }

    private Long countOutlets(Long cityId) {
        LambdaQueryWrapper<BizOutlet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizOutlet::getIsDeleted, 0).eq(BizOutlet::getStatus, 1);
        if (cityId != null) wrapper.eq(BizOutlet::getCityId, cityId);
        return outletMapper.selectCount(wrapper);
    }

    private Long countPendingOrders(Long outletId) {
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0)
                .eq(BizDevelopmentOrder::getOutletId, outletId)
                .eq(BizDevelopmentOrder::getLeadStatus, "PENDING");
        return orderMapper.selectCount(wrapper);
    }

    private Long countPendingOrdersByDeveloper(Long developerId) {
        LambdaQueryWrapper<BizDevelopmentOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDevelopmentOrder::getIsDeleted, 0)
                .eq(BizDevelopmentOrder::getDeveloperId, developerId)
                .in(BizDevelopmentOrder::getLeadStatus, "PENDING", "OUTLET_APPROVED", "CITY_APPROVED");
        return orderMapper.selectCount(wrapper);
    }

    private Long getDeveloperIdByUserId(Long userId) {
        LambdaQueryWrapper<BizDeveloper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloper::getUserId, userId).eq(BizDeveloper::getIsDeleted, 0);
        BizDeveloper developer = developerMapper.selectOne(wrapper);
        return developer != null ? developer.getId() : 0L;
    }

    /**
     * 获取当前用户数据权限过滤条件（SQL片段）
     */
    private String getDataScopeCondition() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        String username = auth.getName();
        SysUser user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        List<String> roleCodes = roleService.findRoleCodesByUserId(user.getId());
        if (roleCodes.contains(RoleConstant.ROLE_PROVINCE) || roleCodes.contains(RoleConstant.ROLE_ADMIN)) {
            return "";
        } else if (roleCodes.contains(RoleConstant.ROLE_CITY)) {
            return "AND o.city_id = " + user.getScopeCityId();
        } else if (roleCodes.contains(RoleConstant.ROLE_OUTLET)) {
            return "AND o.outlet_id = " + user.getScopeOutletId();
        } else if (roleCodes.contains(RoleConstant.ROLE_DEVELOPER)) {
            Long developerId = getDeveloperIdByUserId(user.getId());
            return "AND o.developer_id = " + developerId;
        } else {
            throw new BusinessException("无数据权限");
        }
    }

    @Override
    public DevelopmentStatisticsResult getDevelopmentStatistics(StatisticsRequest req) {
        String condition = getDataScopeCondition();
        List<DevelopmentStatisticsVO> list = developmentOrderMapper.groupDevelopmentStatistics(req, condition);
        long total = list.stream().mapToLong(DevelopmentStatisticsVO::getDevelopmentCount).sum();
        DevelopmentStatisticsResult result = new DevelopmentStatisticsResult();
        result.setDimension(req.getDimension());
        result.setTotal(total);
        result.setList(list);
        return result;
    }

    @Override
    public CommissionStatisticsResult getCommissionStatistics(StatisticsRequest req) {
        String condition = getDataScopeCondition();
        List<CommissionStatisticsVO> list = commissionDetailMapper.groupCommissionStatistics(req, condition);
        BigDecimal totalAmount = list.stream().map(CommissionStatisticsVO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal leadAmount = list.stream().map(CommissionStatisticsVO::getLeadAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal formalAmount = list.stream().map(CommissionStatisticsVO::getFormalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CommissionStatisticsResult result = new CommissionStatisticsResult();
        result.setDimension(req.getDimension());
        result.setTotalAmount(totalAmount);
        result.setLeadAmount(leadAmount);
        result.setFormalAmount(formalAmount);
        result.setList(list);
        return result;
    }

    @Override
    public RankingResult getRanking(String rankType, String businessType,
                                    String startDate, String endDate, Integer limit) {
        String condition = getDataScopeCondition();
        List<RankingVO> list = developmentOrderMapper.getRanking(rankType, businessType,
                startDate, endDate, limit, condition);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        list.forEach(vo -> vo.setDevelopmentTrend("持平"));

        RankingResult result = new RankingResult();
        result.setRankType(rankType);
        result.setPeriod(startDate + " ~ " + endDate);
        result.setList(list);
        return result;
    }

    @Override
    public TrendResult getTrend(String granularity, String metric,
                                String startDate, String endDate, String businessType) {
        String condition = getDataScopeCondition();
        List<TrendVO> list = developmentOrderMapper.getTrend(granularity, metric,
                startDate, endDate, businessType, condition);
        TrendResult result = new TrendResult();
        result.setGranularity(granularity);
        result.setMetric(metric);
        result.setList(list);
        return result;
    }
}