package com.unicom.post.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unicom.post.modules.system.domain.entity.SysRole;
import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    List<String> findRoleCodesByUserId(Long userId);
}