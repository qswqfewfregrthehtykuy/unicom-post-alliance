package com.unicom.post.modules.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import com.unicom.post.modules.system.dto.UserCreateRequest;
import com.unicom.post.modules.system.dto.UserUpdateRequest;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
    SysUser findByUsername(String username);
    SysUser findByPhone(String phone);
    void updateLoginInfo(Long userId);

    // 新增：用于账号管理
    Page<SysUser> queryUserList(String role, Long cityId, Integer status, String keyword, Integer pageNo, Integer pageSize);
    SysUser createUser(UserCreateRequest request);
    boolean updateUser(Long userId, UserUpdateRequest request);
    boolean updateUserStatus(Long userId, Integer status);
    boolean deleteUser(Long userId);
    void assignRoles(Long userId, List<Long> roleIds);

    SysUser getByUsername(String username);
}