package com.unicom.post.modules.statistics.service.impl;

import com.unicom.post.common.constant.RoleConstant;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final BizDevelopmentOrderMappera developmentOrderMapper;
    private final BizCommissionDetailMappera commissionDetailMapper;
    private final SysUserService userService;
    private final SysRoleService roleService;

    /**
     * 获取当前用户数据权限过滤条件（SQL片段）
     */
    private String getDataScopeCondition() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        String username = auth.getName();
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        List<String> roleCodes = roleService.findRoleCodesByUserId(user.getId());
        if (roleCodes.contains(RoleConstant.ROLE_PROVINCE) || roleCodes.contains(RoleConstant.ROLE_ADMIN)) {
            return ""; // 全省
        } else if (roleCodes.contains(RoleConstant.ROLE_CITY)) {
            return "AND o.city_id = " + user.getScopeCityId();
        } else if (roleCodes.contains(RoleConstant.ROLE_OUTLET)) {
            return "AND o.outlet_id = " + user.getScopeOutletId();
        } else if (roleCodes.contains(RoleConstant.ROLE_DEVELOPER)) {
            // 发展人只能看自己的订单（通过 developer_id 或 user_id 关联）
            // 需要获取 developer_id，假设通过 user_id 关联 biz_developer
            Long developerId = getDeveloperIdByUserId(user.getId());
            return "AND o.developer_id = " + developerId;
        } else {
            throw new BusinessException("无数据权限");
        }
    }

    private Long getDeveloperIdByUserId(Long userId) {
        // 查询 biz_developer 表获取 developer_id
        // 此处简化，实际需注入 DeveloperMapper
        // 假设存在 developerMapper.selectOne
        return 1L; // 占位
    }

    @Override
    public DevelopmentStatisticsResult getDevelopmentStatistics(StatisticsRequest req) {
        String condition = getDataScopeCondition();
        List<DevelopmentStatisticsVO> list = developmentOrderMapper.groupDevelopmentStatistics(req, condition);
        // 计算 total（分组总数）
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
        // 汇总总计
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
        // 填充 rank 序号
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setRank(i + 1);
        }
        // 计算环比趋势（略，可查询上一周期数据对比）
        // 此处留空或简单填充
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