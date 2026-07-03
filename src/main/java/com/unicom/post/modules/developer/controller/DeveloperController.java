package com.unicom.post.modules.developer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.UserPrincipal;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.developer.vo.DeveloperProfileVO;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.mapper.BizDevelopmentOrderMapper;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/developer")
public class DeveloperController {

    private final BizDeveloperMapper developerMapper;
    private final BizOutletMapper outletMapper;
    private final BizDevelopmentOrderMapper orderMapper;

    public DeveloperController(BizDeveloperMapper developerMapper,
                               BizOutletMapper outletMapper,
                               BizDevelopmentOrderMapper orderMapper) {
        this.developerMapper = developerMapper;
        this.outletMapper = outletMapper;
        this.orderMapper = orderMapper;
    }

    /**
     * 获取当前发展人的个人资料（Profile 页面使用）
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('DEVELOPER')")
    public Result<DeveloperProfileVO> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId;
        if (auth.getPrincipal() instanceof UserPrincipal) {
            userId = ((UserPrincipal) auth.getPrincipal()).getId();
        } else {
            return Result.error(401, "无法获取用户信息");
        }

        // 查询发展人记录
        BizDeveloper developer = developerMapper.selectOne(
                new LambdaQueryWrapper<BizDeveloper>()
                        .eq(BizDeveloper::getUserId, userId)
                        .eq(BizDeveloper::getIsDeleted, 0)
        );
        if (developer == null) {
            return Result.error(404, "未找到发展人信息");
        }

        // 查询所属网点
        BizOutlet outlet = outletMapper.selectById(developer.getOutletId());

        // 统计发展数据
        Long totalDeveloped = orderMapper.selectCount(
                new LambdaQueryWrapper<BizDevelopmentOrder>()
                        .eq(BizDevelopmentOrder::getDeveloperId, developer.getId())
                        .eq(BizDevelopmentOrder::getIsDeleted, 0)
        );

        // 统计累计佣金（从 biz_commission_detail 查询，此处简化）
        BigDecimal totalCommission = BigDecimal.ZERO;

        DeveloperProfileVO vo = new DeveloperProfileVO();
        vo.setDeveloperId(developer.getId());
        vo.setOutletId(developer.getOutletId());
        vo.setOutletName(outlet != null ? outlet.getOutletName() : "未知网点");
        vo.setDeveloperType(developer.getDeveloperType());
        vo.setShopName(developer.getShopName());
        vo.setLevel("FREE_SHOP".equals(developer.getDeveloperType()) ? "社会加盟商" : "自营网点人员");
        vo.setTotalDeveloped(totalDeveloped);
        vo.setTotalCommission(totalCommission);
        vo.setJoinDate(developer.getJoinDate() != null ? developer.getJoinDate().toString() : "");

        return Result.success(vo);
    }
}