package com.unicom.post.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.unicom.post.modules.system.domain.entity.SysRole;
import com.unicom.post.modules.system.mapper.SysRoleMapper;
import com.unicom.post.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Override
    public List<String> findRoleCodesByUserId(Long userId) {
        return baseMapper.findRoleCodesByUserId(userId);
    }
}