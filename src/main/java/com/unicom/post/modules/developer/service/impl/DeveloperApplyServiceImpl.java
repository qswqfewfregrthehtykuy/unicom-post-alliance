package com.unicom.post.modules.developer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.utils.PasswordUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.developer.domain.entity.BizAuditLog;
import com.unicom.post.modules.developer.domain.entity.BizDeveloper;
import com.unicom.post.modules.developer.domain.entity.BizDeveloperApply;
import com.unicom.post.modules.developer.dto.DeveloperApplyAuditRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyRequest;
import com.unicom.post.modules.developer.dto.DeveloperApplyResponse;
import com.unicom.post.modules.developer.dto.DeveloperCreateRequest;
import com.unicom.post.modules.developer.mapper.BizAuditLogMapper;
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
    private final BizAuditLogMapper auditLogMapper;

    public DeveloperApplyServiceImpl(BizOutletService outletService,
                                     SysUserService userService,
                                     SysRoleService roleService,
                                     BizDeveloperMapper developerMapper,
                                     SysCityMapper cityMapper,
                                     SysDistrictMapper districtMapper,
                                     BizAuditLogMapper auditLogMapper) {
        this.outletService = outletService;
        this.userService = userService;
        this.roleService = roleService;
        this.developerMapper = developerMapper;
        this.cityMapper = cityMapper;
        this.districtMapper = districtMapper;
        this.auditLogMapper = auditLogMapper;
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
            // 查询审核日志
            resp.setAuditLog(buildAuditLogList(apply.getId()));
            resp.setAuditHistory(Collections.emptyList()); // 列表不返回详细审核历史
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
        resp.setAuditHistory(buildAuditHistoryList(apply.getId()));
        resp.setAuditLog(buildAuditLogList(apply.getId()));
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

        // 记录审核日志
        saveAuditLog("DEVELOPER_APPLY", applyId, auditLevel, null,
                currentUserId, isApproved ? "APPROVED" : "REJECTED",
                request.getAuditRemark());

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
        // 记录审核日志
        saveAuditLog("DEVELOPER_APPLY", applyId, "OUTLET", null,
                currentUserId, "REJECTED", rejectReason);
    }

    @Override
    @Transactional
    public Map<String, Object> createDeveloper(DeveloperCreateRequest request, Long currentUserId) {
        // 0. 获取当前用户信息，确定角色权限
        SysUser currentUser = userService.getById(currentUserId);
        if (currentUser == null) {
            throw new BusinessException("当前用户不存在");
        }
        String dataScopeType = currentUser.getDataScopeType();

        // 根据角色锁定归属范围
        if ("OUTLET".equals(dataScopeType)) {
            // 网点管理员：强制归属本网点，忽略请求中的地市/区县/网点
            Long scopeOutletId = currentUser.getScopeOutletId();
            if (scopeOutletId == null) {
                throw new BusinessException("网点管理员未配置归属网点");
            }
            BizOutlet outlet = outletService.getById(scopeOutletId);
            if (outlet == null || outlet.getStatus() == 0) {
                throw new BusinessException("归属网点不存在或已停用");
            }
            request.setOutletId(scopeOutletId);
            request.setCityId(outlet.getCityId());
            request.setDistrictId(outlet.getDistrictId());
        } else if ("CITY".equals(dataScopeType)) {
            // 地市管理员：只能在本市范围内选择网点
            Long scopeCityId = currentUser.getScopeCityId();
            if (scopeCityId == null) {
                throw new BusinessException("地市管理员未配置归属地市");
            }
            if (!scopeCityId.equals(request.getCityId())) {
                throw new BusinessException("只能在本市范围内创建发展人");
            }
            // 校验网点属于该市
            BizOutlet outlet = outletService.getById(request.getOutletId());
            if (outlet == null || outlet.getStatus() == 0) {
                throw new BusinessException("网点不存在或已停用");
            }
            if (!scopeCityId.equals(outlet.getCityId())) {
                throw new BusinessException("所选网点不在您的管辖范围内");
            }
        }
        // PROVINCE 管理员：无额外限制，使用请求中的参数

        // 默认发展人类型为 SELF_EMPLOYED（自营网点人员）
        if (!StringUtils.hasText(request.getDeveloperType())) {
            request.setDeveloperType("SELF_EMPLOYED");
        }

        // 1. 校验手机号未被注册
        if (userService.findByPhone(request.getApplicantPhone()) != null) {
            throw new BusinessException("手机号已被注册，无法创建");
        }
        // 2. 校验身份证号未被重复申请
        LambdaQueryWrapper<BizDeveloperApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizDeveloperApply::getIdCard, request.getIdCard())
                .ne(BizDeveloperApply::getStatus, "REJECTED")
                .eq(BizDeveloperApply::getIsDeleted, 0);
        if (this.count(wrapper) > 0) {
            throw new BusinessException("该身份证号已有申请记录");
        }
        // 3. 校验网点存在且启用
        BizOutlet outlet = outletService.getById(request.getOutletId());
        if (outlet == null || outlet.getStatus() == 0) {
            throw new BusinessException("网点不存在或已停用");
        }
        // 4. 校验区域一致
        if (!outlet.getCityId().equals(request.getCityId()) ||
                !outlet.getDistrictId().equals(request.getDistrictId())) {
            throw new BusinessException("地市区县与网点不匹配");
        }

        // 5. 创建申请记录（直接设为省级通过）
        BizDeveloperApply apply = new BizDeveloperApply();
        apply.setApplicantName(request.getApplicantName());
        apply.setApplicantPhone(request.getApplicantPhone());
        apply.setIdCard(request.getIdCard());
        apply.setDeveloperType(request.getDeveloperType());
        apply.setCityId(request.getCityId());
        apply.setDistrictId(request.getDistrictId());
        apply.setOutletId(request.getOutletId());
        apply.setShopName(request.getShopName());
        apply.setShopAddress(request.getShopAddress());
        apply.setApplyReason("管理员直接创建");
        apply.setStatus("PROVINCE_APPROVED");
        apply.setReviewerId(currentUserId);
        apply.setReviewAt(LocalDateTime.now());
        apply.setReviewRemark("管理员直接创建，跳过审核流程");
        this.save(apply);

        // 6. 自动创建发展人用户账号
        String rawPassword = PasswordUtils.generateRandomPassword();
        String encodedPassword = PasswordUtils.encode(rawPassword);

        SysUser user = new SysUser();
        user.setUsername(request.getApplicantPhone());
        user.setPasswordHash(encodedPassword);
        user.setRealName(request.getApplicantName());
        user.setPhone(request.getApplicantPhone());
        user.setIdCard(request.getIdCard());
        user.setDataScopeType("OUTLET");
        user.setScopeCityId(request.getCityId());
        user.setScopeOutletId(request.getOutletId());
        user.setStatus(1);
        userService.save(user);

        Long roleId = getDeveloperRoleId();
        userService.assignRoles(user.getId(), Collections.singletonList(roleId));

        // 7. 创建发展人记录
        BizDeveloper developer = new BizDeveloper();
        developer.setUserId(user.getId());
        developer.setApplyId(apply.getId());
        developer.setOutletId(request.getOutletId());
        developer.setDeveloperType(request.getDeveloperType());
        developer.setShopName(request.getShopName());
        developer.setShopAddress(request.getShopAddress());
        developer.setCreatedBy(currentUserId);
        developer.setCreatedOutletAdmin(request.getOutletId());
        developer.setStatus(1);
        developer.setJoinDate(LocalDate.now());
        developerMapper.insert(developer);

        apply.setUserId(user.getId());
        this.updateById(apply);

        // 8. 记录审核日志
        saveAuditLog("DEVELOPER_APPLY", apply.getId(), "PROVINCE", null,
                currentUserId, "APPROVED", "管理员直接创建");

        log.info("管理员直接创建发展人成功，手机号：{}，初始密码：{}", request.getApplicantPhone(), rawPassword);

        // 9. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("applyId", apply.getId());
        result.put("userId", user.getId());
        result.put("developerId", developer.getId());
        result.put("username", user.getUsername());
        result.put("tempPassword", rawPassword);
        result.put("status", "PROVINCE_APPROVED");
        result.put("message", "发展人创建成功，初始密码已生成");
        return result;
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
        user.setScopeCityId(apply.getCityId());
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

    // ==================== 审核日志辅助方法 ====================

    /**
     * 保存审核日志
     */
    private void saveAuditLog(String targetType, Long targetId, String auditLevel,
                              String auditPhase, Long auditorId, String auditStatus,
                              String auditRemark) {
        SysUser auditor = userService.getById(auditorId);
        String auditorName = auditor != null ? auditor.getRealName() : "未知";

        BizAuditLog log = new BizAuditLog();
        log.setTargetType(targetType);
        log.setTargetId(targetId);
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
     * 构建审核日志列表（用于列表展示，只显示各级审核状态摘要）
     */
    private List<DeveloperApplyResponse.AuditLog> buildAuditLogList(Long applyId) {
        LambdaQueryWrapper<BizAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAuditLog::getTargetType, "DEVELOPER_APPLY")
                .eq(BizAuditLog::getTargetId, applyId)
                .orderByAsc(BizAuditLog::getCreatedAt);
        List<BizAuditLog> logs = auditLogMapper.selectList(wrapper);

        // 构建预定义的各级审核状态
        String[] levels = {"OUTLET", "CITY", "PROVINCE"};
        List<DeveloperApplyResponse.AuditLog> result = new ArrayList<>();
        for (String level : levels) {
            DeveloperApplyResponse.AuditLog al = new DeveloperApplyResponse.AuditLog();
            al.setAuditLevel(level);
            al.setAuditorName(null);
            al.setAuditStatus("PENDING");
            al.setAuditTime(null);
            // 查找该级别的实际审核记录
            for (BizAuditLog log : logs) {
                if (level.equals(log.getAuditLevel())) {
                    al.setAuditorName(log.getAuditorName());
                    al.setAuditStatus(log.getAuditStatus());
                    al.setAuditTime(log.getAuditTime());
                    break;
                }
            }
            result.add(al);
        }
        return result;
    }

    /**
     * 构建审核历史列表（用于详情展示，返回实际审核记录）
     */
    private List<DeveloperApplyResponse.AuditHistory> buildAuditHistoryList(Long applyId) {
        LambdaQueryWrapper<BizAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizAuditLog::getTargetType, "DEVELOPER_APPLY")
                .eq(BizAuditLog::getTargetId, applyId)
                .orderByAsc(BizAuditLog::getCreatedAt);
        List<BizAuditLog> logs = auditLogMapper.selectList(wrapper);

        List<DeveloperApplyResponse.AuditHistory> result = new ArrayList<>();
        for (BizAuditLog log : logs) {
            DeveloperApplyResponse.AuditHistory history = new DeveloperApplyResponse.AuditHistory();
            history.setAuditLevel(log.getAuditLevel());
            history.setAuditorId(log.getAuditorId());
            history.setAuditorName(log.getAuditorName());
            history.setAuditStatus(log.getAuditStatus());
            history.setAuditRemark(log.getAuditRemark());
            history.setAuditTime(log.getAuditTime());
            result.add(history);
        }
        return result;
    }
}