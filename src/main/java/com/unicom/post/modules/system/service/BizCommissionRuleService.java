package com.unicom.post.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.system.domain.entity.BizCommissionRule;
import com.unicom.post.modules.system.dto.CommissionRuleCreateRequest;
import com.unicom.post.modules.system.dto.CommissionRuleUpdateRequest;

public interface BizCommissionRuleService extends IService<BizCommissionRule> {
    BizCommissionRule createRule(CommissionRuleCreateRequest request);
    BizCommissionRule updateRule(Long id, CommissionRuleUpdateRequest request);
    void disableRule(Long id);


    void enableRule(Long id);
}