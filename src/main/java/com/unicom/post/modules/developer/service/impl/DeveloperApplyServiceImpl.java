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
import com.unicom.post.modules.system.domain.entity.SysCity;
import com.unicom.post.modules.system.domain.entity.SysDistrict;
import com.unicom.post.modules.system.domain.entity.SysRole;
import com.unicom.post.modules.system.mapper.SysCityMapper;
import com.unicom.post.modules.system.mapper.SysDistrictMapper;
import com.unicom.post.modules.system.service.SysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class DeveloperApplyServiceImpl extends ServiceImpl<BizDeveloperApplyMapper, BizDeveloperApply>
        implements DeveloperApplyService {

    private final BizOutletService outletService;
    private final SysUserService userService;
    private final SysRoleService roleService;
    private final BizDeveloperMapper developerMapper;
    private final SysCityMapper cityMapper;
    private final SysDistrictMapper districtMapper;

    public DeveloperApplyServiceImpl(BizOutletService outletService,
                                     SysUserService userService,
                                     SysRoleService roleService,
                                     BizDeveloperMapper developerMapper,
                                     SysCityMapper cityMapper,
                                     SysDistrictMapper districtMapper) {
        this.outletService = outletService;
        this.userService = userService;
        this.roleService = roleService;
        this.developerMapper = developerMapper;
        this.cityMapper = cityMapper;
        this.districtMapper = districtMapper;
    }

    @Override
    @Transactional
    public BizDeveloperApply submitApply(DeveloperApplyRequest request) {
        // 去重校验：手机号或身份证号已存在且未拒绝的申请
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(BizDeveloperApply::getApplicantPhone, request.getApplicantPhone())
                        .or().eq(BizDeveloperApply::getIdCard, request.getIdCard()))
                .ne(BizDeveloperApply::getStatus, "REJECTED")
                .eq(BizDeveloperApply::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("手机号或身份证号已存在申请，请勿重复提交");
        }
        if (userService.findByPhone(request.getApplicantPhone()) != null) {
            throw new BusinessException("手机号已被注册，请勿重复申请");
        }

        // 校验网点存在且启用
        BizOutlet outlet = outletService.getById(request.getOutletId());
        if (outlet == null || outlet.getStatus() == 0) {
            throw new BusinessException("网点不存在或已停用");
        }

        // 校验申请区域与网点区域一致
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
                                                       Integer pageNo, Integer pageSize,
                                                       Long currentUserId, String currentUserRole) {
        Page<BizDeveloperApply> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloperApply::getIsDeleted, 0);

        // 基础查询条件
        if (StringUtils.hasText(status)) {
            wrapper.eq(BizDeveloperApply::getStatus, status);
        }
        if (cityId != null) {
            wrapper.eq(BizDeveloperApply::getCityId, cityId);
        }
        if (outletId != null) {
            wrapper.eq(BizDeveloperApply::getOutletId, outletId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BizDeveloperApply::getApplicantName, keyword)
                    .or().like(BizDeveloperApply::getApplicantPhone, keyword));
        }

        // 数据权限过滤
        SysUser currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        String dataScopeType = currentUser.getDataScopeType();
        if ("CITY".equals(dataScopeType)) {
            Long scopeCityId = currentUser.getScopeCityId();
            if (scopeCityId == null) {
                throw new BusinessException("地市管理员未配置归属地市");
            }
            wrapper.eq(BizDeveloperApply::getCityId, scopeCityId);
        } else if ("OUTLET".equals(dataScopeType)) {
            Long scopeOutletId = currentUser.getScopeOutletId();
            if (scopeOutletId == null) {
                throw new BusinessException("网点管理员未配置归属网点");
            }
            wrapper.eq(BizDeveloperApply::getOutletId, scopeOutletId);
        }

        wrapper.orderByDesc(BizDeveloperApply::getCreatedAt);
        Page<BizDeveloperApply> result = this.page(page, wrapper);

        // 转换为响应DTO
        Page<DeveloperApplyResponse> responsePage = new Page<>(pageNo, pageSize, result.getTotal());
        List<DeveloperApplyResponse> list = new ArrayList<>();
        for (BizDeveloperApply apply : result.getRecords()) {
            DeveloperApplyResponse resp = new DeveloperApplyResponse();
            BeanUtils.copyProperties(apply, resp);
            fillNames(resp);
            resp.setAuditHistory(Collections.emptyList());
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

        // 权限校验
        SysUser currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        String dataScopeType = currentUser.getDataScopeType();
        if ("CITY".equals(dataScopeType)) {
            Long scopeCityId = currentUser.getScopeCityId();
            if (!apply.getCityId().equals(scopeCityId)) {
                throw new BusinessException("无权查看该申请");
            }
        } else if ("OUTLET".equals(dataScopeType)) {
            Long scopeOutletId = currentUser.getScopeOutletId();
            if (!apply.getOutletId().equals(scopeOutletId)) {
                throw new BusinessException("无权查看该申请");
            }
        }

        DeveloperApplyResponse resp = new DeveloperApplyResponse();
        BeanUtils.copyProperties(apply, resp);
        fillNames(resp);
        resp.setAuditHistory(Collections.emptyList());
        return resp;
    }

    @Override
    @Transactional
    public Map<String, Object> auditApply(Long applyId, DeveloperApplyAuditRequest request,
                                          Long currentUserId, String currentUserRole) {
        BizDeveloperApply apply = this.getById(applyId);
        if (apply == null || apply.getIsDeleted() == 1) {
            throw new BusinessException("申请不存在");
        }

        String currentStatus = apply.getStatus();
        if ("REJECTED".equals(currentStatus) || "PROVINCE_APPROVED".equals(currentStatus)) {
            throw new BusinessException("申请已结束，无法再次审核");
        }

        String auditLevel = request.getAuditLevel(); // OUTLET / CITY / PROVINCE
        int levelOrder = getLevelOrder(auditLevel);
        int userLevel = getLevelOrderByRole(currentUserRole);
        if (levelOrder != userLevel) {
            throw new BusinessException(403, "您没有权限审核该级别");
        }

        // 状态机校验
        if (!isValidTransition(currentStatus, auditLevel)) {
            throw new BusinessException("当前状态不允许进行该级别审核");
        }

        // 区域校验
        BizOutlet outlet = outletService.getById(apply.getOutletId());
        if (outlet == null) {
            throw new BusinessException("网点不存在");
        }
        if (!outlet.getCityId().equals(apply.getCityId()) ||
                !outlet.getDistrictId().equals(apply.getDistrictId())) {
            throw new BusinessException("申请区域与网点区域不匹配，审核已拒绝");
        }

        boolean isApproved = "APPROVED".equals(request.getStatus());
        String newStatus;
        String nextAuditorLevel = null;

        if (isApproved) {
            // 通过：推进状态
            switch (auditLevel) {
                case "OUTLET":
                    newStatus = "OUTLET_APPROVED";
                    nextAuditorLevel = "CITY";
                    break;
                case "CITY":
                    newStatus = "CITY_APPROVED";
                    nextAuditorLevel = "PROVINCE";
                    break;
                case "PROVINCE":
                    newStatus = "PROVINCE_APPROVED";
                    nextAuditorLevel = null;
                    break;
                default:
                    throw new BusinessException("未知审核级别");
            }
            // 省级通过时创建发展人账号
            if ("PROVINCE_APPROVED".equals(newStatus)) {
                createDeveloperUser(apply);
            }
        } else {
            // 拒绝
            newStatus = "REJECTED";
            nextAuditorLevel = null;
        }

        // 更新申请
        apply.setStatus(newStatus);
        apply.setReviewerId(currentUserId);
        apply.setReviewAt(LocalDateTime.now());
        apply.setReviewRemark(request.getAuditRemark());
        this.updateById(apply);

        log.info("申请审核完成，applyId: {}, 新状态: {}", applyId, newStatus);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("applyId", applyId);
        result.put("status", newStatus);
        if (nextAuditorLevel != null) {
            Map<String, String> nextAuditor = new HashMap<>();
            nextAuditor.put("level", nextAuditorLevel);
            nextAuditor.put("role", "ROLE_" + nextAuditorLevel);
            nextAuditor.put("needsReview", "true");
            result.put("nextAuditor", nextAuditor);
        } else {
            result.put("nextAuditor", null);
        }
        if ("PROVINCE_APPROVED".equals(newStatus)) {
            result.put("smsStatus", "sent");
        }
        return result;
    }

    @Override
    @Transactional
    public void rejectApply(Long applyId, String rejectReason, Long currentUserId) {
        BizDeveloperApply apply = this.getById(applyId);
        if (apply == null) {
            throw new BusinessException("申请不存在");
        }
        if (!"PENDING".equals(apply.getStatus())) {
            throw new BusinessException("只有待审核的申请可以拒绝");
        }
        apply.setStatus("REJECTED");
        apply.setReviewerId(currentUserId);
        apply.setReviewAt(LocalDateTime.now());
        apply.setReviewRemark(rejectReason);
        this.updateById(apply);
    }

    // ==================== 私有辅助方法 ====================

    private void fillNames(DeveloperApplyResponse resp) {
        if (resp.getCityId() != null) {
            SysCity city = cityMapper.selectById(resp.getCityId());
            if (city != null) {
                resp.setCityName(city.getCityName());
            }
        }
        if (resp.getDistrictId() != null) {
            SysDistrict district = districtMapper.selectById(resp.getDistrictId());
            if (district != null) {
                resp.setDistrictName(district.getDistrictName());
            }
        }
        if (resp.getOutletId() != null) {
            BizOutlet outlet = outletService.getById(resp.getOutletId());
            if (outlet != null) {
                resp.setOutletName(outlet.getOutletName());
            }
        }
    }

    private void createDeveloperUser(BizDeveloperApply apply) {
        if (userService.findByPhone(apply.getApplicantPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }

        String rawPassword = PasswordUtils.generateRandomPassword();
        String encodedPassword = PasswordUtils.encode(rawPassword);

        SysUser user = new SysUser();
        user.setUsername(apply.getApplicantPhone());
        user.setPasswordHash(encodedPassword);
        user.setRealName(apply.getApplicantName());
        user.setPhone(apply.getApplicantPhone());
        user.setIdCard(apply.getIdCard());
        user.setDataScopeType("OUTLET");
        user.setScopeOutletId(apply.getOutletId());
        user.setStatus(1);
        userService.save(user);

        Long roleId = getDeveloperRoleId();
        userService.assignRoles(user.getId(), Collections.singletonList(roleId));

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
        // 实际调用短信服务
        // smsService.send(apply.getApplicantPhone(), "您的初始密码：" + rawPassword);
    }

    private Long getDeveloperRoleId() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, "ROLE_DEVELOPER")
                .eq(SysRole::getIsDeleted, 0);
        SysRole role = roleService.getOne(wrapper);
        if (role == null) {
            throw new BusinessException("角色 ROLE_DEVELOPER 不存在");
        }
        return role.getId();
    }

    private int getLevelOrder(String level) {
        if ("OUTLET".equals(level)) return 1;
        if ("CITY".equals(level)) return 2;
        if ("PROVINCE".equals(level)) return 3;
        throw new BusinessException("未知审核级别");
    }

    private int getLevelOrderByRole(String role) {
        if ("ROLE_OUTLET".equals(role) || "OUTLET".equals(role)) return 1;
        if ("ROLE_CITY".equals(role) || "CITY".equals(role)) return 2;
        if ("ROLE_PROVINCE".equals(role) || "PROVINCE".equals(role)) return 3;
        throw new BusinessException("当前角色无审核权限: " + role);
    }

    private boolean isValidTransition(String currentStatus, String auditLevel) {
        if ("PENDING".equals(currentStatus) && "OUTLET".equals(auditLevel)) return true;
        if ("OUTLET_APPROVED".equals(currentStatus) && "CITY".equals(auditLevel)) return true;
        if ("CITY_APPROVED".equals(currentStatus) && "PROVINCE".equals(auditLevel)) return true;
        return false;
    }
}