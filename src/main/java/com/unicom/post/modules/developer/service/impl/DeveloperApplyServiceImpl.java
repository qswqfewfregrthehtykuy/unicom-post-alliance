package com.unicom.post.modules.developer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.utils.PasswordUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.dto.DeveloperApplyAuditRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyResponse;
import com.unicom.post.modules.developer.mapper.BizDeveloperApplyMapper;
import com.unicom.post.modules.developer.mapper.BizDeveloperMapper;
import com.unicom.post.modules.developer.service.DeveloperApplyService;
import com.unicom.post.modules.outlet.domain.entity.BizOutlet;
import com.unicom.post.modules.outlet.service.BizOutletService;
import com.unicom.post.modules.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
//import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class DeveloperApplyServiceImpl extends ServiceImpl<BizDeveloperApplyMapper, BizDeveloperApply>
        implements DeveloperApplyService {

    private final BizOutletService outletService;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final BizDeveloperMapper developerMapper;

    public DeveloperApplyServiceImpl(BizOutletService outletService,
                                     SysUserService userService,
                                     SysRoleService roleService,
                                     BizDeveloperMapper developerMapper) {
        this.outletService = outletService;
        this.userService = userService;
        this.roleService = roleService;
        this.developerMapper = developerMapper;
    }

    @Override
    @Transactional
    public BizDeveloperApply submitApply(DeveloperApplyRequest request) {
        // 去重校验
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloperApply::getApplicantPhone, request.getApplicantPhone())
                .or().eq(BizDeveloperApply::getIdCard, request.getIdCard())
                .ne(BizDeveloperApply::getStatus, "REJECTED")
                .eq(BizDeveloperApply::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("手机号或身份证号已存在申请，请勿重复提交");
        }
        if (userService.findByPhone(request.getApplicantPhone()) != null) {
            throw new BusinessException("手机号已被注册，请勿重复申请");
        }
        BizOutlet outlet = outletService.getById(request.getOutletId());
        if (outlet == null || outlet.getStatus() == 0) {
            throw new BusinessException("网点不存在或已停用");
        }
        if (!outlet.getCityId().equals(request.getCityId()) ||
                !outlet.getDistrictId().equals(request.getDistrictId())) {
            throw new BusinessException("申请地市与网点地市不匹配");
        }
        BizDeveloperApply apply = new BizDeveloperApply();
        BeanUtils.copyProperties(request, apply);
        apply.setStatus("PENDING");
        this.save(apply);
        return apply;
    }

    @Override
    public Page<DeveloperApplyResponse> queryApplyList(String status, Long cityId, Long outletId, String keyword,
                                                       Integer pageNo, Integer pageSize, Long currentUserId,
                                                       String currentUserRole) {
        Page<BizDeveloperApply> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloperApply::getIsDeleted, 0);
        if (StringUtils.hasText(status)) wrapper.eq(BizDeveloperApply::getStatus, status);
        if (cityId != null) wrapper.eq(BizDeveloperApply::getCityId, cityId);
        if (outletId != null) wrapper.eq(BizDeveloperApply::getOutletId, outletId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizDeveloperApply::getApplicantName, keyword)
                    .or().like(BizDeveloperApply::getApplicantPhone, keyword));
        }
        // 数据权限过滤可在此扩展
        wrapper.orderByDesc(BizDeveloperApply::getCreatedAt);
        Page<BizDeveloperApply> result = this.page(page, wrapper);
        Page<DeveloperApplyResponse> responsePage = new Page<>(pageNo, pageSize, result.getTotal());
        List<DeveloperApplyResponse> list = new ArrayList<>();
        for (BizDeveloperApply apply : result.getRecords()) {
            DeveloperApplyResponse resp = new DeveloperApplyResponse();
            BeanUtils.copyProperties(apply, resp);
            // 填充名称（略），构建审核日志
            resp.setAuditLog(buildAuditLog(apply));
            list.add(resp);
        }
        responsePage.setRecords(list);
        return responsePage;
    }

    @Override
    public DeveloperApplyResponse getApplyDetail(Long applyId, Long currentUserId, String currentUserRole) {
        BizDeveloperApply apply = this.getById(applyId);
        if (apply == null || apply.getIsDeleted() == 1) {
            throw new BusinessException("申请不存在");
        }
        DeveloperApplyResponse resp = new DeveloperApplyResponse();
        BeanUtils.copyProperties(apply, resp);
        resp.setAuditLog(buildAuditLog(apply));
        return resp;
    }

    @Override
    @Transactional
    public void auditApply(Long applyId, DeveloperApplyAuditRequest request, Long currentUserId, String currentUserRole) {
        BizDeveloperApply apply = this.getById(applyId);
        if (apply == null || apply.getIsDeleted() == 1) {
            throw new BusinessException("申请不存在");
        }
        String currentStatus = apply.getStatus();
        if ("REJECTED".equals(currentStatus) || "PROVINCE_APPROVED".equals(currentStatus)) {
            throw new BusinessException("申请已结束，无法再次审核");
        }
        String auditLevel = request.getAuditLevel();
        String targetStatus = request.getStatus();
        boolean isApproved = "APPROVED".equals(targetStatus);
        int levelOrder = getLevelOrder(auditLevel);
        int currentLevel = getLevelOrderByRole(currentUserRole);
        if (levelOrder != currentLevel) {
            throw new BusinessException(403, "您没有权限审核该级别");
        }
        if (!isValidTransition(currentStatus, auditLevel)) {
            throw new BusinessException("当前状态不允许进行该级别审核");
        }
        BizOutlet outlet = outletService.getById(apply.getOutletId());
        if (outlet == null) {
            throw new BusinessException("网点不存在");
        }
        if (!outlet.getCityId().equals(apply.getCityId()) ||
                !outlet.getDistrictId().equals(apply.getDistrictId())) {
            throw new BusinessException("申请区域与网点区域不匹配，审核已拒绝");
        }
        String newStatus;
        if (isApproved) {
            if ("OUTLET".equals(auditLevel)) newStatus = "OUTLET_APPROVED";
            else if ("CITY".equals(auditLevel)) newStatus = "CITY_APPROVED";
            else if ("PROVINCE".equals(auditLevel)) newStatus = "PROVINCE_APPROVED";
            else throw new BusinessException("未知审核级别");
            if ("PROVINCE_APPROVED".equals(newStatus)) {
                createDeveloperUser(apply);
            }
        } else {
            newStatus = "REJECTED";
        }
        apply.setStatus(newStatus);
        apply.setReviewerId(currentUserId);
        apply.setReviewAt(LocalDateTime.now());
        apply.setReviewRemark(request.getAuditRemark());
        this.updateById(apply);
    }

    @Override
    @Transactional
    public void rejectApply(Long applyId, String rejectReason, Long currentUserId) {
        BizDeveloperApply apply = this.getById(applyId);
        if (apply == null) throw new BusinessException("申请不存在");
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BusinessException("只有待审核的申请可以拒绝");
        }
        apply.setStatus("REJECTED");
        apply.setReviewerId(currentUserId);
        apply.setReviewAt(LocalDateTime.now());
        apply.setReviewRemark(rejectReason);
        this.updateById(apply);
    }

    private void createDeveloperUser(BizDeveloperApply apply) {
        if (userService.findByPhone(apply.getApplicantPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }
        String rawPassword = PasswordUtils.generateRandomPassword();
        SysUser user = new SysUser();
        user.setUsername(apply.getApplicantPhone());
        user.setPasswordHash(PasswordUtils.encode(rawPassword));
        user.setRealName(apply.getApplicantName());
        user.setPhone(apply.getApplicantPhone());
        user.setIdCard(apply.getIdCard());
        user.setDataScopeType("OUTLET");
        user.setScopeOutletId(apply.getOutletId());
        user.setStatus(1);
        userService.save(user);

        Long roleId = getDeveloperRoleId();
        userService.assignRoles(user.getId(), Arrays.asList(roleId));

        BizDeveloper developer = new BizDeveloper();
        developer.setUserId(user.getId());
        developer.setApplyId(apply.getId());
        developer.setOutletId(apply.getOutletId());
        developer.setDeveloperType(apply.getDeveloperType());
        developer.setShopName(apply.getShopName());
        developer.setShopAddress(apply.getShopAddress());
        developer.setCreatedBy(apply.getReviewerId());
        developer.setCreatedOutletAdmin(apply.getOutletId());
        developer.setStatus(1);
        developer.setJoinDate(LocalDate.now());
        developerMapper.insert(developer);

        apply.setUserId(user.getId());
        this.updateById(apply);

        log.info("发展人创建成功，手机号：{}，初始密码：{}", apply.getApplicantPhone(), rawPassword);
    }

    private Long getDeveloperRoleId() {
        LambdaQueryWrapper<com.unicom.post.modules.system.domain.entity.SysRole> wrapper =
                new LambdaQueryWrapper<>();
        wrapper.eq(com.unicom.post.modules.system.domain.entity.SysRole::getRoleCode, "ROLE_DEVELOPER")
                .eq(com.unicom.post.modules.system.domain.entity.SysRole::getIsDeleted, 0);
        var role = roleService.getOne(wrapper);
        if (role == null) throw new BusinessException("角色 ROLE_DEVELOPER 不存在");
        return role.getId();
    }

    private List<DeveloperApplyResponse.AuditLog> buildAuditLog(BizDeveloperApply apply) {
        List<DeveloperApplyResponse.AuditLog> logs = new ArrayList<>();
        // 简化：根据状态填充示例
        DeveloperApplyResponse.AuditLog outlet = new DeveloperApplyResponse.AuditLog();
        outlet.setAuditLevel("OUTLET");
        outlet.setAuditStatus(apply.getStatus().equals("PENDING") ? "PENDING" : "APPROVED");
        logs.add(outlet);
        DeveloperApplyResponse.AuditLog city = new DeveloperApplyResponse.AuditLog();
        city.setAuditLevel("CITY");
        city.setAuditStatus(apply.getStatus().equals("CITY_APPROVED") ? "APPROVED" : "PENDING");
        logs.add(city);
        DeveloperApplyResponse.AuditLog province = new DeveloperApplyResponse.AuditLog();
        province.setAuditLevel("PROVINCE");
        province.setAuditStatus(apply.getStatus().equals("PROVINCE_APPROVED") ? "APPROVED" : "PENDING");
        logs.add(province);
        return logs;
    }

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

    private boolean isValidTransition(String currentStatus, String auditLevel) {
        if ("PENDING".equals(currentStatus) && "OUTLET".equals(auditLevel)) return true;
        if ("OUTLET_APPROVED".equals(currentStatus) && "CITY".equals(auditLevel)) return true;
        if ("CITY_APPROVED".equals(currentStatus) && "PROVINCE".equals(auditLevel)) return true;
        return false;
    }
}