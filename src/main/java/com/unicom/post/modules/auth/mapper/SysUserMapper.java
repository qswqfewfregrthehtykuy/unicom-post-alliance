package com.unicom.post.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unicom.post.modules.auth.domain.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}