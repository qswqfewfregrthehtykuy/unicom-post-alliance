package com.unicom.post.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.modules.system.domain.entity.BizCommissionRule;
import com.unicom.post.modules.system.dto.CommissionRuleCreateRequest;
import com.unicom.post.modules.system.dto.CommissionRuleUpdateRequest;
import com.unicom.post.modules.system.mapper.BizCommissionRuleMapper;
import com.unicom.post.modules.system.service.BizCommissionRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class BizCommissionRuleServiceImpl
        extends ServiceImpl<BizCommissionRuleMapper, BizCommissionRule>
        implements BizCommissionRuleService {

    @Override
    @Transactional
    public BizCommissionRule createRule(CommissionRuleCreateRequest request) {
        // 1. 校验比例之和是否为1
        BigDecimal sum = request.getOutletRatio()
                .add(request.getDeveloperRatio())
                .add(request.getPlatformRatio());
        if (sum.compareTo(BigDecimal.ONE) != 0) {
            throw new BusinessException(400,
                    "比例之和必须等于1.0000，当前和为" + sum.setScale(4, BigDecimal.ROUND_HALF_UP));
        }

        // 2. 检查是否重复
        LambdaQueryWrapper<BizCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCommissionRule::getBusinessType, request.getBusinessType())
                .eq(BizCommissionRule::getDevelopSource, request.getDevelopSource())
                .eq(BizCommissionRule::getCommissionPhase, request.getCommissionPhase())
                .eq(BizCommissionRule::getEffectiveDate, request.getEffectiveDate());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400,
                    "该规则已存在（" + request.getBusinessType() + " + "
                            + request.getDevelopSource() + " + "
                            + request.getCommissionPhase() + " + "
                            + request.getEffectiveDate() + "）");
        }

        // 3. 构建实体
        BizCommissionRule rule = new BizCommissionRule();
        rule.setRuleName(request.getRuleName());
        rule.setBusinessType(request.getBusinessType());
        rule.setDevelopSource(request.getDevelopSource());
        rule.setCommissionPhase(request.getCommissionPhase());
        rule.setTotalAmount(request.getTotalAmount());
        rule.setOutletRatio(request.getOutletRatio());
        rule.setDeveloperRatio(request.getDeveloperRatio());
        rule.setPlatformRatio(request.getPlatformRatio());
        rule.setEffectiveDate(request.getEffectiveDate());
        rule.setExpiryDate(request.getExpiryDate());
        rule.setRemark(request.getRemark());
        rule.setStatus(1); // 默认启用

        this.save(rule);
        return rule;
    }

    @Override
    @Transactional
    public BizCommissionRule updateRule(Long id, CommissionRuleUpdateRequest request) {
        BizCommissionRule rule = this.getById(id);
        if (rule == null) {
            throw new BusinessException(404, "规则不存在");
        }

        // 已启用的规则不允许修改
        if (rule.getStatus() == 1) {
            throw new BusinessException(400, "生效规则不允许修改，请新建规则");
        }

        // 校验比例（如果有更新比例字段）
        BigDecimal outlet = request.getOutletRatio();
        BigDecimal developer = request.getDeveloperRatio();
        BigDecimal platform = request.getPlatformRatio();
        if (outlet != null && developer != null && platform != null) {
            BigDecimal sum = outlet.add(developer).add(platform);
            if (sum.compareTo(BigDecimal.ONE) != 0) {
                throw new BusinessException(400,
                        "比例之和必须等于1.0000，当前和为" + sum.setScale(4, BigDecimal.ROUND_HALF_UP));
            }
        } else if (outlet != null || developer != null || platform != null) {
            // 如果只传了部分比例，则用已有值补充，再校验
            BigDecimal finalOutlet = outlet != null ? outlet : rule.getOutletRatio();
            BigDecimal finalDeveloper = developer != null ? developer : rule.getDeveloperRatio();
            BigDecimal finalPlatform = platform != null ? platform : rule.getPlatformRatio();
            BigDecimal sum = finalOutlet.add(finalDeveloper).add(finalPlatform);
            if (sum.compareTo(BigDecimal.ONE) != 0) {
                throw new BusinessException(400,
                        "比例之和必须等于1.0000，当前和为" + sum.setScale(4, BigDecimal.ROUND_HALF_UP));
            }
        }

        // 更新字段
        if (request.getRuleName() != null) rule.setRuleName(request.getRuleName());
        if (request.getTotalAmount() != null) rule.setTotalAmount(request.getTotalAmount());
        if (request.getOutletRatio() != null) rule.setOutletRatio(request.getOutletRatio());
        if (request.getDeveloperRatio() != null) rule.setDeveloperRatio(request.getDeveloperRatio());
        if (request.getPlatformRatio() != null) rule.setPlatformRatio(request.getPlatformRatio());
        if (request.getRemark() != null) rule.setRemark(request.getRemark());

        this.updateById(rule);
        return rule;
    }

    @Override
    @Transactional
    public void disableRule(Long id) {
        BizCommissionRule rule = this.getById(id);
        if (rule == null) {
            throw new BusinessException(404, "规则不存在");
        }
        // 软删除 + 状态禁用
        rule.setIsDeleted(1);
        rule.setStatus(0);
        this.updateById(rule);
    }


    @Override
    @Transactional
    public void enableRule(Long id) {
        // 1. 查询规则
        BizCommissionRule rule = this.getById(id);
        if (rule == null) {
            throw new BusinessException(404, "规则不存在");
        }

        // 2. 可选：检查是否已启用
        if (rule.getStatus() == 1 && rule.getIsDeleted() == 0) {
            throw new BusinessException(400, "该规则已是启用状态");
        }

        // 3. 可选：检查过期日期
        if (rule.getExpiryDate() != null && rule.getExpiryDate().isBefore(LocalDate.now())) {
            throw new BusinessException(400, "该规则已过期，无法启用");
        }

        // 4. 可选：检查是否存在相同业务类型+发展来源+佣金阶段+生效日期的其他启用规则（避免重复启用）
        LambdaQueryWrapper<BizCommissionRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizCommissionRule::getBusinessType, rule.getBusinessType())
                .eq(BizCommissionRule::getDevelopSource, rule.getDevelopSource())
                .eq(BizCommissionRule::getCommissionPhase, rule.getCommissionPhase())
                .eq(BizCommissionRule::getEffectiveDate, rule.getEffectiveDate())
                .eq(BizCommissionRule::getStatus, 1)
                .eq(BizCommissionRule::getIsDeleted, 0)
                .ne(BizCommissionRule::getId, id); // 排除自身
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "已存在相同维度的启用规则，请先停用旧规则");
        }

        // 5. 启用：恢复软删除，设置状态为启用
        rule.setIsDeleted(0);
        rule.setStatus(1);
        this.updateById(rule);
    }
}