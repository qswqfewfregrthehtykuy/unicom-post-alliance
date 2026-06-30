package com.unicom.post.modules.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.common.exception.BusinessException;
import com.unicom.post.common.utils.PasswordUtils;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.auth.mapper.SysUserMapper;
import com.unicom.post.modules.auth.service.SysUserService;
import com.unicom.post.modules.system.domain.entity.SysUserRole;
import com.unicom.post.modules.system.dto.UserCreateRequest;
import com.unicom.post.modules.system.dto.UserUpdateRequest;
import com.unicom.post.modules.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;

    public SysUserServiceImpl(SysUserRoleMapper userRoleMapper) {
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public SysUser findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0));
    }

    @Override
    public SysUser findByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getIsDeleted, 0));
    }

    @Override
    public void updateLoginInfo(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        this.updateById(user);
    }

    @Override
    public Page<SysUser> queryUserList(String role, Long cityId, Integer status, String keyword,
                                       Integer pageNo, Integer pageSize) {
        Page<SysUser> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsDeleted, 0);

        if (cityId != null) {
            wrapper.eq(SysUser::getScopeCityId, cityId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }

        // ✅ 新增：按角色过滤
        if (role != null && !role.isEmpty()) {
            // 使用子查询：用户ID必须在 sys_user_role 中关联了该角色
            wrapper.inSql(SysUser::getId,
                    "SELECT user_id FROM sys_user_role ur " +
                            "INNER JOIN sys_role r ON ur.role_id = r.id " +
                            "WHERE r.role_code = '" + role + "'"
            );
        }

        wrapper.orderByDesc(SysUser::getCreatedAt);
        return this.page(page, wrapper);
    }

    @Override
    @Transactional
    public SysUser createUser(UserCreateRequest request) {
        if (this.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        if (this.findByPhone(request.getPhone()) != null) {
            throw new BusinessException("手机号已被注册");
        }
        if ("OUTLET".equals(request.getDataScopeType()) && request.getScopeOutletId() == null) {
            throw new BusinessException("OUTLET角色必须指定scopeOutletId");
        }
        if ("CITY".equals(request.getDataScopeType()) && request.getScopeCityId() == null) {
            throw new BusinessException("CITY角色必须指定scopeCityId");
        }
        String rawPassword = PasswordUtils.generateRandomPassword();
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(PasswordUtils.encode(rawPassword));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setDataScopeType(request.getDataScopeType());
        user.setScopeCityId(request.getScopeCityId());
        user.setScopeOutletId(request.getScopeOutletId());
        user.setStatus(1);
        user.setRemark(request.getRemark());
        this.save(user);

        // 分配角色
        assignRoles(user.getId(), request.getRoleIds());

        log.info("用户 {} 创建成功，初始密码：{}", user.getUsername(), rawPassword);
        user.setTempPassword(rawPassword);
        return user;
    }

    @Override
    @Transactional
    public boolean updateUser(Long userId, UserUpdateRequest request) {
        SysUser exist = this.getById(userId);
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        if (request.getRealName() != null) exist.setRealName(request.getRealName());
        if (request.getPhone() != null) {
            SysUser other = this.findByPhone(request.getPhone());
            if (other != null && !other.getId().equals(userId)) {
                throw new BusinessException("手机号已被其他用户使用");
            }
            exist.setPhone(request.getPhone());
        }
        if (request.getDataScopeType() != null) {
            exist.setDataScopeType(request.getDataScopeType());
            if ("OUTLET".equals(request.getDataScopeType()) && request.getScopeOutletId() == null) {
                throw new BusinessException("OUTLET角色必须指定scopeOutletId");
            }
            exist.setScopeOutletId(request.getScopeOutletId());
            if ("CITY".equals(request.getDataScopeType()) && request.getScopeCityId() == null) {
                throw new BusinessException("CITY角色必须指定scopeCityId");
            }
            exist.setScopeCityId(request.getScopeCityId());
        }
        if (request.getRemark() != null) exist.setRemark(request.getRemark());
        this.updateById(exist);

        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(userId, request.getRoleIds());
        }
        return true;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer status) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        return this.updateById(user);
    }

    @Override
    public boolean deleteUser(Long userId) {
        return this.removeById(userId);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除旧关联
        userRoleMapper.deleteByUserId(userId);
        // 插入新关联
        if (!roleIds.isEmpty()) {
            List<SysUserRole> list = roleIds.stream().map(roleId -> {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());
            userRoleMapper.insertBatch(list);
        }
    }


    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username)
                .eq(SysUser::getIsDeleted, 0);
        return this.getOne(wrapper);
    }


    // 在 SysUserServiceImpl 中实现
    @Override
    public List<Long> listUserIdsByCityId(Long cityId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getScopeCityId, cityId)
                .eq(SysUser::getIsDeleted, 0)
                .select(SysUser::getId);
        return this.listObjs(wrapper, obj -> (Long) obj);
    }
}