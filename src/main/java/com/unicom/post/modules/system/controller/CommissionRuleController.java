package com.unicom.post.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicom.post.common.result.PageResult;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.IpUtils;
import com.unicom.post.modules.system.domain.entity.BizCommissionRule;
import com.unicom.post.modules.system.dto.CommissionRuleCreateRequest;
import com.unicom.post.modules.system.dto.CommissionRuleUpdateRequest;
import com.unicom.post.modules.system.service.BizCommissionRuleService;
import com.unicom.post.modules.system.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/commission-rules")
public class CommissionRuleController {

    private final BizCommissionRuleService ruleService;
    private final SysOperationLogService operationLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommissionRuleController(BizCommissionRuleService ruleService,
                                     @Qualifier("operationLogService") SysOperationLogService operationLogService) {
        this.ruleService = ruleService;
        this.operationLogService = operationLogService;
    }

    /**
     * 6.1 查询规则列表
     */
    @GetMapping
    public Result<PageResult<BizCommissionRule>> listRules(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String developSource,
            @RequestParam(required = false) String commissionPhase,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        LambdaQueryWrapper<BizCommissionRule> wrapper = new LambdaQueryWrapper<>();
        if (businessType != null) {
            wrapper.eq(BizCommissionRule::getBusinessType, businessType);
        }
        if (developSource != null) {
            wrapper.eq(BizCommissionRule::getDevelopSource, developSource);
        }
        if (commissionPhase != null) {
            wrapper.eq(BizCommissionRule::getCommissionPhase, commissionPhase);
        }
        if (status != null) {
            wrapper.eq(BizCommissionRule::getStatus, status);
        }
        wrapper.orderByDesc(BizCommissionRule::getCreatedAt);

        Page<BizCommissionRule> page = new Page<>(pageNo, pageSize);
        Page<BizCommissionRule> result = ruleService.page(page, wrapper);

        PageResult<BizCommissionRule> pageResult = new PageResult<>(
                result.getTotal(),
                (int) result.getCurrent(), // 直接强转（非空场景）
                (int) result.getSize(),
                result.getRecords()
        );
        return Result.success(pageResult);
    }

    /**
     * 6.2 查看规则详情
     */
    @GetMapping("/{ruleId}")
    public Result<BizCommissionRule> getRuleDetail(@PathVariable Long ruleId) {
        BizCommissionRule rule = ruleService.getById(ruleId);
        if (rule == null) {
            return Result.error(404, "规则不存在");
        }
        return Result.success(rule);
    }

    /**
     * 6.3 新建佣金规则
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<Map<String, Object>> createRule(@Valid @RequestBody CommissionRuleCreateRequest request,
                                                   HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        try {
            BizCommissionRule rule = ruleService.createRule(request);
            Map<String, Object> data = new HashMap<>();
            data.put("id", rule.getId());
            data.put("ruleName", rule.getRuleName());
            data.put("createdAt", rule.getCreatedAt());

            operationLogService.logDetail("COMMISSION_RULE", "创建佣金规则", rule.getId(), "CommissionRule",
                    "创建成功: " + rule.getRuleName(), null, toJson(rule), ip, "SUCCESS", null);
            return Result.success("规则创建成功", data);
        } catch (Exception e) {
            operationLogService.log("COMMISSION_RULE", "创建佣金规则", null,
                    "创建失败", ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 6.4 编辑佣金规则
     */
    @PutMapping("/{ruleId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> updateRule(@PathVariable Long ruleId,
                                      @Valid @RequestBody CommissionRuleUpdateRequest request,
                                      HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizCommissionRule before = ruleService.getById(ruleId);
        String beforeData = toJson(before);
        try {
            ruleService.updateRule(ruleId, request);
            BizCommissionRule after = ruleService.getById(ruleId);
            operationLogService.logDetail("COMMISSION_RULE", "编辑佣金规则", ruleId, "CommissionRule",
                    "编辑成功", beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("规则修改成功");
        } catch (Exception e) {
            operationLogService.logDetail("COMMISSION_RULE", "编辑佣金规则", ruleId, "CommissionRule",
                    "编辑失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }

    /**
     * 6.5 停用规则
     */
    @DeleteMapping("/{ruleId}")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> disableRule(@PathVariable Long ruleId,
                                       HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizCommissionRule before = ruleService.getById(ruleId);
        String beforeData = toJson(before);
        try {
            ruleService.disableRule(ruleId);
            operationLogService.logDetail("COMMISSION_RULE", "停用规则", ruleId, "CommissionRule",
                    "停用成功", beforeData, null, ip, "SUCCESS", null);
            return Result.success("规则已停用");
        } catch (Exception e) {
            operationLogService.logDetail("COMMISSION_RULE", "停用规则", ruleId, "CommissionRule",
                    "停用失败", beforeData, null, ip, "FAIL", e.getMessage());
            throw e;
        }
    }


    /**
     * 启用规则
     */
    @PutMapping("/{ruleId}/enable")
    @PreAuthorize("hasRole('PROVINCE')")
    public Result<String> enableRule(@PathVariable Long ruleId,
                                      HttpServletRequest httpRequest) {
        String ip = IpUtils.getClientIp(httpRequest);
        BizCommissionRule before = ruleService.getById(ruleId);
        String beforeData = toJson(before);
        try {
            ruleService.enableRule(ruleId);
            BizCommissionRule after = ruleService.getById(ruleId);
            operationLogService.logDetail("COMMISSION_RULE", "启用规则", ruleId, "CommissionRule",
                    "启用成功", beforeData, toJson(after), ip, "SUCCESS", null);
            return Result.success("规则已启用");
        } catch (Exception e) {
            operationLogService.logDetail("COMMISSION_RULE", "启用规则", ruleId, "CommissionRule",
                    "启用失败", beforeData, null, ip, "FAIL", e.getMessage());
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