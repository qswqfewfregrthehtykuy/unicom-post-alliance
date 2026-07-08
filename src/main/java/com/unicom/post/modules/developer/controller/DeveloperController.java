package com.unicom.post.modules.developer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unicom.post.common.result.Result;
import com.unicom.post.common.utils.SecurityUtils;
import com.unicom.post.modules.auth.domain.UserPrincipal;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.developer.vo.DeveloperProfileVO;
import com.unicom.post.modules.order.domain.entity.BizCommissionDetailb;
import com.unicom.post.modules.order.domain.entity.BizDevelopmentOrder;
import com.unicom.post.modules.order.mapper.BizCommissionDetailMapper;
import com.unicom.post.modules.order.mapper.BizDevelopmentOrderMapper;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.mapper.BizOutletMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/developer")
public class DeveloperController {

    private final BizDeveloperMapper developerMapper;
    private final BizOutletMapper outletMapper;
    private final BizDevelopmentOrderMapper orderMapper;
    private final BizCommissionDetailMapper commissionDetailMapper;
    private final SysUserService userService;

    public DeveloperController(BizDeveloperMapper developerMapper,
                               BizOutletMapper outletMapper,
                               BizDevelopmentOrderMapper orderMapper,
                               BizCommissionDetailMapper commissionDetailMapper,
                               SysUserService userService) {
        this.developerMapper = developerMapper;
        this.outletMapper = outletMapper;
        this.orderMapper = orderMapper;
        this.commissionDetailMapper = commissionDetailMapper;
        this.userService = userService;
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

        // 统计累计佣金（从 biz_commission_detail 查询发展人的佣金记录）
        BigDecimal totalCommission = commissionDetailMapper.selectList(
                new LambdaQueryWrapper<BizCommissionDetailb>()
                        .eq(BizCommissionDetailb::getPayeeType, "DEVELOPER")
                        .eq(BizCommissionDetailb::getPayeeId, developer.getId())
                        .eq(BizCommissionDetailb::getIsDeleted, 0)
        ).stream().map(BizCommissionDetailb::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

    /**
     * 获取发展人列表（供网点管理员录单时选择发展人）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('PROVINCE','CITY','OUTLET')")
    public Result<List<Map<String, Object>>> listDevelopers(
            @RequestParam(required = false) Long outletId) {
        LambdaQueryWrapper<BizDeveloper> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloper::getIsDeleted, 0);
        if (outletId != null) {
            wrapper.eq(BizDeveloper::getOutletId, outletId);
        }
        List<BizDeveloper> developers = developerMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (BizDeveloper dev : developers) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", dev.getId());
            item.put("outletId", dev.getOutletId());
            item.put("developerType", dev.getDeveloperType());
            item.put("shopName", dev.getShopName());
            // 查询关联用户的真实姓名
            SysUser user = userService.getById(dev.getUserId());
            item.put("name", user != null ? user.getRealName() : "未知");
            item.put("phone", user != null ? user.getPhone() : "");
            result.add(item);
        }
        return Result.success(result);
    }
}